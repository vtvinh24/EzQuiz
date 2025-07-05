package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;
import java.util.stream.Collectors;
import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.FlashcardResult;

public class FlashcardSummaryActivity extends AppCompatActivity {

    public static final String EXTRA_RESULTS = "flashcard_results";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard_summary); // Bạn cần tạo layout này

        TextView textSummary = findViewById(R.id.text_summary);
        Button btnDone = findViewById(R.id.btn_done);
        // Thêm các button khác nếu cần: "Học lại", "Học lại các thẻ chưa biết"

        List<FlashcardResult> results = (List<FlashcardResult>) getIntent().getSerializableExtra(EXTRA_RESULTS);

        if (results != null && !results.isEmpty()) {
            long knownCount = results.stream().filter(FlashcardResult::wasKnown).count();
            int totalCount = results.size();
            String summary = String.format("Kết quả: Bạn đã biết %d / %d thẻ!", knownCount, totalCount);
            textSummary.setText(summary);
        } else {
            textSummary.setText("Không có kết quả để hiển thị.");
        }

        btnDone.setOnClickListener(v -> finish());
    }
}