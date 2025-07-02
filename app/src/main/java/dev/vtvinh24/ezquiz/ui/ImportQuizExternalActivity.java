package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import dev.vtvinh24.ezquiz.network.QuizImporter;

public class ImportQuizExternalActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_import_quiz_external);

    EditText editPasteId = findViewById(R.id.edit_paste_id);
    RadioGroup radioImportType = findViewById(R.id.radio_import_type);
    RadioButton radioFlashcard = findViewById(R.id.radio_flashcard);
    RadioButton radioQuiz = findViewById(R.id.radio_quiz);
    Button btnImportPaste = findViewById(R.id.btn_import_paste);
    TextView textStatus = findViewById(R.id.text_import_status);

    btnImportPaste.setOnClickListener(v -> {
      String pasteId = editPasteId.getText().toString().trim();
      if (pasteId.isEmpty()) {
        textStatus.setText(getString(R.string.prompt_paste_id));
        return;
      }
      QuizImporter importer = new QuizImporter();
      Handler mainHandler = new Handler(Looper.getMainLooper());
      Executors.newSingleThreadExecutor().execute(() -> {
        List<Quiz> quizzes;
        if (radioFlashcard.isChecked()) {
          quizzes = importer.importFlashcards(pasteId);
        } else {
          quizzes = importer.importQuizzes(pasteId);
        }
        mainHandler.post(() -> {
          if (quizzes.isEmpty()) {
            textStatus.setText(getString(R.string.error_no_items_imported));
            return;
          }
          AppDatabase db = AppDatabaseProvider.getDatabase(this);
          QuizSetEntity set = new QuizSetEntity();
          set.name = getString(R.string.imported_set_name, pasteId);
          set.description = getString(R.string.imported_set_description, pasteId);
          set.collectionId = 0;
          set.createdAt = System.currentTimeMillis();
          set.updatedAt = System.currentTimeMillis();
          long setId = db.quizSetDao().insert(set);
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
  }
}
