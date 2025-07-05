package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.io.Serializable;
import java.util.ArrayList;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.util.QuizAttemptResult;

public class PracticeActivity extends AppCompatActivity {

    public static final String EXTRA_SET_ID = "setId";

    private PracticeViewModel viewModel;
    private ViewPager2 viewPager;
    private PracticeAdapter adapter;
    private Button btnCheckAnswer, btnNextQuestion;
    private TextView textProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice); // Dùng layout mới

        long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        if (setId == -1) {
            Toast.makeText(this, "Error: Invalid Set ID.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        viewPager = findViewById(R.id.practice_view_pager);
        btnCheckAnswer = findViewById(R.id.btn_check_answer);
        btnNextQuestion = findViewById(R.id.btn_next_question);
        textProgress = findViewById(R.id.text_practice_progress);

        viewModel = new ViewModelProvider(this).get(PracticeViewModel.class);
        adapter = new PracticeAdapter(this, viewModel); // Truyền ViewModel vào Adapter
        viewPager.setAdapter(adapter);
        viewPager.setUserInputEnabled(false); // Không cho người dùng vuốt

        setupListeners();
        setupObservers();

        viewModel.startSession(setId);
    }

    private void setupListeners() {
        btnCheckAnswer.setOnClickListener(v -> viewModel.checkCurrentAnswer());
        btnNextQuestion.setOnClickListener(v -> viewModel.moveToNextQuestion());
    }

    private void setupObservers() {
        viewModel.quizItems.observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                adapter.submitList(items);
            } else {
                Toast.makeText(this, "No quizzes found for practice.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        viewModel.currentQuestionPosition.observe(this, position -> {
            if (position != null) {
                viewPager.setCurrentItem(position, true);
                btnCheckAnswer.setEnabled(true);
                btnNextQuestion.setVisibility(View.GONE);
                btnCheckAnswer.setVisibility(View.VISIBLE);
            }
        });

        viewModel.sessionProgressText.observe(this, progressText -> {
            textProgress.setText(progressText);
        });

        // Khi câu trả lời được kiểm tra, ẩn nút Check và hiện nút Next
        viewModel.isAnswerChecked.observe(this, isChecked -> {
            if(isChecked){
                btnCheckAnswer.setVisibility(View.GONE);
                btnNextQuestion.setVisibility(View.VISIBLE);
                btnNextQuestion.setEnabled(true);
            }
        });

        viewModel.sessionFinished.observe(this, event -> {
            if (event.getContentIfNotHandled() != null) {
                Intent intent = new Intent(this, ScoreActivity.class);
                intent.putExtra("results", (Serializable) new ArrayList<>(event.peekContent()));
                startActivity(intent);
                finish();
            }
        });
    }
}