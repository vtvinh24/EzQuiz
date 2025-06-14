package dev.vtvinh24.ezquiz.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

public class QuizSetAdapter extends RecyclerView.Adapter<QuizSetAdapter.ViewHolder> {
  private final List<QuizSetEntity> sets;
  private final OnItemClickListener listener;
  public QuizSetAdapter(List<QuizSetEntity> sets, OnItemClickListener listener) {
    this.sets = sets;
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_set, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    QuizSetEntity set = sets.get(position);
    holder.textName.setText(set.name);
    holder.textDescription.setText(set.description);
    holder.itemView.setOnClickListener(v -> listener.onItemClick(set));
  }

  @Override
  public int getItemCount() {
    return sets.size();
  }

  public interface OnItemClickListener {
    void onItemClick(QuizSetEntity set);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textName, textDescription;

    ViewHolder(View itemView) {
      super(itemView);
      textName = itemView.findViewById(R.id.textName);
      textDescription = itemView.findViewById(R.id.textDescription);
    }
  }
}

