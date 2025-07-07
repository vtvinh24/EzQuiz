package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.util.QuizAttemptResult;

public class ScoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        TextView textScore = findViewById(R.id.text_score);
        TextView textScorePercentage = findViewById(R.id.text_score_percentage);
        TextView textCorrectCount = findViewById(R.id.text_correct_count);
        TextView textIncorrectCount = findViewById(R.id.text_incorrect_count);
        MaterialButton btnTryAgain = findViewById(R.id.btn_try_again);
        MaterialButton btnBackToSets = findViewById(R.id.btn_back_to_sets);

        List<QuizAttemptResult> results = (List<QuizAttemptResult>) getIntent().getSerializableExtra("results");

        if (results != null && !results.isEmpty()) {
            int total = results.size();
            long correctCount = results.stream().filter(r -> r.isCorrect).count();
            long incorrectCount = total - correctCount;
            int percentage = (int) (((double) correctCount / total) * 100);

            // Set main score
            textScore.setText(String.format("%d/%d", correctCount, total));
            textScorePercentage.setText(String.format("%d%% Chính xác", percentage));

            // Set statistics
            textCorrectCount.setText(String.valueOf(correctCount));
            textIncorrectCount.setText(String.valueOf(incorrectCount));
        }

        btnTryAgain.setOnClickListener(v -> {
            // Get the quiz set ID from previous activity
            long setId = getIntent().getLongExtra("quiz_set_id", -1);
            if (setId != -1) {
                Intent intent = new Intent(this, PracticeActivity.class);
                intent.putExtra(PracticeActivity.EXTRA_SET_ID, setId);
                startActivity(intent);
            }
            finish();
        });

        btnBackToSets.setOnClickListener(v -> finish());
    }
}