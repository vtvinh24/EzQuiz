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

        // === Ánh xạ các View từ layout ===
        ImageView icon = findViewById(R.id.icon_summary);
        TextView message = findViewById(R.id.text_result_message);
        TextView scoreText = findViewById(R.id.text_result_score);
        TextView textCorrectCount = findViewById(R.id.text_correct_count);
        TextView textIncorrectCount = findViewById(R.id.text_incorrect_count);
        MaterialButton btnShare = findViewById(R.id.btn_share_result);
        MaterialButton btnPracticeAgain = findViewById(R.id.btn_practice_again);
        MaterialButton btnFinish = findViewById(R.id.btn_finish);

        // === Lấy dữ liệu kết quả từ Intent ===
        TestViewModel.TestResult result = (TestViewModel.TestResult) getIntent().getSerializableExtra("test_result");
        long quizSetId = getIntent().getLongExtra("quiz_set_id", -1); // Lấy lại ID để dùng cho nút "Làm lại"

        if (result == null) {
            Toast.makeText(this, "Không có dữ liệu kết quả.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // === Tính toán và hiển thị điểm số ===
        double score = (result.totalCount > 0) ? (double) result.correctCount / result.totalCount : 0.0;
        int percentage = (int) (score * 100);

        scoreText.setText(String.format("%d/%d (%d%%)", result.correctCount, result.totalCount, percentage));
        textCorrectCount.setText(String.valueOf(result.correctCount));
        textIncorrectCount.setText(String.valueOf(result.totalCount - result.correctCount));

        // === Cập nhật UI dựa trên điểm số ===
        if (score >= 0.9) {
            icon.setImageResource(R.drawable.ic_trophy);
            message.setText("Bạn thật xuất sắc! 🎉");
            btnShare.setVisibility(View.VISIBLE);
            btnPracticeAgain.setVisibility(View.GONE);
        } else if (score >= 0.5) { // Thay đổi ngưỡng điểm nếu muốn
            icon.setImageResource(R.drawable.ic_check_circle);
            message.setText("Chúc mừng! Bạn đã hoàn thành bài kiểm tra.");
            btnShare.setVisibility(View.GONE);
            btnPracticeAgain.setVisibility(View.VISIBLE);
        } else {
            icon.setImageResource(R.drawable.ic_sad);
            message.setText("Bạn cần luyện tập thêm nhiều hơn nữa 😢");
            btnShare.setVisibility(View.GONE);
            btnPracticeAgain.setVisibility(View.VISIBLE);
        }

        // === Xử lý sự kiện cho các nút ===
        btnShare.setOnClickListener(v -> {
            String shareContent = String.format("Tôi vừa hoàn thành bài kiểm tra trên EzQuiz với số điểm %d/%d! 💪",
                    result.correctCount, result.totalCount);
            ShareManager.shareText(this, "Kết quả EzQuiz", shareContent);
        });

        btnPracticeAgain.setOnClickListener(v -> {
            // Quay lại màn hình cấu hình để làm lại bài kiểm tra
            if (quizSetId != -1) {
                // TODO: Chuyển sang màn hình TestConfigActivity
                // Intent intent = new Intent(TestResultActivity.this, TestConfigActivity.class);
                // intent.putExtra("quizSetId", quizSetId);
                // startActivity(intent);
                // finish();
                Toast.makeText(this, "Tính năng 'Làm lại' sẽ được thêm vào sau!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Lỗi: Không tìm thấy ID để làm lại.", Toast.LENGTH_SHORT).show();
            }
        });

        btnFinish.setOnClickListener(v -> {
            // Đơn giản là đóng màn hình kết quả và quay về màn hình trước đó
            finish();
        });
    }
}
