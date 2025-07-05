// File: dev/vtvinh24/ezquiz/ui/FlashcardFragment.java
package dev.vtvinh24.ezquiz.ui;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
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

        Quiz flashcard = (Quiz) getArguments().getSerializable("flashcard_data");

        // Ánh xạ View và Animation
        cardFront = view.findViewById(R.id.card_front);
        cardBack = view.findViewById(R.id.card_back);
        loadAnimations();

        // Đặt khoảng cách camera để hiệu ứng 3D đẹp hơn
        float scale = getContext().getResources().getDisplayMetrics().density;
        cardFront.setCameraDistance(8000 * scale);
        cardBack.setCameraDistance(8000 * scale);

        // Hiển thị dữ liệu
        TextView textQuestion = view.findViewById(R.id.text_flashcard_question);
        TextView textAnswer = view.findViewById(R.id.text_flashcard_answer);
        textQuestion.setText(flashcard.getQuestion());
        // Giả sử flashcard chỉ có 1 câu trả lời
        if (!flashcard.getAnswers().isEmpty()) {
            textAnswer.setText(flashcard.getAnswers().get(0));
        }

        // Đặt sự kiện click để lật thẻ
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