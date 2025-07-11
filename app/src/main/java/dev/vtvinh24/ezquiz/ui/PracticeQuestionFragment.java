package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

        textQuestion.setText(quiz.getQuestion());
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

        for (int i = 0; i < answers.size(); i++) {
            MaterialCardView cardView = (MaterialCardView) inflater.inflate(R.layout.item_practice_answer, answersContainer, false);
            TextView textAnswer = cardView.findViewById(R.id.text_answer_option);
            textAnswer.setText(answers.get(i));

            final int position = i;
            cardView.setOnClickListener(v -> handleAnswerClick(cardView, position));

            optionsViews.add(cardView);
            answersContainer.addView(cardView);
        }
    }

    private void handleAnswerClick(MaterialCardView clickedCard, int position) {
        if (quiz.getType() == Quiz.Type.SINGLE_CHOICE) {
            for (MaterialCardView card : optionsViews) {
                card.setChecked(false);
            }
            clickedCard.setChecked(true);
            viewModel.onAnswerSelected(quizId, Collections.singletonList(position));
        } else { // MULTIPLE_CHOICE
            clickedCard.setChecked(!clickedCard.isChecked());
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

    /**
     * === CẬP NHẬT PHƯƠNG THỨC NÀY ===
     * Hiển thị kết quả sau khi người dùng nhấn "Check Answer".
     * Bao gồm tô màu viền card và hiển thị nhãn "Đúng"/"Sai".
     */
    private void showResults() {
        List<Integer> correctAnswers = quiz.getCorrectAnswerIndices();
        int colorCorrect = ContextCompat.getColor(requireContext(), R.color.correct_answer);
        int colorIncorrect = ContextCompat.getColor(requireContext(), R.color.incorrect_answer);

        for (int i = 0; i < optionsViews.size(); i++) {
            MaterialCardView cardView = optionsViews.get(i);
            // Lấy TextView trạng thái từ bên trong card
            TextView statusText = cardView.findViewById(R.id.text_answer_status);

            cardView.setEnabled(false); // Vô hiệu hóa click sau khi đã kiểm tra

            boolean isCorrectAnswer = correctAnswers.contains(i);
            boolean isUserSelected = cardView.isChecked();

            if (isCorrectAnswer) {
                // Nếu là đáp án đúng
                cardView.setStrokeColor(colorCorrect);
                cardView.setChecked(true); // Hiển thị icon check cho mọi đáp án đúng

                statusText.setText("Đúng");
                statusText.setTextColor(colorCorrect);
                statusText.setVisibility(View.VISIBLE);

            } else if (isUserSelected) {
                // Nếu là đáp án sai mà người dùng đã chọn
                cardView.setStrokeColor(colorIncorrect);

                statusText.setText("Sai");
                statusText.setTextColor(colorIncorrect);
                statusText.setVisibility(View.VISIBLE);
            }
        }
    }
}