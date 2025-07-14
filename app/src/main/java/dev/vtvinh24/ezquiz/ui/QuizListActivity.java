package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.model.Quiz;

public class QuizListActivity extends AppCompatActivity implements QuizReviewAdapter.OnItemLongClickListener {
  private RecyclerView recyclerView;
  private QuizReviewAdapter adapter;
  private List<QuizEntity> quizzes;
  private AppDatabase db;
  private long quizSetId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_quiz_list);
    recyclerView = findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    quizSetId = getIntent().getLongExtra("quizSetId", -1);
    db = AppDatabaseProvider.getDatabase(this);
    quizzes = db.quizDao().getByQuizSetId(quizSetId);
    adapter = new QuizReviewAdapter(quizzes);
    adapter.setOnItemLongClickListener(this);
    recyclerView.setAdapter(adapter);
    FloatingActionButton fab = findViewById(R.id.fab_add_quiz);
    fab.setOnClickListener(v -> showAddQuizDialog());
    refreshQuizzes();
  }

  private void showAddQuizDialog() {
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_quiz, null);
    EditText questionInput = dialogView.findViewById(R.id.edit_quiz_question);
    Spinner typeSpinner = dialogView.findViewById(R.id.spinner_quiz_type);
    LinearLayout answersContainer = dialogView.findViewById(R.id.answers_container);
    Button btnAddAnswer = dialogView.findViewById(R.id.btn_add_answer);
    ArrayList<EditText> answerInputs = new ArrayList<>();
    ArrayList<CheckBox> correctChecks = new ArrayList<>();

    // Add first answer row by default
    addAnswerRow(answersContainer, answerInputs, correctChecks);

    btnAddAnswer.setOnClickListener(v -> addAnswerRow(answersContainer, answerInputs, correctChecks));

    typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
        updateCheckboxesForType(typeSpinner, correctChecks);
      }

      @Override
      public void onNothingSelected(android.widget.AdapterView<?> parent) {
      }
    });

    new AlertDialog.Builder(this).setTitle(R.string.dialog_add_quiz_title).setView(dialogView).setPositiveButton(R.string.btn_add, (d, w) -> {
      String question = questionInput.getText().toString();
      ArrayList<String> answers = new ArrayList<>();
      ArrayList<Integer> correctIndices = new ArrayList<>();
      for (int i = 0; i < answerInputs.size(); i++) {
        String ans = answerInputs.get(i).getText().toString();
        if (!ans.isEmpty()) {
          answers.add(ans);
          if (correctChecks.get(i).isChecked()) correctIndices.add(i);
        }
      }
      Quiz.Type type = typeSpinner.getSelectedItemPosition() == 1 ? Quiz.Type.MULTIPLE_CHOICE : Quiz.Type.SINGLE_CHOICE;
      QuizEntity entity = new QuizEntity();
      entity.question = question;
      entity.answers = answers;
      entity.correctAnswerIndices = correctIndices;
      entity.type = type;
      entity.quizSetId = quizSetId;
      db.quizDao().insert(entity);
      refreshQuizzes();
    }).setNegativeButton(R.string.btn_cancel, null).show();
  }

  private void addAnswerRow(LinearLayout container, ArrayList<EditText> answerInputs, ArrayList<CheckBox> correctChecks) {
    View row = LayoutInflater.from(this).inflate(R.layout.item_answer_row, container, false);
    EditText answerInput = row.findViewById(R.id.edit_answer_text);
    CheckBox correctCheck = row.findViewById(R.id.checkbox_correct);
    answerInputs.add(answerInput);
    correctChecks.add(correctCheck);
    container.addView(row);
    updateCheckboxesForType(((View) container.getParent()).findViewById(R.id.spinner_quiz_type), correctChecks);
  }

  private void updateCheckboxesForType(Spinner typeSpinner, ArrayList<CheckBox> correctChecks) {
    boolean isMultiple = typeSpinner.getSelectedItemPosition() == 1;
    for (CheckBox cb : correctChecks) {
      cb.setOnCheckedChangeListener(null);
      cb.setChecked(false);
      cb.setEnabled(true);
    }
    if (!isMultiple) {
      for (int i = 0; i < correctChecks.size(); i++) {
        CheckBox cb = correctChecks.get(i);
        cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
          if (isChecked) {
            for (CheckBox other : correctChecks) {
              if (other != cb) other.setChecked(false);
            }
          }
        });
      }
    }
  }

  private void showEditQuizDialog(QuizEntity quiz) {
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_quiz, null);
    EditText questionInput = dialogView.findViewById(R.id.edit_quiz_question);
    Spinner typeSpinner = dialogView.findViewById(R.id.spinner_quiz_type);
    LinearLayout answersContainer = dialogView.findViewById(R.id.answers_container);
    Button btnAddAnswer = dialogView.findViewById(R.id.btn_add_answer);
    ArrayList<EditText> answerInputs = new ArrayList<>();
    ArrayList<CheckBox> correctChecks = new ArrayList<>();

    // Populate fields from quiz
    questionInput.setText(quiz.question);
    // Set type
    typeSpinner.setSelection(quiz.type == Quiz.Type.MULTIPLE_CHOICE ? 1 : 0);

    // Add answer rows for each answer
    for (int i = 0; i < quiz.answers.size(); i++) {
      addAnswerRow(answersContainer, answerInputs, correctChecks);
      answerInputs.get(i).setText(quiz.answers.get(i));
      if (quiz.correctAnswerIndices.contains(i)) correctChecks.get(i).setChecked(true);
    }
    if (quiz.answers.isEmpty()) addAnswerRow(answersContainer, answerInputs, correctChecks);

    btnAddAnswer.setOnClickListener(v -> addAnswerRow(answersContainer, answerInputs, correctChecks));

    typeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
        updateCheckboxesForType(typeSpinner, correctChecks);
      }

      @Override
      public void onNothingSelected(android.widget.AdapterView<?> parent) {
      }
    });

    new AlertDialog.Builder(this).setTitle(R.string.dialog_edit_quiz_title).setView(dialogView).setPositiveButton(R.string.btn_save, (d, w) -> {
      quiz.question = questionInput.getText().toString();
      ArrayList<String> answers = new ArrayList<>();
      ArrayList<Integer> correctIndices = new ArrayList<>();
      for (int i = 0; i < answerInputs.size(); i++) {
        String ans = answerInputs.get(i).getText().toString();
        if (!ans.isEmpty()) {
          answers.add(ans);
          if (correctChecks.get(i).isChecked()) correctIndices.add(i);
        }
      }
      quiz.answers = answers;
      quiz.correctAnswerIndices = correctIndices;
      quiz.type = typeSpinner.getSelectedItemPosition() == 1 ? Quiz.Type.MULTIPLE_CHOICE : Quiz.Type.SINGLE_CHOICE;
      db.quizDao().update(quiz);
      refreshQuizzes();
    }).setNegativeButton(R.string.btn_cancel, null).show();
  }

  private void refreshQuizzes() {
    quizzes.clear();
    quizzes.addAll(db.quizDao().getByQuizSetId(quizSetId));
    adapter.notifyDataSetChanged();
    View emptyView = findViewById(R.id.empty_view);
    if (quizzes.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
      recyclerView.setVisibility(View.GONE);
    } else {
      emptyView.setVisibility(View.GONE);
      recyclerView.setVisibility(View.VISIBLE);
    }
  }

  public void onItemLongClick(QuizEntity quiz) {
    String[] options = {"Edit", "Delete"};
    new AlertDialog.Builder(this).setTitle(quiz.question).setItems(options, (dialog, which) -> {
      if (which == 0) showEditQuizDialog(quiz);
      else if (which == 1) {
        db.quizDao().delete(quiz);
        refreshQuizzes();
        Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
      }
    }).show();
  }
}
