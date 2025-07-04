package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.util.QuizAttemptResult;

public class ScoreActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_score);

        TextView textScore = findViewById(R.id.text_score_value);
        TextView textScoreDetails = findViewById(R.id.text_score_details);
        Button btnDone = findViewById(R.id.btn_score_done);

        List<QuizAttemptResult> results = (List<QuizAttemptResult>) getIntent().getSerializableExtra("results");

        if (results != null && !results.isEmpty()) {
            int total = results.size();
            long correctCount = results.stream().filter(r -> r.isCorrect).count();

            int percentage = (int) (((double) correctCount / total) * 100);

            textScore.setText(percentage + "%");
            textScoreDetails.setText("You answered " + correctCount + " out of " + total + " questions correctly.");
        }

        btnDone.setOnClickListener(v -> finish());
    }
}
