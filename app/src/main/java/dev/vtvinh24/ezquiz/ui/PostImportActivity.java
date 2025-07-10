package dev.vtvinh24.ezquiz.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.repo.QuizSetRepository;
import dev.vtvinh24.ezquiz.network.QuizImporter;

public class PostImportActivity extends AppCompatActivity {
  public static final String EXTRA_COLLECTION_ID = "collection_id";
  public static final String EXTRA_SET_NAME = "set_name";

  private long selectedCollectionId = -1;
  private String setName = null;
  private MaterialTextView textStatus;
  private MaterialToolbar toolbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    if (!intent.hasExtra(EXTRA_COLLECTION_ID) || !intent.hasExtra(EXTRA_SET_NAME)) {
      // If missing extras, start step 1
      Intent step1 = new Intent(this, PreImportActivity.class);
      startActivity(step1);
      finish();
      return;
    }

    selectedCollectionId = intent.getLongExtra(EXTRA_COLLECTION_ID, -1);
    setName = intent.getStringExtra(EXTRA_SET_NAME);
    setContentView(R.layout.activity_import_quiz_external);

    initializeViews();
    setupToolbar();
    setupClickListeners();
  }

  private void initializeViews() {
    textStatus = findViewById(R.id.text_import_status);
    toolbar = findViewById(R.id.toolbar);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
  }

  private void setupClickListeners() {
    MaterialButton btnImportManually = findViewById(R.id.btn_import_manually);
    btnImportManually.setOnClickListener(v -> showImportDialog());
  }

  private void showImportDialog() {
    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_import_paste, null);

    TextInputLayout pasteIdLayout = dialogView.findViewById(R.id.text_input_layout);
    TextInputEditText editPasteId = dialogView.findViewById(R.id.edit_paste_id);
    MaterialButton btnImportPaste = dialogView.findViewById(R.id.btn_import_paste);

    MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.title_import_paste)
            .setView(dialogView)
            .setNegativeButton(android.R.string.cancel, null);

    AlertDialog dialog = builder.create();

    btnImportPaste.setOnClickListener(view -> {
      String pasteId = editPasteId.getText().toString().trim();
      if (pasteId.isEmpty()) {
        pasteIdLayout.setError(getString(R.string.prompt_paste_id));
        return;
      }

      dialog.dismiss();
      importFromPasteId(pasteId);
    });

    dialog.show();
  }

  private void importFromPasteId(String pasteId) {
    textStatus.setText(R.string.importing_please_wait);

    Handler mainHandler = new Handler(Looper.getMainLooper());
    Executors.newSingleThreadExecutor().execute(() -> {
      QuizImporter importer = new QuizImporter();
      List<Quiz> quizzes = importer.importFlashcards(pasteId);

      mainHandler.post(() -> {
        if (quizzes.isEmpty()) {
          textStatus.setText(getString(R.string.error_no_items_imported));
          return;
        }

        saveQuizzesToDatabase(quizzes, pasteId);
      });
    });
  }

  private void saveQuizzesToDatabase(List<Quiz> quizzes, String pasteId) {
    AppDatabase db = AppDatabaseProvider.getDatabase(this);
    QuizSetEntity set = new QuizSetEntity();
    set.name = setName;
    set.description = getString(R.string.imported_set_description, pasteId);
    set.collectionId = selectedCollectionId;
    set.createdAt = System.currentTimeMillis();
    set.updatedAt = System.currentTimeMillis();

    QuizSetRepository setRepo = new QuizSetRepository(db);
    long setId = setRepo.insertWithDefaultCollectionIfNeeded(set);
    set = setRepo.getSet(setId);

    for (Quiz quiz : quizzes) {
      QuizEntity entity = new QuizEntity();
      entity.question = quiz.getQuestion();
      entity.answers = quiz.getAnswers();
      entity.correctAnswerIndices = quiz.getCorrectAnswerIndices();
      entity.type = quiz.getType();
      entity.quizSetId = setId;
      entity.createdAt = System.currentTimeMillis();
      entity.updatedAt = System.currentTimeMillis();
      db.quizDao().insert(entity);
    }

    textStatus.setText(getString(R.string.import_success, quizzes.size()));
  }
}
