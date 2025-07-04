package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import java.io.Serializable;
import java.util.List;
import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.FlashcardResult;

public class FlashcardActivity extends AppCompatActivity {

    public static final String EXTRA_SET_ID = "quiz_set_id";

    private FlashcardViewModel viewModel;
    private ViewPager2 viewPager;
    private FlashcardAdapter adapter;
    private TextView progressTextView;
    private Button knowButton, dontKnowButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashcard); // Bạn cần tạo layout này

        long quizSetId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        if (quizSetId == -1) {
            // Không có ID, không thể tiếp tục
            finish();
            return;
        }

        viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);

        setupUI();
        observeViewModel();

        viewModel.startSession(quizSetId);
    }

    private void setupUI() {
        viewPager = findViewById(R.id.view_pager_flashcard);
        progressTextView = findViewById(R.id.text_flashcard_progress);
        knowButton = findViewById(R.id.btn_know);
        dontKnowButton = findViewById(R.id.btn_dont_know);

        adapter = new FlashcardAdapter(this);
        viewPager.setAdapter(adapter);

        knowButton.setOnClickListener(v -> viewModel.markAsKnown());
        dontKnowButton.setOnClickListener(v -> viewModel.markAsUnknown());

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                viewModel.onUserSwiped(position);
            }
        });
    }

    private void observeViewModel() {
        viewModel.flashcards.observe(this, quizzes -> {
            adapter.submitList(quizzes);
        });

        viewModel.currentCardPosition.observe(this, position -> {
            viewPager.setCurrentItem(position, true);
        });

        viewModel.sessionProgressText.observe(this, text -> {
            progressTextView.setText(text);
        });

        viewModel.sessionFinished.observe(this, event -> {
            List<FlashcardResult> results = event.getContentIfNotHandled();
            if (results != null) {
                Intent intent = new Intent(this, FlashcardSummaryActivity.class);
                intent.putExtra(FlashcardSummaryActivity.EXTRA_RESULTS, (Serializable) results);
                startActivity(intent);
                finish(); // Kết thúc màn hình học hiện tại
            }
        });
    }
}