package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.util.QuizAttemptResult;

public class PracticeActivity extends AppCompatActivity {

    public static final String EXTRA_SET_ID = "setId";

    private PracticeViewModel viewModel;
    private TextView textProgress;
    private TextView textQuestion;
    private ProgressBar progressBar;
    private MaterialButton btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4;
    private MaterialButton btnNextQuestion;
    private TextView[] answerStatusTexts;
    private List<MaterialButton> answerButtons;
    private int selectedAnswerIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        if (setId == -1) {
            Toast.makeText(this, "Error: Invalid Set ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupAnswerButtons();

        viewModel = new ViewModelProvider(this).get(PracticeViewModel.class);
        setupObservers();
        viewModel.startSession(setId);
    }

    private void initializeViews() {
        textProgress = findViewById(R.id.text_practice_progress);
        textQuestion = findViewById(R.id.text_practice_question);
        progressBar = findViewById(R.id.progress_bar);
        btnAnswer1 = findViewById(R.id.btn_answer_1);
        btnAnswer2 = findViewById(R.id.btn_answer_2);
        btnAnswer3 = findViewById(R.id.btn_answer_3);
        btnAnswer4 = findViewById(R.id.btn_answer_4);
        btnNextQuestion = findViewById(R.id.btn_next_question);

        answerButtons = Arrays.asList(btnAnswer1, btnAnswer2, btnAnswer3, btnAnswer4);

        // Initialize status text array
        answerStatusTexts = new TextView[]{
            findViewById(R.id.text_answer_status_1),
            findViewById(R.id.text_answer_status_2),
            findViewById(R.id.text_answer_status_3),
            findViewById(R.id.text_answer_status_4)
        };
    }

    private void onAnswerSelected(int index) {
        selectedAnswerIndex = index;
        // Disable all buttons immediately after selection
        for (MaterialButton btn : answerButtons) {
            btn.setEnabled(false);
        }

        MaterialButton selectedButton = answerButtons.get(index);
        Quiz currentQuiz = viewModel.getCurrentQuiz();
        if (currentQuiz == null) return;

        List<Integer> correctAnswers = currentQuiz.getCorrectAnswerIndices();
        int colorCorrect = getColor(R.color.correct_answer);
        int colorIncorrect = getColor(R.color.incorrect_answer);

        // If answer is correct
        if (correctAnswers.contains(index)) {
            selectedButton.setStrokeColor(ColorStateList.valueOf(colorCorrect));
            selectedButton.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.button_stroke_width_selected));
            selectedButton.setIcon(getDrawable(R.drawable.ic_check_correct));

            // Show "Đúng" text
            answerStatusTexts[index].setText("Đúng");
            answerStatusTexts[index].setTextColor(colorCorrect);
            answerStatusTexts[index].setVisibility(View.VISIBLE);
        } else {
            // If answer is wrong
            selectedButton.setStrokeColor(ColorStateList.valueOf(colorIncorrect));
            selectedButton.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.button_stroke_width_selected));
            selectedButton.setIcon(getDrawable(R.drawable.ic_cross_incorrect));

            // Show "Sai" text
            answerStatusTexts[index].setText("Sai");
            answerStatusTexts[index].setTextColor(colorIncorrect);
            answerStatusTexts[index].setVisibility(View.VISIBLE);

            // Show the correct answer(s)
            for (Integer correctIndex : correctAnswers) {
                MaterialButton correctButton = answerButtons.get(correctIndex);
                correctButton.setStrokeColor(ColorStateList.valueOf(colorCorrect));
                correctButton.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.button_stroke_width_selected));
                correctButton.setIcon(getDrawable(R.drawable.ic_check_correct));

                // Show "Đúng" text for correct answer
                answerStatusTexts[correctIndex].setText("Đúng");
                answerStatusTexts[correctIndex].setTextColor(colorCorrect);
                answerStatusTexts[correctIndex].setVisibility(View.VISIBLE);
            }
        }

        // Show next button with animation
        btnNextQuestion.setVisibility(View.VISIBLE);
        btnNextQuestion.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));
        btnNextQuestion.setEnabled(true);

        // Update the ViewModel
        viewModel.onAnswerSelected(viewModel.getCurrentQuizId(), Collections.singletonList(index));
        viewModel.checkCurrentAnswer();
    }

    private void setupAnswerButtons() {
        for (int i = 0; i < answerButtons.size(); i++) {
            final int index = i;
            answerButtons.get(i).setOnClickListener(v -> onAnswerSelected(index));
        }

        btnNextQuestion.setOnClickListener(v -> viewModel.moveToNextQuestion());
    }

    private void resetAnswerButtons() {
        selectedAnswerIndex = -1;
        for (int i = 0; i < answerButtons.size(); i++) {
            MaterialButton btn = answerButtons.get(i);
            btn.setEnabled(true);
            btn.setStrokeColor(ColorStateList.valueOf(getColor(R.color.outline)));
            btn.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.button_stroke_width));
            btn.setIcon(null);
            answerStatusTexts[i].setVisibility(View.GONE);
        }
        btnNextQuestion.setVisibility(View.GONE);
    }

    private void setupObservers() {
        viewModel.quizItems.observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                updateQuestion(items.get(0));
            } else {
                Toast.makeText(this, "No quizzes found for practice.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        viewModel.currentQuestionPosition.observe(this, position -> {
            if (position != null) {
                List<PracticeViewModel.PracticeItem> items = viewModel.quizItems.getValue();
                if (items != null && !items.isEmpty()) {
                    updateQuestion(items.get(position));
                    resetAnswerButtons();

                    // Update progress
                    int progress = (int) (((position + 1) * 100.0f) / items.size());
                    progressBar.setProgress(progress);
                    textProgress.setText(String.format("%d/%d", position + 1, items.size()));
                }
            }
        });

        viewModel.sessionFinished.observe(this, event -> {
            if (event.getContentIfNotHandled() != null) {
                Intent intent = new Intent(this, ScoreActivity.class);
                intent.putExtra("results", (Serializable) new ArrayList<>(event.peekContent()));
                intent.putExtra("quiz_set_id", getIntent().getLongExtra(EXTRA_SET_ID, -1));
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateQuestion(PracticeViewModel.PracticeItem item) {
        textQuestion.setText(item.quiz.getQuestion());
        List<String> answers = item.quiz.getAnswers();
        for (int i = 0; i < Math.min(answers.size(), answerButtons.size()); i++) {
            MaterialButton btn = answerButtons.get(i);
            btn.setVisibility(View.VISIBLE);
            btn.setText(answers.get(i));
            answerStatusTexts[i].setVisibility(View.GONE);
        }
        // Hide unused buttons
        for (int i = answers.size(); i < answerButtons.size(); i++) {
            answerButtons.get(i).setVisibility(View.GONE);
        }
    }
}
