package dev.vtvinh24.ezquiz.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

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

    Button btnImportManually = findViewById(R.id.btn_import_manually);
    TextView textStatus = findViewById(R.id.text_import_status);
    btnImportManually.setOnClickListener(v -> {
      View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_import_paste, null);
      EditText editPasteId = dialogView.findViewById(R.id.edit_paste_id);
      Button btnImportPaste = dialogView.findViewById(R.id.btn_import_paste);
      AlertDialog dialog = new AlertDialog.Builder(this)
        .setTitle(R.string.title_import_paste)
        .setView(dialogView)
        .setNegativeButton(android.R.string.cancel, null)
        .create();
      btnImportPaste.setOnClickListener(view -> {
        String pasteId = editPasteId.getText().toString().trim();
        if (pasteId.isEmpty()) {
          editPasteId.setError(getString(R.string.prompt_paste_id));
          return;
        }
        dialog.dismiss();
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
          QuizImporter importer = new QuizImporter();
          List<Quiz> quizzes = importer.importFlashcards(pasteId);
          mainHandler.post(() -> {
            if (quizzes.isEmpty()) {
              textStatus.setText(getString(R.string.error_no_items_imported));
              return;
            }
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
          });
        });
      });
      dialog.show();
    });
  }
}
