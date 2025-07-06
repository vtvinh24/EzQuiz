package dev.vtvinh24.ezquiz.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;

public class QuizCollectionAdapter extends RecyclerView.Adapter<QuizCollectionAdapter.ViewHolder> {
  private final List<QuizCollectionEntity> collections;
  private final OnItemClickListener listener;
  private final Context context;

  // Array of gradient colors for collection icons
  private final int[][] gradientColors = {
      {R.color.gradient_blue_start, R.color.gradient_blue_end},
      {R.color.gradient_green_start, R.color.gradient_green_end},
      {R.color.gradient_orange_start, R.color.gradient_orange_end},
      {R.color.gradient_purple_start, R.color.gradient_purple_end},
      {R.color.gradient_pink_start, R.color.gradient_pink_end}
  };

  public QuizCollectionAdapter(List<QuizCollectionEntity> collections, OnItemClickListener listener) {
    this.collections = collections;
    this.listener = listener;
    this.context = null;
  }

  public QuizCollectionAdapter(Context context, List<QuizCollectionEntity> collections, OnItemClickListener listener) {
    this.context = context;
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
    Context itemContext = holder.itemView.getContext();

    // Set collection name and description
    holder.textName.setText(collection.name != null ? collection.name : "Unnamed Collection");

    String description = collection.description;
    if (description == null || description.trim().isEmpty()) {
      description = "No description available";
    }
    holder.textDescription.setText(description);

    // Set dynamic gradient background for icon
    setGradientBackground(holder.iconBackground, position, itemContext);

    // Get actual quiz set count from database
    AppDatabase db = AppDatabaseProvider.getDatabase(itemContext);
    int setCount = db.quizSetDao().countByCollectionId(collection.id);
    String setCountText = setCount + (setCount == 1 ? " set" : " sets");
    holder.chipQuizCount.setText(setCountText);

    // Set last updated info
    String lastUpdated = formatLastUpdated(collection.updatedAt);
    holder.chipLastUpdated.setText(lastUpdated);

    // Set click listener
    holder.itemView.setOnClickListener(v -> {
      if (listener != null) {
        listener.onItemClick(collection);
      }
    });
  }

  @Override
  public int getItemCount() {
    return collections.size();
  }

  private void setGradientBackground(ImageView iconBackground, int position, Context context) {
    // Select gradient colors based on position
    int colorIndex = position % gradientColors.length;
    int startColorRes = gradientColors[colorIndex][0];
    int endColorRes = gradientColors[colorIndex][1];

    int startColor = ContextCompat.getColor(context, startColorRes);
    int endColor = ContextCompat.getColor(context, endColorRes);

    // Create gradient drawable
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setShape(GradientDrawable.OVAL);
    gradientDrawable.setColors(new int[]{startColor, endColor});
    gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    gradientDrawable.setOrientation(GradientDrawable.Orientation.TL_BR);

    iconBackground.setImageDrawable(gradientDrawable);
  }

  private String formatLastUpdated(long timestamp) {
    if (timestamp == 0) {
      return "Just created";
    }

    long now = System.currentTimeMillis();
    long diff = now - timestamp;

    // Less than 1 hour
    if (diff < 3600000) {
      long minutes = diff / 60000;
      if (minutes < 1) return "Just now";
      return minutes + "m ago";
    }
    // Less than 1 day
    else if (diff < 86400000) {
      long hours = diff / 3600000;
      return hours + "h ago";
    }
    // Less than 1 week
    else if (diff < 604800000) {
      long days = diff / 86400000;
      return days + "d ago";
    }
    // More than 1 week
    else {
      SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.getDefault());
      return sdf.format(new Date(timestamp));
    }
  }

  public interface OnItemClickListener {
    void onItemClick(QuizCollectionEntity collection);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textName, textDescription;
    ImageView iconBackground, iconCollection;
    Chip chipQuizCount, chipLastUpdated;

    ViewHolder(View itemView) {
      super(itemView);
      textName = itemView.findViewById(R.id.textName);
      textDescription = itemView.findViewById(R.id.textDescription);
      iconBackground = itemView.findViewById(R.id.icon_background);
      iconCollection = itemView.findViewById(R.id.icon_collection);
      chipQuizCount = itemView.findViewById(R.id.chip_quiz_count);
      chipLastUpdated = itemView.findViewById(R.id.chip_last_updated);
    }
  }
}
