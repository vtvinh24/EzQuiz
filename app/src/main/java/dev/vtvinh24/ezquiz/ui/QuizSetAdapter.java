package dev.vtvinh24.ezquiz.ui;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

public class QuizSetAdapter extends RecyclerView.Adapter<QuizSetAdapter.ViewHolder> {
  private final List<QuizSetEntity> quizSets;
  private final Context context;


  private final OnItemClickListener itemClickListener;
  private final OnPlayFlashcardClickListener playFlashcardClickListener;
  private final OnPracticeClickListener practiceClickListener;
  private final OnTestClickListener testClickListener;

  private final int[][] gradientColors = {
          {R.color.gradient_blue_start, R.color.gradient_blue_end},
          {R.color.gradient_green_start, R.color.gradient_green_end},
          {R.color.gradient_orange_start, R.color.gradient_orange_end},
          {R.color.gradient_purple_start, R.color.gradient_purple_end},
          {R.color.gradient_pink_start, R.color.gradient_pink_end}
  };

  public QuizSetAdapter(Context context, List<QuizSetEntity> quizSets,
                        OnItemClickListener itemClickListener,
                        OnPlayFlashcardClickListener playFlashcardClickListener,
                        OnPracticeClickListener practiceClickListener,
                        OnTestClickListener testClickListener) {
    this.context = context;
    this.quizSets = quizSets;
    this.itemClickListener = itemClickListener;
    this.playFlashcardClickListener = playFlashcardClickListener;
    this.practiceClickListener = practiceClickListener;
    this.testClickListener = testClickListener;
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
    Context itemContext = holder.itemView.getContext();


    holder.textName.setText(quizSet.name != null ? quizSet.name : "Unnamed Quiz Set");
    String description = quizSet.description;
    if (description == null || description.trim().isEmpty()) {
      description = "No description available";
    }
    holder.textDescription.setText(description);


    setGradientBackground(holder.iconBackground, position, itemContext);

    // --- Get actual quiz count from database ---

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Handler mainHandler = new Handler(Looper.getMainLooper());
    executor.execute(() -> {
      AppDatabase db = AppDatabaseProvider.getDatabase(itemContext);
      int quizCount = db.quizDao().countByQuizSetId(quizSet.id);
      mainHandler.post(() -> {
        String quizCountText = quizCount + (quizCount == 1 ? " quiz" : " quizzes");
        holder.chipQuizCount.setText(quizCountText);
      });
    });


    // --- Set difficulty ---
    String difficulty = getDifficultyText(quizSet.difficulty);
    holder.chipDifficulty.setText(difficulty);
    int difficultyColor = getDifficultyColor(quizSet.difficulty);
    holder.chipDifficulty.setChipBackgroundColorResource(difficultyColor);

    // === Set click listeners for all actions ===
    holder.itemView.setOnClickListener(v -> {
      if (itemClickListener != null) {
        itemClickListener.onItemClick(quizSet);
      }
    });

    holder.btnPlayFlashcard.setOnClickListener(v -> {
      if (playFlashcardClickListener != null) {
        playFlashcardClickListener.onPlayFlashcardClick(quizSet.id);
      }
    });

    holder.btnPracticeQuiz.setOnClickListener(v -> {
      if (practiceClickListener != null) {
        practiceClickListener.onPracticeClick(quizSet.id);
      }
    });


    holder.btnTest.setOnClickListener(v -> {
      if (testClickListener != null) {
        testClickListener.onTestClick(quizSet.id);
      }
    });
  }

  @Override
  public int getItemCount() {
    return quizSets.size();
  }


  private void setGradientBackground(ImageView iconBackground, int position, Context context) {
    int colorIndex = position % gradientColors.length;
    int startColorRes = gradientColors[colorIndex][0];
    int endColorRes = gradientColors[colorIndex][1];
    int startColor = ContextCompat.getColor(context, startColorRes);
    int endColor = ContextCompat.getColor(context, endColorRes);
    GradientDrawable gradientDrawable = new GradientDrawable();
    gradientDrawable.setShape(GradientDrawable.OVAL);
    gradientDrawable.setColors(new int[]{startColor, endColor});
    gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
    gradientDrawable.setOrientation(GradientDrawable.Orientation.TL_BR);
    iconBackground.setImageDrawable(gradientDrawable);
  }

  private String getDifficultyText(int difficulty) {
    switch (difficulty) {
      case 1: return "Easy";
      case 2: return "Medium";
      case 3: return "Hard";
      default: return "Unknown";
    }
  }

  private int getDifficultyColor(int difficulty) {
    switch (difficulty) {
      case 1: return R.color.gradient_green_start;
      case 2: return R.color.gradient_orange_start;
      case 3: return R.color.gradient_pink_start;
      default: return R.color.gradient_blue_start;
    }
  }


  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textName, textDescription;
    ImageView iconBackground, iconQuizSet;
    Chip chipQuizCount, chipDifficulty;
    MaterialButton btnPlayFlashcard;
    MaterialButton btnPracticeQuiz;
    MaterialButton btnTest;

    ViewHolder(View itemView) {
      super(itemView);
      textName = itemView.findViewById(R.id.text_set_name);
      textDescription = itemView.findViewById(R.id.text_set_description);
      iconBackground = itemView.findViewById(R.id.icon_background);
      iconQuizSet = itemView.findViewById(R.id.icon_quiz_set);
      chipQuizCount = itemView.findViewById(R.id.chip_quiz_count);
      chipDifficulty = itemView.findViewById(R.id.chip_difficulty);
      btnPlayFlashcard = itemView.findViewById(R.id.btn_play_flashcard);
      btnPracticeQuiz = itemView.findViewById(R.id.btn_practice_quiz);
      btnTest = itemView.findViewById(R.id.btn_test);
    }
  }


  public interface OnItemClickListener {
    void onItemClick(QuizSetEntity quizSet);
  }

  public interface OnPlayFlashcardClickListener {
    void onPlayFlashcardClick(long quizSetId);
  }

  public interface OnPracticeClickListener {
    void onPracticeClick(long quizSetId);
  }

  public interface OnTestClickListener {
    void onTestClick(long quizSetId);
  }
}