package dev.vtvinh24.ezquiz.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

public class QuizSetAdapter extends RecyclerView.Adapter<QuizSetAdapter.ViewHolder> {
  private final List<QuizSetEntity> quizSets;
  private final OnItemClickListener itemClickListener;
  private final OnPlayFlashcardClickListener playFlashcardClickListener;

  // === THÊM LISTENER MỚI VÀO ĐÂY ===
  private final OnPracticeClickListener practiceClickListener;

  // === THÊM INTERFACE MỚI ===
  public interface OnPracticeClickListener {
    void onPracticeClick(long quizSetId);
  }

  // === SỬA CONSTRUCTOR ĐỂ NHẬN LISTENER MỚI ===
  public QuizSetAdapter(List<QuizSetEntity> quizSets, OnItemClickListener itemClickListener,
                        OnPlayFlashcardClickListener playFlashcardClickListener,
                        OnPracticeClickListener practiceClickListener) {
    this.quizSets = quizSets;
    this.itemClickListener = itemClickListener;
    this.playFlashcardClickListener = playFlashcardClickListener;
    this.practiceClickListener = practiceClickListener; // Gán listener
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_set, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    QuizSetEntity quizSet = quizSets.get(position);
    holder.textName.setText(quizSet.name);
    holder.textDescription.setText(quizSet.description);

    // Giữ nguyên listener cho click toàn bộ item
    holder.itemView.setOnClickListener(v -> {
      if (itemClickListener != null) {
        itemClickListener.onItemClick(quizSet);
      }
    });

    // Giữ nguyên listener cho nút Play Flashcards
    holder.btnPlayFlashcard.setOnClickListener(v -> {
      if (playFlashcardClickListener != null) {
        playFlashcardClickListener.onPlayFlashcardClick(quizSet.id);
      }
    });

    // === BẮT SỰ KIỆN CHO NÚT MỚI ===
    holder.btnPracticeQuiz.setOnClickListener(v -> {
      if(practiceClickListener != null) {
        practiceClickListener.onPracticeClick(quizSet.id);
      }
    });
  }

  @Override
  public int getItemCount() {
    return quizSets.size();
  }

  public interface OnItemClickListener {
    void onItemClick(QuizSetEntity quizSet);
  }

  public interface OnPlayFlashcardClickListener {
    void onPlayFlashcardClick(long quizSetId);
  }

  // === SỬA VIEWHOLDER ĐỂ ÁNH XẠ NÚT MỚI ===
  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textName, textDescription;
    Button btnPlayFlashcard, btnPracticeQuiz; // Thêm nút practice

    ViewHolder(View itemView) {
      super(itemView);
      textName = itemView.findViewById(R.id.text_set_name);
      textDescription = itemView.findViewById(R.id.text_set_description);
      btnPlayFlashcard = itemView.findViewById(R.id.btn_play_flashcard);
      btnPracticeQuiz = itemView.findViewById(R.id.btn_practice_quiz); // Ánh xạ nút mới
    }
  }
}