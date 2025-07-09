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

    // Thay CheckBox bằng SwitchMaterial
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
            Toast.makeText(this, "Lỗi: ID bộ câu hỏi không hợp lệ.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        viewModel = new ViewModelProvider(this).get(TestConfigViewModel.class);
        setupObservers();

        CompoundButton.OnCheckedChangeListener listener = (buttonView, isChecked) -> updateMaxQuestionsHint();
        // Gắn listener cho Switch
        switchMultipleChoice.setOnCheckedChangeListener(listener);
        switchTrueFalse.setOnCheckedChangeListener(listener);

        viewModel.loadAllTestableQuizzes(quizSetId);
        btnStartTest.setOnClickListener(v -> startTest());

        // (Tùy chọn) Thêm sự kiện cho nút đóng
        findViewById(R.id.icon_close).setOnClickListener(v -> finish());
    }

    private void initViews() {
        // Ánh xạ các view mới
        switchMultipleChoice = findViewById(R.id.switch_multiple_choice);
        switchTrueFalse = findViewById(R.id.switch_true_false);
        editQuestionCount = findViewById(R.id.edit_question_count);
        textMaxHint = findViewById(R.id.text_max_questions_hint);
        btnStartTest = findViewById(R.id.btn_start_test);
    }

    private void setupObservers() {
        viewModel.mcCount.observe(this, count -> {
            availableMcCount = count;
            // Cập nhật text cho switch để hiển thị số câu hỏi có sẵn
            switchMultipleChoice.setText(String.format("Trắc nghiệm (%d)", count));
            updateMaxQuestionsHint();
        });
        viewModel.tfCount.observe(this, count -> {
            availableTfCount = count;
            // Cập nhật text cho switch để hiển thị số câu hỏi có sẵn
            switchTrueFalse.setText(String.format("Đúng/Sai (%d)", count));
            updateMaxQuestionsHint();
        });
    }

    private void updateMaxQuestionsHint() {
        textMaxHint.setText("Câu hỏi (tối đa: " + getMaxQuestionsFromSelection() + ")");
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
            Toast.makeText(this, "Vui lòng chọn ít nhất một loại câu hỏi.", Toast.LENGTH_SHORT).show();
            return;
        }

        int maxQuestionsForSelection = getMaxQuestionsFromSelection();
        if (maxQuestionsForSelection == 0) {
            Toast.makeText(this, "Không có câu hỏi nào thuộc (các) loại đã chọn.", Toast.LENGTH_SHORT).show();
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
                    editQuestionCount.setError("Số câu hỏi không hợp lệ (1 - " + maxQuestionsForSelection + ")");
                    return;
                }
            } catch (NumberFormatException e) {
                editQuestionCount.setError("Số câu hỏi không hợp lệ.");
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