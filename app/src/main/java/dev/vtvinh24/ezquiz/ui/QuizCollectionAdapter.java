package dev.vtvinh24.ezquiz.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;

public class QuizCollectionAdapter extends RecyclerView.Adapter<QuizCollectionAdapter.ViewHolder> {
  private final List<QuizCollectionEntity> collections;
  private final OnItemClickListener listener;
  public QuizCollectionAdapter(List<QuizCollectionEntity> collections, OnItemClickListener listener) {
    this.collections = collections;
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_collection, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    QuizCollectionEntity collection = collections.get(position);
    holder.textName.setText(collection.name);
    holder.textDescription.setText(collection.description);
    holder.itemView.setOnClickListener(v -> listener.onItemClick(collection));
  }

  @Override
  public int getItemCount() {
    return collections.size();
  }

  public interface OnItemClickListener {
    void onItemClick(QuizCollectionEntity collection);
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

