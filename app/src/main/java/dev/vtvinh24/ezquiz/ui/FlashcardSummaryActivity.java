package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.FlashcardResult;

public class FlashcardSummaryActivity extends AppCompatActivity {

    public static final String EXTRA_RESULTS = "flashcard_results";
    public static final String EXTRA_UNKNOWN_CARD_IDS = "unknown_card_ids";

    // Các mã kết quả
    public static final int RESULT_DONE = 100;
    public static final int RESULT_RESTART = 101;
    public static final int RESULT_STUDY_UNKNOWN = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_summary);

        TextView textSummary = findViewById(R.id.text_summary);
        Button btnDone = findViewById(R.id.btn_done);
        Button btnRestart = findViewById(R.id.btn_restart);
        Button btnStudyUnknown = findViewById(R.id.btn_study_unknown_again);

        List<FlashcardResult> results = (List<FlashcardResult>) getIntent().getSerializableExtra(EXTRA_RESULTS);

        // Danh sách để chứa ID của các thẻ chưa biết
        final ArrayList<Long> unknownCardIds = new ArrayList<>();

        if (results != null && !results.isEmpty()) {
            long knownCount = results.stream().filter(FlashcardResult::wasKnown).count();
            int totalCount = results.size();
            String summary = String.format("Bạn đã biết %d / %d thẻ!", knownCount, totalCount);
            textSummary.setText(summary);

            // Lọc ra các ID chưa biết
            unknownCardIds.addAll(
                    results.stream()
                            .filter(r -> !r.wasKnown())
                            .map(FlashcardResult::getQuizId)
                            .collect(Collectors.toList())
            );

            // Chỉ hiện nút "Học lại" nếu có thẻ chưa biết
            if (!unknownCardIds.isEmpty()) {
                btnStudyUnknown.setVisibility(View.VISIBLE);
                btnStudyUnknown.setText(String.format("Học lại %d thẻ chưa biết", unknownCardIds.size()));
            } else {
                btnStudyUnknown.setVisibility(View.GONE);
            }

        } else {
            textSummary.setText("Không có kết quả để hiển thị.");
            btnStudyUnknown.setVisibility(View.GONE);
        }

        // --- SỬA LẠI LOGIC CÁC NÚT ---

        btnDone.setOnClickListener(v -> {
            setResult(RESULT_DONE);
            finish();
        });

        btnRestart.setOnClickListener(v -> {
            setResult(RESULT_RESTART);
            finish();
        });

        btnStudyUnknown.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(EXTRA_UNKNOWN_CARD_IDS, unknownCardIds);
            setResult(RESULT_STUDY_UNKNOWN, resultIntent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_DONE);
        super.onBackPressed();
    }
}