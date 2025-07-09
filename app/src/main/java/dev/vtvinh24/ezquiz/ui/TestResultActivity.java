package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.util.ShareManager;

public class TestResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_result);


        ImageView icon = findViewById(R.id.icon_summary);
        TextView message = findViewById(R.id.text_result_message);
        TextView scoreText = findViewById(R.id.text_result_score);
        TextView textCorrectCount = findViewById(R.id.text_correct_count);
        TextView textIncorrectCount = findViewById(R.id.text_incorrect_count);
        MaterialButton btnShare = findViewById(R.id.btn_share_result);
        MaterialButton btnPracticeAgain = findViewById(R.id.btn_practice_again);
        MaterialButton btnFinish = findViewById(R.id.btn_finish);


        TestViewModel.TestResult result = (TestViewModel.TestResult) getIntent().getSerializableExtra("test_result");
        long quizSetId = getIntent().getLongExtra("quiz_set_id", -1); //

        if (result == null) {
            Toast.makeText(this, getString(R.string.toast_no_result_data), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        double score = (result.totalCount > 0) ? (double) result.correctCount / result.totalCount : 0.0;
        int percentage = (int) (score * 100);

        scoreText.setText(getString(R.string.test_result_score_format, result.correctCount, result.totalCount, percentage));
        textCorrectCount.setText(String.valueOf(result.correctCount));
        textIncorrectCount.setText(String.valueOf(result.totalCount - result.correctCount));


        if (score >= 0.9) {
            icon.setImageResource(R.drawable.ic_trophy);
            message.setText(R.string.test_result_message_excellent);
            btnShare.setVisibility(View.VISIBLE);
            btnPracticeAgain.setVisibility(View.GONE);
        } else if (score >= 0.5) {
            icon.setImageResource(R.drawable.ic_check_circle);
            message.setText(R.string.test_result_message_good);
            btnShare.setVisibility(View.GONE);
            btnPracticeAgain.setVisibility(View.VISIBLE);
        } else {
            icon.setImageResource(R.drawable.ic_sad);
            message.setText(R.string.test_result_message_needs_practice);
            btnShare.setVisibility(View.GONE);
            btnPracticeAgain.setVisibility(View.VISIBLE);
        }


        btnShare.setOnClickListener(v -> {
            String shareContent = getString(R.string.share_result_content,
                    result.correctCount, result.totalCount);
            ShareManager.shareText(this, getString(R.string.share_result_title), shareContent);
        });

        btnPracticeAgain.setOnClickListener(v -> {

            if (quizSetId != -1) {

                // Intent intent = new Intent(TestResultActivity.this, TestConfigActivity.class);
                // intent.putExtra("quizSetId", quizSetId);
                // startActivity(intent);
                // finish();
                Toast.makeText(this, getString(R.string.toast_practice_again_coming_soon), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.error_cannot_find_id_for_retry), Toast.LENGTH_SHORT).show();
            }
        });

        btnFinish.setOnClickListener(v -> {

            finish();
        });
    }
}