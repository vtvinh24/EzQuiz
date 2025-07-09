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
            Toast.makeText(this, getString(R.string.error_invalid_test_params), Toast.LENGTH_SHORT).show();
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
        gradingDialog.setMessage(getString(R.string.dialog_grading_message));
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
                    Toast.makeText(this, getString(R.string.toast_no_questions_found), Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    this.currentTestItems = testQuestionItems;
                    adapter.submitList(testQuestionItems);
                    updateProgress();
                }
            }
        });


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
        textProgress.setText(getString(R.string.test_progress_format, (int) answeredCount, totalCount));
    }

    private void showUnansweredWarningDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_unanswered_title)
                .setMessage(R.string.dialog_unanswered_message)
                .setPositiveButton(R.string.btn_submit_anyway, (dialog, which) -> viewModel.forceSubmitTest())
                .setNegativeButton(R.string.btn_continue_test, null)
                .show();
    }

    private void showCloseConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_close_test_title)
                .setMessage(R.string.dialog_close_test_message)
                .setPositiveButton(R.string.btn_exit, (dialog, which) -> finish())
                .setNegativeButton(R.string.btn_cancel, null)
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