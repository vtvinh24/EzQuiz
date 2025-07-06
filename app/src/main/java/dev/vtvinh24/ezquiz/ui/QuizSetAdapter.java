package dev.vtvinh24.ezquiz.ui;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Random;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

public class QuizSetAdapter extends RecyclerView.Adapter<QuizSetAdapter.ViewHolder> {

  private final List<QuizSetEntity> sets;
  private final OnItemClickListener listener;
  private final Random random = new Random();

  public QuizSetAdapter(List<QuizSetEntity> sets, OnItemClickListener listener) {
    this.sets = sets;
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_quiz_set, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    QuizSetEntity set = sets.get(position);
    Context context = holder.itemView.getContext();

    holder.textTitle.setText(set.name);
    holder.textDescription.setText(
            (set.description == null || set.description.trim().isEmpty())
                    ? "No description"
                    : set.description
    );

    holder.textIcon.setText(String.valueOf(position + 1));

    int randomColor = getRandomMaterialColor(context);

    GradientDrawable bg = (GradientDrawable) holder.textIcon.getBackground();
    bg.setColor(randomColor);

    // ✅ Set background cho card
    holder.cardView.setCardBackgroundColor(adjustAlpha(randomColor, 0.12f));

    holder.itemView.setOnClickListener(v -> listener.onItemClick(set));
    holder.itemView.setOnLongClickListener(v -> {
      listener.onItemLongClick(set);
      return true;
    });
  }

  @Override
  public int getItemCount() {
    return sets.size();
  }

  public interface OnItemClickListener {
    void onItemClick(QuizSetEntity set);
    void onItemLongClick(QuizSetEntity set);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textTitle, textDescription, textIcon;
    CardView cardView;

    ViewHolder(View itemView) {
      super(itemView);
      textTitle = itemView.findViewById(R.id.text_title);
      textDescription = itemView.findViewById(R.id.text_description);
      textIcon = itemView.findViewById(R.id.text_icon);
      cardView = itemView.findViewById(R.id.card_container);
    }
  }

  private int getRandomMaterialColor(Context context) {
    int[] colors = context.getResources().getIntArray(R.array.random_material_colors);
    return colors[random.nextInt(colors.length)];
  }

  private int adjustAlpha(int color, float factor) {
    int alpha = Math.round(Color.alpha(color) * factor);
    int red = Color.red(color);
    int green = Color.green(color);
    int blue = Color.blue(color);
    return Color.argb(alpha, red, green, blue);
  }
}
