package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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

  public static final int RESULT_DONE = 100;
  public static final int RESULT_RESTART = 101;
  public static final int RESULT_STUDY_UNKNOWN = 102;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_flashcard_summary);

    TextView textSummary = findViewById(R.id.text_summary);
    TextView textKnownCount = findViewById(R.id.text_known_count);
    TextView textUnknownCount = findViewById(R.id.text_unknown_count);
    Button btnDone = findViewById(R.id.btn_done);
    Button btnRestart = findViewById(R.id.btn_restart);
    Button btnStudyUnknown = findViewById(R.id.btn_study_unknown_again);

    List<FlashcardResult> results = (List<FlashcardResult>) getIntent().getSerializableExtra(EXTRA_RESULTS);
    ArrayList<Long> unknownCardIds = new ArrayList<>();

    if (results != null && !results.isEmpty()) {
      long knownCount = results.stream().filter(FlashcardResult::wasKnown).count();
      long unknownCount = results.size() - knownCount;
      int totalCount = results.size();

      String summary = getString(R.string.flashcard_summary_known_format, knownCount, totalCount);
      textSummary.setText(summary);

      textKnownCount.setText(String.valueOf(knownCount));
      textUnknownCount.setText(String.valueOf(unknownCount));

      unknownCardIds.addAll(
              results.stream()
                      .filter(r -> !r.wasKnown())
                      .map(FlashcardResult::getQuizId)
                      .collect(Collectors.toList())
      );

      if (!unknownCardIds.isEmpty()) {
        btnStudyUnknown.setVisibility(View.VISIBLE);
        btnStudyUnknown.setText(getString(R.string.flashcard_summary_study_unknown_format, unknownCardIds.size()));
      } else {
        btnStudyUnknown.setVisibility(View.GONE);
      }

    } else {
      textSummary.setText(R.string.flashcard_summary_no_results);
      textKnownCount.setText("0");
      textUnknownCount.setText("0");
      btnStudyUnknown.setVisibility(View.GONE);
    }

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