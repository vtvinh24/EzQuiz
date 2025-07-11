package dev.vtvinh24.ezquiz.ui;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.Quiz;

public class FlashcardFragment extends Fragment {

  private AnimatorSet frontAnim, backAnim;
  private boolean isFrontVisible = true;
  private View cardFront, cardBack;

  public static FlashcardFragment newInstance(Quiz flashcard) {
    FlashcardFragment fragment = new FlashcardFragment();
    Bundle args = new Bundle();
    args.putSerializable("flashcard_data", flashcard);
    fragment.setArguments(args);
    return fragment;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_flashcard, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    if (getArguments() == null) return;
    Quiz flashcard = (Quiz) getArguments().getSerializable("flashcard_data");
    if (flashcard == null) return;

    cardFront = view.findViewById(R.id.card_front);
    cardBack = view.findViewById(R.id.card_back);
    loadAnimations();

    float scale = requireContext().getResources().getDisplayMetrics().density;
    cardFront.setCameraDistance(8000 * scale);
    cardBack.setCameraDistance(8000 * scale);

    // --- Display Front (Question + All choices) ---
    TextView textQuestion = view.findViewById(R.id.text_flashcard_question);
    LinearLayout frontAnswersContainer = view.findViewById(R.id.front_answers_container);
    textQuestion.setText(flashcard.getQuestion());

    frontAnswersContainer.removeAllViews();

    List<String> allAnswers = flashcard.getAnswers();
    if (allAnswers != null) {
      for (String answer : allAnswers) {
        TextView answerView = new TextView(getContext());
        answerView.setText(getString(R.string.flashcard_answer_bullet_format, answer));
        answerView.setTextSize(18);
        answerView.setPadding(0, 8, 0, 8);
        frontAnswersContainer.addView(answerView);
      }
    }

    // --- Display Back (Correct answer(s) only) ---
    TextView textAnswer = view.findViewById(R.id.text_flashcard_answer);
    List<Integer> correctIndices = flashcard.getCorrectAnswerIndices();
    List<String> correctAnswers = new ArrayList<>();

    if (allAnswers != null && correctIndices != null) {
      for (Integer index : correctIndices) {
        if (index >= 0 && index < allAnswers.size()) {
          correctAnswers.add(allAnswers.get(index));
        }
      }
    }

    if (correctAnswers.isEmpty()) {
      textAnswer.setText(R.string.flashcard_no_correct_answer_specified);
    } else {
      textAnswer.setText(String.join("\n", correctAnswers));
    }

    view.setOnClickListener(v -> flipCard());
  }

  private void loadAnimations() {
    frontAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_out);
    backAnim = (AnimatorSet) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_in);
  }

  private void flipCard() {
    if (isFrontVisible) {
      frontAnim.setTarget(cardFront);
      backAnim.setTarget(cardBack);
      frontAnim.start();
      backAnim.start();
      isFrontVisible = false;
    } else {
      frontAnim.setTarget(cardBack);
      backAnim.setTarget(cardFront);
      frontAnim.start();
      backAnim.start();
      isFrontVisible = true;
    }
  }
}