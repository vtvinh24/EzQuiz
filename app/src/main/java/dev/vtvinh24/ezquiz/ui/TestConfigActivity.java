package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.switchmaterial.SwitchMaterial; // Import SwitchMaterial
import com.google.android.material.textfield.TextInputEditText;

import dev.vtvinh24.ezquiz.R;

public class TestConfigActivity extends AppCompatActivity {

    public static final String EXTRA_SET_ID = "quizSetId";

    private long quizSetId;
    private TestConfigViewModel viewModel;


    private SwitchMaterial switchMultipleChoice;
    private SwitchMaterial switchTrueFalse;
    private TextInputEditText editQuestionCount;
    private TextView textMaxHint;
    private Button btnStartTest;

    private int availableMcCount = 0;
    private int availableTfCount = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_config);

        quizSetId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        if (quizSetId == -1) {
            Toast.makeText(this, getString(R.string.error_invalid_quiz_set_id), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        viewModel = new ViewModelProvider(this).get(TestConfigViewModel.class);
        setupObservers();

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> updateMaxQuestionsHint();

        switchMultipleChoice.setOnCheckedChangeListener(listener);
        switchTrueFalse.setOnCheckedChangeListener(listener);

        viewModel.loadAllTestableQuizzes(quizSetId);
        btnStartTest.setOnClickListener(v -> startTest());


        findViewById(R.id.icon_close).setOnClickListener(v -> finish());
    }

    private void initViews() {

        switchMultipleChoice = findViewById(R.id.switch_multiple_choice);
        switchTrueFalse = findViewById(R.id.switch_true_false);
        editQuestionCount = findViewById(R.id.edit_question_count);
        textMaxHint = findViewById(R.id.text_max_questions_hint);
        btnStartTest = findViewById(R.id.btn_start_test);
    }

    private void setupObservers() {
        viewModel.mcCount.observe(this, count -> {
            availableMcCount = count;

            switchMultipleChoice.setText(getString(R.string.switch_label_multiple_choice, count));
            updateMaxQuestionsHint();
        });
        viewModel.tfCount.observe(this, count -> {
            availableTfCount = count;

            switchTrueFalse.setText(getString(R.string.switch_label_true_false, count));
            updateMaxQuestionsHint();
        });
    }

    private void updateMaxQuestionsHint() {
        textMaxHint.setText(getString(R.string.hint_question_count, getMaxQuestionsFromSelection()));
    }

    private int getMaxQuestionsFromSelection() {
        int totalMax = 0;
        if (switchMultipleChoice.isChecked()) {
            totalMax += availableMcCount;
        }
        if (switchTrueFalse.isChecked()) {
            totalMax += availableTfCount;
        }
        return totalMax;
    }

    private void startTest() {
        boolean isMcChecked = switchMultipleChoice.isChecked();
        boolean isTfChecked = switchTrueFalse.isChecked();

        if (!isMcChecked && !isTfChecked) {
            Toast.makeText(this, getString(R.string.toast_select_at_least_one_type), Toast.LENGTH_SHORT).show();
            return;
        }

        int maxQuestionsForSelection = getMaxQuestionsFromSelection();
        if (maxQuestionsForSelection == 0) {
            Toast.makeText(this, getString(R.string.toast_no_questions_for_selection), Toast.LENGTH_SHORT).show();
            return;
        }

        String countStr = editQuestionCount.getText().toString().trim();
        int questionCount;

        if (countStr.isEmpty()) {
            questionCount = maxQuestionsForSelection;
        } else {
            try {
                questionCount = Integer.parseInt(countStr);
                if (questionCount <= 0 || questionCount > maxQuestionsForSelection) {
                    editQuestionCount.setError(getString(R.string.error_invalid_question_count_range, maxQuestionsForSelection));
                    return;
                }
            } catch (NumberFormatException e) {
                editQuestionCount.setError(getString(R.string.error_invalid_question_count));
                return;
            }
        }

        Intent intent = new Intent(this, TestActivity.class);
        intent.putExtra(TestActivity.EXTRA_SET_ID, quizSetId);
        intent.putExtra(TestActivity.EXTRA_QUESTION_COUNT, questionCount);
        intent.putExtra("isMcChecked", isMcChecked);
        intent.putExtra("isTfChecked", isTfChecked);
        startActivity(intent);
        finish();
    }
}