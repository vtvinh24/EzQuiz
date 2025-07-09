package dev.vtvinh24.ezquiz.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.Quiz;

public class TestActivity extends AppCompatActivity {

    public static final String EXTRA_SET_ID = "quizSetId";
    public static final String EXTRA_QUESTION_COUNT = "questionCount";

    private TestViewModel viewModel;
    private RecyclerView recyclerView;
    private TestAdapter adapter;
    private ImageButton btnCloseTest;
    private TextView textProgress;
    private MaterialButton btnSubmit;
    private ProgressDialog gradingDialog;
    private List<TestViewModel.TestQuestionItem> currentTestItems = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        int questionCount = getIntent().getIntExtra(EXTRA_QUESTION_COUNT, Integer.MAX_VALUE);
        boolean isMcChecked = getIntent().getBooleanExtra("isMcChecked", false);
        boolean isTfChecked = getIntent().getBooleanExtra("isTfChecked", false);

        if (setId == -1) {
            Toast.makeText(this, "Lỗi: Tham số bài kiểm tra không hợp lệ.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();

        viewModel = new ViewModelProvider(this).get(TestViewModel.class);
        adapter = new TestAdapter(viewModel);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        setupObservers();
        viewModel.startTest(setId, questionCount, isMcChecked, isTfChecked);
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_test_questions);
        btnCloseTest = findViewById(R.id.btn_close_test);
        textProgress = findViewById(R.id.text_test_progress);
        btnSubmit = findViewById(R.id.btn_submit_test);
        gradingDialog = new ProgressDialog(this);
        gradingDialog.setMessage("🔄 Đang chấm điểm…");
        gradingDialog.setCancelable(false);
    }

    private void setupListeners() {
        btnSubmit.setOnClickListener(v -> viewModel.submitTest());
        btnCloseTest.setOnClickListener(v -> showCloseConfirmationDialog());
    }

    private void setupObservers() {
        viewModel.questions.observe(this, testQuestionItems -> {
            if (testQuestionItems != null) {
                if (testQuestionItems.isEmpty()) {
                    Toast.makeText(this, "Không tìm thấy câu hỏi phù hợp cho (các) chế độ đã chọn.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    this.currentTestItems = testQuestionItems;
                    adapter.submitList(testQuestionItems);
                    updateProgress();
                }
            }
        });

        // Observer này sẽ được kích hoạt mỗi khi một item được cập nhật
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                updateProgress();
            }
        });

        viewModel.isGrading.observe(this, isGrading -> {
            if (isGrading) {
                gradingDialog.show();
            } else if (gradingDialog.isShowing()) {
                gradingDialog.dismiss();
            }
        });

        viewModel.unansweredWarning.observe(this, event -> {
            if (event.getContentIfNotHandled() != null) {
                showUnansweredWarningDialog();
            }
        });

        viewModel.sessionFinished.observe(this, event -> {
            TestViewModel.TestResult result = event.getContentIfNotHandled();
            if (result != null) {
                navigateToResultScreen(result);
            }
        });
    }

    private void updateProgress() {
        if (currentTestItems.isEmpty()) {
            return;
        }
        long answeredCount = currentTestItems.stream().filter(item -> !item.userAnswerIndices.isEmpty()).count();
        int totalCount = currentTestItems.size();
        textProgress.setText(answeredCount + " / " + totalCount);
    }

    private void showUnansweredWarningDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Chưa hoàn thành")
                .setMessage("Bạn chưa làm hết tất cả các câu hỏi. Bạn có chắc chắn muốn nộp bài không?")
                .setPositiveButton("Nộp bài", (dialog, which) -> viewModel.forceSubmitTest())
                .setNegativeButton("Làm tiếp", null)
                .show();
    }

    private void showCloseConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Thoát bài kiểm tra?")
                .setMessage("Tiến trình của bạn sẽ không được lưu lại. Bạn có chắc chắn muốn thoát?")
                .setPositiveButton("Thoát", (dialog, which) -> finish())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void navigateToResultScreen(TestViewModel.TestResult result) {
        long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        Intent intent = new Intent(this, TestResultActivity.class);
        intent.putExtra("test_result", result);
        intent.putExtra("quiz_set_id", setId);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        showCloseConfirmationDialog();
    }
}

