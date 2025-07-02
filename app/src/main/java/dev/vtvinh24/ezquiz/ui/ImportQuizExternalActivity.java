package dev.vtvinh24.ezquiz.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.repo.QuizCollectionRepository;
import dev.vtvinh24.ezquiz.data.repo.QuizSetRepository;
import dev.vtvinh24.ezquiz.network.QuizImporter;

public class ImportQuizExternalActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_import_quiz_external);

    Spinner spinnerCollection = findViewById(R.id.spinner_collection);
    EditText editSetName = findViewById(R.id.edit_set_name);
    Button btnImportManually = findViewById(R.id.btn_import_manually);
    TextView textStatus = findViewById(R.id.text_import_status);

    // Populate collection spinner
    AppDatabase db = AppDatabaseProvider.getDatabase(this);
    QuizCollectionRepository collectionRepo = new QuizCollectionRepository(db);
    List<QuizCollectionEntity> collections = collectionRepo.getAllCollections();
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    for (QuizCollectionEntity c : collections) {
      adapter.add(c.name);
    }
    spinnerCollection.setAdapter(adapter);

    btnImportManually.setOnClickListener(v -> {
      View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_import_paste, null);
      EditText editPasteId = dialogView.findViewById(R.id.edit_paste_id);
      Button btnImportPaste = dialogView.findViewById(R.id.btn_import_paste);
      AlertDialog dialog = new AlertDialog.Builder(this).setTitle(R.string.title_import_paste).setView(dialogView).setNegativeButton(android.R.string.cancel, null).create();
      btnImportPaste.setOnClickListener(view -> {
        String pasteId = editPasteId.getText().toString().trim();
        if (pasteId.isEmpty()) {
          editPasteId.setError(getString(R.string.prompt_paste_id));
          return;
        }
        dialog.dismiss();
        // Start import
        Handler mainHandler = new Handler(Looper.getMainLooper());
        Executors.newSingleThreadExecutor().execute(() -> {
          QuizImporter importer = new QuizImporter();
          List<Quiz> quizzes;
          // TODO: Update importer to handle different formats
          quizzes = importer.importFlashcards(pasteId);
          mainHandler.post(() -> {
            if (quizzes.isEmpty()) {
              textStatus.setText(getString(R.string.error_no_items_imported));
              return;
            }
            // Use selected collection
            int selectedIdx = spinnerCollection.getSelectedItemPosition();
            long collectionId = collections.get(selectedIdx).id;
            QuizSetEntity set = new QuizSetEntity();
            String setName = editSetName.getText().toString().trim();
            set.name = setName.isEmpty() ? getString(R.string.imported_set_name, pasteId) : setName;
            set.description = getString(R.string.imported_set_description, pasteId);
            set.collectionId = collectionId;
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
