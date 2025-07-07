package dev.vtvinh24.ezquiz.ui.fragment;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.ui.QuizSetListActivity;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;

public class ProgressFragment extends Fragment {

    private TextView textOverallProgress, textOverallPercentage, textStreakCount;
    private TextView textTotalFlashcards, textMasteredCards, textStudySessions, textStudyTime;
    private ProgressBar progressOverall;
    private LinearLayout weeklyChartContainer;
    private RecyclerView recyclerSubjectProgress;

    private List<SubjectProgressData> subjectProgressList;
    private SubjectProgressAdapter subjectProgressAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupRecyclerView();
        loadProgressData();
        setupWeeklyChart();
        animateProgressBars();
    }

    private void initViews(View view) {
        textOverallProgress = view.findViewById(R.id.text_overall_progress);
        textOverallPercentage = view.findViewById(R.id.text_overall_percentage);
        textStreakCount = view.findViewById(R.id.text_streak_count);
        textTotalFlashcards = view.findViewById(R.id.text_total_flashcards);
        textMasteredCards = view.findViewById(R.id.text_mastered_cards);
        textStudySessions = view.findViewById(R.id.text_study_sessions);
        textStudyTime = view.findViewById(R.id.text_study_time);
        progressOverall = view.findViewById(R.id.progress_overall);
        weeklyChartContainer = view.findViewById(R.id.weekly_chart_container);
        recyclerSubjectProgress = view.findViewById(R.id.recycler_subject_progress);
    }

    private void setupRecyclerView() {
        subjectProgressList = new ArrayList<>();
        subjectProgressAdapter = new SubjectProgressAdapter(subjectProgressList);
        recyclerSubjectProgress.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerSubjectProgress.setAdapter(subjectProgressAdapter);
    }

    private void loadProgressData() {
        // Simulate loading real data - replace with actual database calls
        int totalCards = 156;
        int masteredCards = 98;
        int learningCards = 35;
        int unknownCards = 23;

        float overallProgress = (float) masteredCards / totalCards * 100;
        int streakDays = 7;
        int studySessions = 23;
        float studyHours = 12.5f;

        // Update UI with data
        textOverallProgress.setText(String.format("Bạn đã học %.0f%% nội dung", overallProgress));
        textOverallPercentage.setText(String.format("%.0f%%", overallProgress));
        textStreakCount.setText(String.valueOf(streakDays));
        textTotalFlashcards.setText(String.valueOf(totalCards));
        textMasteredCards.setText(String.valueOf(masteredCards));
        textStudySessions.setText(String.valueOf(studySessions));
        textStudyTime.setText(String.format("%.1fh", studyHours));

        progressOverall.setProgress((int) overallProgress);

        // Load subject progress data
        loadSubjectProgressData();
    }

    private void loadSubjectProgressData() {
        subjectProgressList.clear();

        // Sample subject data with quizSetId - replace with real data from database
        subjectProgressList.add(new SubjectProgressData(
            "Từ vựng Tiếng Anh", 45, 10, 5, 60, "#2196F3", 1L
        ));
        subjectProgressList.add(new SubjectProgressData(
            "Ngữ pháp", 32, 8, 10, 50, "#4CAF50", 2L
        ));
        subjectProgressList.add(new SubjectProgressData(
            "Lịch sử Việt Nam", 28, 12, 6, 46, "#FF9800", 3L
        ));

        subjectProgressAdapter.notifyDataSetChanged();
    }

    private void setupWeeklyChart() {
        weeklyChartContainer.removeAllViews();

        // Sample weekly data - replace with real data
        int[] weeklyData = {15, 23, 18, 30, 25, 20, 35}; // Study minutes per day
        int maxValue = 40; // Maximum value for scaling

        for (int i = 0; i < weeklyData.length; i++) {
            View barView = createWeeklyBarView(weeklyData[i], maxValue);
            weeklyChartContainer.addView(barView);
        }
    }

    private View createWeeklyBarView(int value, int maxValue) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
            0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
        params.setMargins(4, 0, 4, 0);

        LinearLayout container = new LinearLayout(getContext());
        container.setLayoutParams(params);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setGravity(android.view.Gravity.BOTTOM | android.view.Gravity.CENTER_HORIZONTAL);

        // Create bar
        View bar = new View(getContext());
        int barHeight = (int) (140 * ((float) value / maxValue)); // Scale to container height
        LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(
            24, barHeight);
        bar.setLayoutParams(barParams);

        // Set bar color based on value
        if (value > 25) {
            bar.setBackgroundColor(Color.parseColor("#4CAF50")); // Green for high activity
        } else if (value > 10) {
            bar.setBackgroundColor(Color.parseColor("#FF9800")); // Orange for medium activity
        } else {
            bar.setBackgroundColor(Color.parseColor("#E0E0E0")); // Gray for low activity
        }

        // Add corner radius
        bar.setBackground(getContext().getDrawable(R.drawable.circle_background));
        bar.getBackground().setTint(bar.getSolidColor());

        container.addView(bar);
        return container;
    }

    private void animateProgressBars() {
        // Animate overall progress bar
        ValueAnimator progressAnimator = ValueAnimator.ofInt(0, progressOverall.getProgress());
        progressAnimator.setDuration(1500);
        progressAnimator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            progressOverall.setProgress(animatedValue);
        });
        progressAnimator.start();

        // Animate count-up for statistics
        animateCountUp(textTotalFlashcards, 156, 1000);
        animateCountUp(textMasteredCards, 98, 1200);
        animateCountUp(textStudySessions, 23, 800);
        animateCountUp(textStreakCount, 7, 600);
    }

    private void animateCountUp(TextView textView, int targetValue, int duration) {
        ValueAnimator animator = ValueAnimator.ofInt(0, targetValue);
        animator.setDuration(duration);
        animator.addUpdateListener(animation -> {
            int animatedValue = (int) animation.getAnimatedValue();
            textView.setText(String.valueOf(animatedValue));
        });
        animator.start();
    }

    // Data class for subject progress
    private static class SubjectProgressData {
        String name;
        int knownCount;
        int learningCount;
        int unknownCount;
        int totalCount;
        String color;
        long quizSetId; // Add quizSetId for navigation

        SubjectProgressData(String name, int known, int learning, int unknown, int total, String color, long quizSetId) {
            this.name = name;
            this.knownCount = known;
            this.learningCount = learning;
            this.unknownCount = unknown;
            this.totalCount = total;
            this.color = color;
            this.quizSetId = quizSetId;
        }

        float getProgressPercentage() {
            return (float) knownCount / totalCount * 100;
        }
    }

    // Adapter for subject progress RecyclerView
    private static class SubjectProgressAdapter extends RecyclerView.Adapter<SubjectProgressAdapter.ViewHolder> {
        private List<SubjectProgressData> data;

        SubjectProgressAdapter(List<SubjectProgressData> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_subject_progress, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            SubjectProgressData item = data.get(position);
            holder.bind(item);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView textSubjectName, textSubjectStats, textSubjectPercentage;
            TextView textKnownCount, textLearningCount, textUnknownCount;
            TextView textProgressCenter;
            View circularProgressView;
            com.google.android.material.button.MaterialButton btnContinueStudy;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                textSubjectName = itemView.findViewById(R.id.text_subject_name);
                textSubjectStats = itemView.findViewById(R.id.text_subject_stats);
                textSubjectPercentage = itemView.findViewById(R.id.text_subject_percentage);
                textKnownCount = itemView.findViewById(R.id.text_known_count);
                textLearningCount = itemView.findViewById(R.id.text_learning_count);
                textUnknownCount = itemView.findViewById(R.id.text_unknown_count);
                textProgressCenter = itemView.findViewById(R.id.text_progress_center);
                circularProgressView = itemView.findViewById(R.id.circular_progress_view);
                btnContinueStudy = itemView.findViewById(R.id.btn_continue_study);
            }

            void bind(SubjectProgressData data) {
                textSubjectName.setText(data.name);
                textSubjectStats.setText(String.format("%d/%d thẻ đã học",
                    data.knownCount, data.totalCount));

                float percentage = data.getProgressPercentage();
                textSubjectPercentage.setText(String.format("%.0f%%", percentage));
                textProgressCenter.setText(String.format("%.0f%%", percentage));

                textKnownCount.setText(String.valueOf(data.knownCount));
                textLearningCount.setText(String.valueOf(data.learningCount));
                textUnknownCount.setText(String.valueOf(data.unknownCount));

                // Set up continue study button click listener
                btnContinueStudy.setOnClickListener(v -> {
                    Intent intent = new Intent(itemView.getContext(), QuizSetListActivity.class);
                    intent.putExtra("quizSetId", data.quizSetId);
                    itemView.getContext().startActivity(intent);
                });

                // Animate circular progress
                if (circularProgressView.getBackground() instanceof android.graphics.drawable.GradientDrawable) {
                    android.graphics.drawable.GradientDrawable drawable =
                        (android.graphics.drawable.GradientDrawable) circularProgressView.getBackground();

                    ValueAnimator animator = ValueAnimator.ofInt(0, (int) (percentage * 100));
                    animator.setDuration(1000);
                    animator.addUpdateListener(animation -> {
                        int animatedValue = (int) animation.getAnimatedValue();
                        drawable.setLevel(animatedValue);
                        textProgressCenter.setText(String.format("%.0f%%", animatedValue / 100f));
                    });
                    animator.start();
                }
            }
        }
    }
}
