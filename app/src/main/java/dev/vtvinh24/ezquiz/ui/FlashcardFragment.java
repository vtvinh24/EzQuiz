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

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.Quiz;

public class FlashcardFragment extends Fragment {

  private AnimatorSet frontAnim, backAnim;
  private boolean isFrontVisible = true;
  private View cardFront, cardBack;

  public static FlashcardFragment newInstance(Quiz flashcard) {
    FlashcardFragment fragment = new FlashcardFragment();
    Bundle args = new Bundle();
    // Quiz đã là Serializable, không cần thay đổi
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
    if (flashcard == null) return; // Thoát nếu không có dữ liệu

    // --- CÀI ĐẶT CHUNG ---
    cardFront = view.findViewById(R.id.card_front);
    cardBack = view.findViewById(R.id.card_back);
    loadAnimations();

    float scale = getContext().getResources().getDisplayMetrics().density;
    cardFront.setCameraDistance(8000 * scale);
    cardBack.setCameraDistance(8000 * scale);

    // --- BẮT ĐẦU THAY ĐỔI LOGIC HIỂN THỊ ---

    // 1. HIỂN THỊ MẶT TRƯỚC (Câu hỏi + TẤT CẢ lựa chọn)
    TextView textQuestion = view.findViewById(R.id.text_flashcard_question);
    LinearLayout frontAnswersContainer = view.findViewById(R.id.front_answers_container);
    textQuestion.setText(flashcard.getQuestion());

    // Xóa các view cũ trước khi thêm mới (quan trọng)
    frontAnswersContainer.removeAllViews();

    // Lấy danh sách tất cả các lựa chọn
    List<String> allAnswers = flashcard.getAnswers();
    if (allAnswers != null) {
      for (int i = 0; i < allAnswers.size(); i++) {
        // Tạo một TextView mới cho mỗi lựa chọn
        TextView answerView = new TextView(getContext());
        answerView.setText(String.format("• %s", allAnswers.get(i))); // Thêm dấu • cho đẹp
        answerView.setTextSize(18); // Kích thước chữ
        answerView.setPadding(0, 8, 0, 8); // Khoảng cách giữa các dòng
        frontAnswersContainer.addView(answerView); // Thêm vào layout
      }
    }

    // 2. HIỂN THỊ MẶT SAU (CHỈ đáp án đúng)
    TextView textAnswer = view.findViewById(R.id.text_flashcard_answer);
    List<Integer> correctIndices = flashcard.getCorrectAnswerIndices();
    List<String> correctAnswers = new ArrayList<>();

    if (allAnswers != null && correctIndices != null) {
      for (Integer index : correctIndices) {
        // Lấy ra chuỗi đáp án đúng dựa vào chỉ số
        if (index >= 0 && index < allAnswers.size()) {
          correctAnswers.add(allAnswers.get(index));
        }
      }
    }

    if (correctAnswers.isEmpty()) {
      textAnswer.setText("Không có đáp án đúng được chỉ định.");
    } else {
      // Nối các đáp án đúng lại với nhau, mỗi đáp án một dòng
      textAnswer.setText(String.join("\n", correctAnswers));
    }

    // --- KẾT THÚC THAY ĐỔI LOGIC ---

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