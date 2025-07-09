package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox; // Thay đổi import
import android.widget.LinearLayout; // Thay đổi import
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.vtvinh24.ezquiz.R;

public class TestQuestionFragment extends Fragment {

    private static final String ARG_QUESTION_ITEM = "question_item";
    private TestViewModel viewModel;
    private TestViewModel.TestQuestionItem questionItem;
    private LinearLayout containerAnswers; // Thay RadioGroup bằng LinearLayout

    public static TestQuestionFragment newInstance(TestViewModel.TestQuestionItem item) {
        TestQuestionFragment fragment = new TestQuestionFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_QUESTION_ITEM, item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(TestViewModel.class);
        if (getArguments() != null) {
            questionItem = (TestViewModel.TestQuestionItem) getArguments().getSerializable(ARG_QUESTION_ITEM);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Cần một layout mới hoặc chỉnh sửa layout fragment_test_question
        // Giả sử layout đã được sửa để dùng LinearLayout với id là "container_answers"
        return inflater.inflate(R.layout.fragment_test_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (questionItem == null) return;

        TextView textQuestion = view.findViewById(R.id.text_question);
        // Thay thế RadioGroup bằng LinearLayout
        containerAnswers = view.findViewById(R.id.container_answers); // Đảm bảo layout có id này

        textQuestion.setText(questionItem.quiz.getQuestion());

        containerAnswers.removeAllViews();
        for (int i = 0; i < questionItem.quiz.getAnswers().size(); i++) {
            // Dùng CheckBox thay vì RadioButton
            CheckBox cb = new CheckBox(getContext());
            cb.setText(questionItem.quiz.getAnswers().get(i));
            cb.setId(i); // Dùng index làm ID
            containerAnswers.addView(cb);

            // Kiểm tra xem đáp án này có trong danh sách đã chọn không
            if (questionItem.userAnswerIndices.contains(i)) {
                cb.setChecked(true);
            }

            // Xử lý sự kiện khi check/uncheck
            cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                updateAnswerFromCheckboxes();
            });
        }
    }

    private void updateAnswerFromCheckboxes() {
        List<Integer> selectedIndices = new ArrayList<>();
        for (int i = 0; i < containerAnswers.getChildCount(); i++) {
            View child = containerAnswers.getChildAt(i);
            if (child instanceof CheckBox) {
                CheckBox cb = (CheckBox) child;
                if (cb.isChecked()) {
                    selectedIndices.add(cb.getId());
                }
            }
        }
        // Gửi danh sách các index đã được chọn lên ViewModel
        viewModel.onAnswerSelected(questionItem.id, selectedIndices);
    }
}

