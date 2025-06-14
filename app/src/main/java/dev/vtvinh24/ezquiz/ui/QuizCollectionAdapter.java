package dev.vtvinh24.ezquiz.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;

public class QuizCollectionAdapter extends RecyclerView.Adapter<QuizCollectionAdapter.ViewHolder> {

  private final List<QuizCollectionEntity> collections;
  private final OnItemClickListener listener;

  public interface OnItemClickListener {
    void onItemClick(QuizCollectionEntity collection);
    void onItemLongClick(QuizCollectionEntity collection);
  }

  public QuizCollectionAdapter(List<QuizCollectionEntity> collections, OnItemClickListener listener) {
    this.collections = collections;
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_collection, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    QuizCollectionEntity collection = collections.get(position);
    Context context = holder.itemView.getContext();

    holder.textName.setText(collection.name);
    holder.textDescription.setText(collection.description);
    holder.textIndex.setText(String.valueOf(position + 1));

    // Mảng màu nền cho item
    int[] cardColors = {
            R.color.bg_quiz_1,
            R.color.bg_quiz_2,
            R.color.bg_quiz_3,
            R.color.bg_quiz_4,
            R.color.bg_quiz_5,
            R.color.bg_quiz_6
    };
    int cardColor = ContextCompat.getColor(context, cardColors[position % cardColors.length]);
    holder.cardView.setCardBackgroundColor(cardColor);

    // Mảng drawable cho vòng tròn chỉ số
    int[] circleDrawables = {
            R.drawable.bg_circle_blue,
            R.drawable.bg_circle_orange,
            R.drawable.bg_circle_purple,
            R.drawable.bg_circle_teal,
            R.drawable.bg_circle_red,
            R.drawable.bg_circle_green
    };
    holder.textIndex.setBackgroundResource(circleDrawables[position % circleDrawables.length]);

    // Sự kiện click
    holder.itemView.setOnClickListener(v -> listener.onItemClick(collection));
    holder.itemView.setOnLongClickListener(v -> {
      listener.onItemLongClick(collection);
      return true;
    });
  }

  @Override
  public int getItemCount() {
    return collections.size();
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textName, textDescription, textIndex;
    CardView cardView;

    ViewHolder(View itemView) {
      super(itemView);
      textName = itemView.findViewById(R.id.textName);
      textDescription = itemView.findViewById(R.id.textDescription);
      textIndex = itemView.findViewById(R.id.textIndex);
      cardView = (CardView) itemView;
    }
  }
}
