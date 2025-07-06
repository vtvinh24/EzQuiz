package dev.vtvinh24.ezquiz.ui;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.Quiz;

public class PracticeQuestionFragment extends Fragment {

    private PracticeViewModel viewModel;
    private Quiz quiz;
    private long quizId;
    private LinearLayout answersContainer;
    private List<CompoundButton> optionsViews; // List để giữ các RadioButton/CheckBox

    public static PracticeQuestionFragment newInstance(long quizId, Quiz quiz) {
        PracticeQuestionFragment fragment = new PracticeQuestionFragment();
        Bundle args = new Bundle();
        args.putLong("quiz_id", quizId);
        args.putSerializable("quiz_data", quiz);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(PracticeViewModel.class);
        if (getArguments() != null) {
            quizId = getArguments().getLong("quiz_id");
            quiz = (Quiz) getArguments().getSerializable("quiz_data");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_practice_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textQuestion = view.findViewById(R.id.text_practice_question);
        answersContainer = view.findViewById(R.id.practice_answers_container);
        optionsViews = new ArrayList<>();

        textQuestion.setText(quiz.getQuestion());
        setupAnswerOptions();

        viewModel.isAnswerChecked.observe(getViewLifecycleOwner(), isChecked -> {
            if(isChecked){
                showResults();
            }
        });
    }

    private void setupAnswerOptions() {
        answersContainer.removeAllViews();
        optionsViews.clear();
        List<String> answers = quiz.getAnswers();

        if (quiz.getType() == Quiz.Type.SINGLE_CHOICE) {
            RadioGroup radioGroup = new RadioGroup(getContext());
            for (int i = 0; i < answers.size(); i++) {
                RadioButton rb = new RadioButton(getContext());
                rb.setText(answers.get(i));
                rb.setId(i);
                rb.setTextSize(18);
                rb.setPadding(8, 16, 8, 16);
                optionsViews.add(rb);
                radioGroup.addView(rb);
            }
            radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                viewModel.onAnswerSelected(quizId, Collections.singletonList(checkedId));
            });
            answersContainer.addView(radioGroup);
        } else { // MULTIPLE_CHOICE
            for (int i = 0; i < answers.size(); i++) {
                CheckBox cb = new CheckBox(getContext());
                cb.setText(answers.get(i));
                cb.setId(i);
                cb.setTextSize(18);
                cb.setPadding(8, 16, 8, 16);
                cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    updateViewModelWithMultipleChoices();
                });
                optionsViews.add(cb);
                answersContainer.addView(cb);
            }
        }
    }

    private void updateViewModelWithMultipleChoices() {
        ArrayList<Integer> selectedIndices = new ArrayList<>();
        for(CompoundButton cb : optionsViews){
            if(cb.isChecked()){
                selectedIndices.add(cb.getId());
            }
        }
        viewModel.onAnswerSelected(quizId, selectedIndices);
    }

    private void showResults() {
        List<Integer> correctAnswers = quiz.getCorrectAnswerIndices();
        for (CompoundButton option : optionsViews) {
            int optionId = option.getId();
            option.setEnabled(false); // Vô hiệu hóa lựa chọn

            // Tô màu xanh cho đáp án đúng
            if (correctAnswers.contains(optionId)) {
                option.setTextColor(ContextCompat.getColor(getContext(), R.color.correct_answer));
            }

            // Nếu người dùng chọn sai, tô màu đỏ
            if (option.isChecked() && !correctAnswers.contains(optionId)) {
                option.setTextColor(ContextCompat.getColor(getContext(), R.color.incorrect_answer));
            }
        }
    }
}