package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import java.io.Serializable;
import java.util.ArrayList;
import dev.vtvinh24.ezquiz.R;

public class PracticeActivity extends AppCompatActivity {

    public static final String EXTRA_SET_ID = "setId";

    private PracticeViewModel viewModel;
    private PracticeAdapter practiceAdapter;

    private ViewPager2 viewPager;
    private TextView textProgress;
    private ProgressBar progressBar;
    private MaterialButton btnCheckAnswer, btnNextQuestion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        if (setId == -1) {
            Toast.makeText(this, "Error: Invalid Set ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupViewModel();
        setupViewPager();
        setupClickListeners();
        setupObservers();

        viewModel.startSession(setId);
    }

    private void initializeViews() {
        textProgress = findViewById(R.id.text_practice_progress);
        progressBar = findViewById(R.id.progress_bar);
        viewPager = findViewById(R.id.view_pager_practice);
        btnCheckAnswer = findViewById(R.id.btn_check_answer);
        btnNextQuestion = findViewById(R.id.btn_next_question);

        // Ngăn người dùng tự vuốt để chuyển câu hỏi
        viewPager.setUserInputEnabled(false);
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(PracticeViewModel.class);
    }

    private void setupViewPager() {
        practiceAdapter = new PracticeAdapter(this);
        viewPager.setAdapter(practiceAdapter);
    }

    private void setupClickListeners() {
        btnCheckAnswer.setOnClickListener(v -> viewModel.checkCurrentAnswer());
        btnNextQuestion.setOnClickListener(v -> viewModel.moveToNextQuestion());
    }

    private void setupObservers() {
        viewModel.quizItems.observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                practiceAdapter.submitList(items);
                updateProgress();
            } else {
                Toast.makeText(this, "No quizzes available for practice.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        viewModel.currentQuestionPosition.observe(this, position -> {
            if (position != null) {
                viewPager.setCurrentItem(position, false); // Chuyển trang không cần hiệu ứng
                updateProgress();
                btnCheckAnswer.setVisibility(View.VISIBLE);
                btnNextQuestion.setVisibility(View.GONE);
            }
        });

        viewModel.isAnswerChecked.observe(this, isChecked -> {
            if (isChecked) {
                btnCheckAnswer.setVisibility(View.GONE);
                btnNextQuestion.setVisibility(View.VISIBLE);
            }
        });

        viewModel.canCheckAnswer.observe(this, canCheck -> {
            btnCheckAnswer.setEnabled(canCheck);
        });

        viewModel.sessionFinished.observe(this, event -> {
            if (event.getContentIfNotHandled() != null) {
                Intent intent = new Intent(this, ScoreActivity.class); // Giả sử có ScoreActivity
                intent.putExtra("results", (Serializable) new ArrayList<>(event.peekContent()));
                intent.putExtra("quiz_set_id", getIntent().getLongExtra(EXTRA_SET_ID, -1));
                startActivity(intent);
                finish();
            }
        });
    }

    private void updateProgress() {
        Integer position = viewModel.currentQuestionPosition.getValue();
        int total = practiceAdapter.getItemCount();

        if (position != null && total > 0) {
            int progressValue = (int) (((position + 1) * 100.0f) / total);
            progressBar.setProgress(progressValue);
            textProgress.setText(String.format("%d/%d", position + 1, total));
        }
    }
}