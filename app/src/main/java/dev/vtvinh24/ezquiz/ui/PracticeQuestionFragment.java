package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.card.MaterialCardView;
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
    private List<MaterialCardView> optionsViews;
    private TextView textAnswerInstruction;

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
        optionsViews = new ArrayList<>();
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
        textAnswerInstruction = view.findViewById(R.id.text_answer_instruction);

        textQuestion.setText(quiz.getQuestion());

        // === CẬP NHẬT LOGIC ĐỂ SỬ DỤNG STRING RESOURCES ===
        if (quiz.getType() == Quiz.Type.MULTIPLE_CHOICE) {
            textAnswerInstruction.setText(R.string.prompt_select_multiple_answers);
        } else { // Mặc định cho SINGLE_CHOICE
            textAnswerInstruction.setText(R.string.prompt_select_one_answer);
        }


        setupAnswerOptions();

        viewModel.isAnswerChecked.observe(getViewLifecycleOwner(), isChecked -> {
            if (isChecked && viewModel.getCurrentPosition() == getAdapterPosition()) {
                showResults();
            }
        });
    }

    private int getAdapterPosition() {
        List<PracticeViewModel.PracticeItem> items = viewModel.quizItems.getValue();
        if (items != null) {
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).id == quizId) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void setupAnswerOptions() {
        answersContainer.removeAllViews();
        optionsViews.clear();
        List<String> answers = quiz.getAnswers();
        LayoutInflater inflater = LayoutInflater.from(getContext());

        // Debug logging
        android.util.Log.d("PracticeFragment", "Quiz question: " + quiz.getQuestion());
        android.util.Log.d("PracticeFragment", "Number of answers: " + (answers != null ? answers.size() : 0));
        if (answers != null) {
            for (int j = 0; j < answers.size(); j++) {
                android.util.Log.d("PracticeFragment", "Answer " + j + ": " + answers.get(j));
            }
        }

        if (answers == null || answers.isEmpty()) {
            android.util.Log.e("PracticeFragment", "No answers found for quiz!");
            return;
        }

        for (int i = 0; i < answers.size(); i++) {
            MaterialCardView cardView = (MaterialCardView) inflater.inflate(R.layout.item_practice_answer, answersContainer, false);
            TextView textAnswer = cardView.findViewById(R.id.text_answer_option);
            ImageView choiceIndicator = cardView.findViewById(R.id.choice_indicator);
            LinearLayout container = cardView.findViewById(R.id.choice_indicator).getParent() instanceof LinearLayout ?
                (LinearLayout) cardView.findViewById(R.id.choice_indicator).getParent() : null;

            textAnswer.setText(answers.get(i));

            // Set appropriate choice indicator based on question type
            if (quiz.getType() == Quiz.Type.MULTIPLE_CHOICE) {
                choiceIndicator.setImageResource(R.drawable.selector_checkbox_practice);
            } else {
                choiceIndicator.setImageResource(R.drawable.selector_radio_practice);
            }

            final int position = i;
            cardView.setOnClickListener(v -> handleAnswerClick(cardView, position));

            optionsViews.add(cardView);
            answersContainer.addView(cardView);

            android.util.Log.d("PracticeFragment", "Added answer view " + i + " to container");
        }

        android.util.Log.d("PracticeFragment", "Total views added: " + optionsViews.size());
        android.util.Log.d("PracticeFragment", "Container child count: " + answersContainer.getChildCount());
    }

    private void handleAnswerClick(MaterialCardView clickedCard, int position) {
        ImageView choiceIndicator = clickedCard.findViewById(R.id.choice_indicator);
        LinearLayout container = (LinearLayout) choiceIndicator.getParent();

        if (quiz.getType() == Quiz.Type.SINGLE_CHOICE) {
            // Reset all cards for single choice
            for (MaterialCardView card : optionsViews) {
                card.setChecked(false);
                ImageView indicator = card.findViewById(R.id.choice_indicator);
                LinearLayout cardContainer = (LinearLayout) indicator.getParent();
                indicator.setSelected(false);
                cardContainer.setSelected(false);
            }

            // Select clicked card
            clickedCard.setChecked(true);
            choiceIndicator.setSelected(true);
            container.setSelected(true);
            viewModel.onAnswerSelected(quizId, Collections.singletonList(position));
        } else { // MULTIPLE_CHOICE
            // Toggle selection for multiple choice
            boolean isCurrentlySelected = clickedCard.isChecked();
            clickedCard.setChecked(!isCurrentlySelected);
            choiceIndicator.setSelected(!isCurrentlySelected);
            container.setSelected(!isCurrentlySelected);
            updateViewModelWithMultipleChoices();
        }
    }

    private void updateViewModelWithMultipleChoices() {
        ArrayList<Integer> selectedIndices = new ArrayList<>();
        for (int i = 0; i < optionsViews.size(); i++) {
            if (optionsViews.get(i).isChecked()) {
                selectedIndices.add(i);
            }
        }
        viewModel.onAnswerSelected(quizId, selectedIndices);
    }

    private void showResults() {
        List<Integer> correctAnswers = quiz.getCorrectAnswerIndices();
        int colorCorrect = ContextCompat.getColor(requireContext(), R.color.correct_answer);
        int colorIncorrect = ContextCompat.getColor(requireContext(), R.color.incorrect_answer);

        for (int i = 0; i < optionsViews.size(); i++) {
            MaterialCardView cardView = optionsViews.get(i);
            TextView statusText = cardView.findViewById(R.id.text_answer_status);

            cardView.setEnabled(false);

            boolean isCorrectAnswer = correctAnswers.contains(i);
            boolean isUserSelected = cardView.isChecked();

            if (isCorrectAnswer) {
                // Đáp án đúng: luôn hiển thị "Correct" màu xanh
                cardView.setStrokeColor(colorCorrect);
                cardView.setStrokeWidth(3);
                statusText.setText(R.string.label_correct);
                statusText.setTextColor(colorCorrect);
                statusText.setVisibility(View.VISIBLE);
            } else if (isUserSelected) {
                // Đáp án sai mà user đã chọn: hiển thị "Wrong" màu đỏ
                cardView.setStrokeColor(colorIncorrect);
                cardView.setStrokeWidth(3);
                statusText.setText(R.string.wrong_answer);
                statusText.setTextColor(colorIncorrect);
                statusText.setVisibility(View.VISIBLE);
            }
        }
    }
}