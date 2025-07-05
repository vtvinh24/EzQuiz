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
  private final List<QuizSetEntity> quizSets; // Đổi từ collections thành quizSets cho rõ ràng
  private final OnItemClickListener itemClickListener;
  private final OnPlayFlashcardClickListener playFlashcardClickListener; // NEW: Listener cho nút Play

  // Thay đổi constructor để nhận listener mới
  public QuizSetAdapter(List<QuizSetEntity> quizSets, OnItemClickListener itemClickListener, OnPlayFlashcardClickListener playFlashcardClickListener) {
    this.quizSets = quizSets;
    this.itemClickListener = itemClickListener;
    this.playFlashcardClickListener = playFlashcardClickListener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_set, parent, false); // Đảm bảo đúng tên layout item
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    QuizSetEntity quizSet = quizSets.get(position); // Đổi từ collection thành quizSet
    holder.textName.setText(quizSet.name);
    holder.textDescription.setText(quizSet.description);

    // Xử lý click cho toàn bộ item (nếu có)
    holder.itemView.setOnClickListener(v -> {
      if (itemClickListener != null) {
        itemClickListener.onItemClick(quizSet);
      }
    });

    // NEW: Xử lý click cho nút "Play Flashcards"
    holder.btnPlayFlashcard.setOnClickListener(v -> {
      if (playFlashcardClickListener != null) {
        playFlashcardClickListener.onPlayFlashcardClick(quizSet.id); // Truyền ID của Quiz Set
      }
    });
  }

  @Override
  public int getItemCount() {
    return quizSets.size();
  }

  // Interface cho click vào toàn bộ item (nếu có)
  public interface OnItemClickListener {
    void onItemClick(QuizSetEntity quizSet); // Đổi từ QuizCollectionEntity thành QuizSetEntity
  }

  // NEW: Interface cho click vào nút "Play Flashcards"
  public interface OnPlayFlashcardClickListener {
    void onPlayFlashcardClick(long quizSetId);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textName, textDescription;
    Button btnPlayFlashcard; // NEW: Ánh xạ nút

    ViewHolder(View itemView) {
      super(itemView);
      textName = itemView.findViewById(R.id.text_set_name); // Đảm bảo đúng ID
      textDescription = itemView.findViewById(R.id.text_set_description); // Đảm bảo đúng ID
      btnPlayFlashcard = itemView.findViewById(R.id.btn_play_flashcard); // NEW: Ánh xạ nút
    }
  }
}