package dev.vtvinh24.ezquiz.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.HistoryItem;
import dev.vtvinh24.ezquiz.ui.PracticeActivity;
import dev.vtvinh24.ezquiz.ui.TestResultActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<HistoryItem> historyItems = new ArrayList<>();
    private final Context context;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());

    private final int[][] gradientColors = {
            {R.color.gradient_blue_start, R.color.gradient_blue_end},
            {R.color.gradient_green_start, R.color.gradient_green_end},
            {R.color.gradient_orange_start, R.color.gradient_orange_end},
            {R.color.gradient_purple_start, R.color.gradient_purple_end},
            {R.color.gradient_pink_start, R.color.gradient_pink_end}
    };

    public HistoryAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        HistoryItem item = historyItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public void setHistoryItems(List<HistoryItem> items) {
        this.historyItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    private void setGradientBackground(ImageView imageView, int position, Context context) {
        int colorIndex = position % gradientColors.length;
        int startColor = ContextCompat.getColor(context, gradientColors[colorIndex][0]);
        int endColor = ContextCompat.getColor(context, gradientColors[colorIndex][1]);

        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[]{startColor, endColor}
        );
        gradientDrawable.setShape(GradientDrawable.OVAL);
        imageView.setBackground(gradientDrawable);
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView quizSetNameText;
        private final TextView collectionNameText;
        private final Chip statusChip;
        private final TextView lastUpdatedText;
        private final ProgressBar progressBar;
        private final TextView progressText;
        private final ImageView statusIcon;
        private final ImageView iconBackground;
        private final MaterialButton actionButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_history_item);
            quizSetNameText = itemView.findViewById(R.id.text_quiz_set_name);
            collectionNameText = itemView.findViewById(R.id.text_collection_name);
            statusChip = itemView.findViewById(R.id.chip_status);
            lastUpdatedText = itemView.findViewById(R.id.text_last_updated);
            progressBar = itemView.findViewById(R.id.progress_bar);
            progressText = itemView.findViewById(R.id.text_progress);
            statusIcon = itemView.findViewById(R.id.icon_status);
            iconBackground = itemView.findViewById(R.id.icon_background);
            actionButton = itemView.findViewById(R.id.button_action);
        }

        public void bind(HistoryItem item, int position) {
            // Use correct method names from HistoryItem
            quizSetNameText.setText(item.getQuizSet() != null ? item.getQuizSet().name : "Unknown Quiz");
            collectionNameText.setText(item.getCollectionName() != null ? item.getCollectionName() : "Unknown Collection");

            // Set gradient background for icon
            setGradientBackground(iconBackground, position, itemView.getContext());

            // Configure progress using correct methods
            int progress = item.getCompletedQuestions();
            int total = item.getTotalQuestions();
            progressBar.setMax(total);
            progressBar.setProgress(progress);
            progressText.setText(String.format(Locale.getDefault(), "%d/%d", progress, total));

            // Configure status chip with colors
            String status = getStatusFromItem(item);
            configureStatusChip(status);

            // Set appropriate icon based on status
            setStatusIcon(status);

            // Configure action button
            configureActionButton(item, status);

            // Set last updated time using correct method
            long lastUpdated = item.getLastUpdated();
            if (lastUpdated > 0) {
                String timeAgo = getTimeAgo(lastUpdated);
                lastUpdatedText.setText(timeAgo);
            } else {
                lastUpdatedText.setText("");
            }
        }

        private String getStatusFromItem(HistoryItem item) {
            if (item.isCompleted()) {
                return "COMPLETED";
            } else if (item.getCompletedQuestions() > 0) {
                return "IN_PROGRESS";
            } else {
                return "NOT_STARTED";
            }
        }

        private void configureStatusChip(String status) {
            switch (status) {
                case "COMPLETED":
                    statusChip.setText("Completed");
                    statusChip.setChipBackgroundColorResource(R.color.correct_answer);
                    break;
                case "IN_PROGRESS":
                    statusChip.setText("In Progress");
                    statusChip.setChipBackgroundColorResource(R.color.bottom_nav_active);
                    break;
                case "NOT_STARTED":
                    statusChip.setText("Not Started");
                    statusChip.setChipBackgroundColorResource(R.color.bottom_nav_inactive);
                    break;
                default:
                    statusChip.setText("Unknown");
                    statusChip.setChipBackgroundColorResource(R.color.bottom_nav_inactive);
                    break;
            }
        }

        private void setStatusIcon(String status) {
            switch (status) {
                case "COMPLETED":
                    statusIcon.setImageResource(R.drawable.ic_check_circle);
                    break;
                case "IN_PROGRESS":
                    statusIcon.setImageResource(R.drawable.ic_arrow_forward);
                    break;
                case "NOT_STARTED":
                    statusIcon.setImageResource(R.drawable.ic_add);
                    break;
                default:
                    statusIcon.setImageResource(R.drawable.ic_history);
                    break;
            }
        }

        private void configureActionButton(HistoryItem item, String status) {
            switch (status) {
                case "COMPLETED":
                    actionButton.setText("Review");
                    actionButton.setIconResource(R.drawable.ic_info);
                    actionButton.setOnClickListener(v -> {
                        Intent intent = new Intent(context, TestResultActivity.class);
                        intent.putExtra("quizSetId", item.getQuizSet().id);
                        context.startActivity(intent);
                    });
                    break;
                case "IN_PROGRESS":
                    actionButton.setText("Continue");
                    actionButton.setIconResource(R.drawable.ic_arrow_forward);
                    actionButton.setOnClickListener(v -> {
                        Intent intent = new Intent(context, PracticeActivity.class);
                        intent.putExtra(PracticeActivity.EXTRA_SET_ID, item.getQuizSet().id);
                        intent.putExtra("continueFromHistory", true);
                        context.startActivity(intent);
                    });
                    break;
                case "NOT_STARTED":
                    actionButton.setText("Start");
                    actionButton.setIconResource(R.drawable.ic_arrow_forward);
                    actionButton.setOnClickListener(v -> {
                        Intent intent = new Intent(context, PracticeActivity.class);
                        intent.putExtra(PracticeActivity.EXTRA_SET_ID, item.getQuizSet().id);
                        context.startActivity(intent);
                    });
                    break;
                default:
                    actionButton.setText("View");
                    actionButton.setIconResource(R.drawable.ic_info);
                    actionButton.setOnClickListener(null);
                    break;
            }
        }

        private String getTimeAgo(long timestamp) {
            long diff = System.currentTimeMillis() - timestamp;
            long seconds = diff / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            long weeks = days / 7;
            long months = days / 30;

            if (months > 0) {
                return months + "mo ago";
            } else if (weeks > 0) {
                return weeks + "w ago";
            } else if (days > 0) {
                return days + "d ago";
            } else if (hours > 0) {
                return hours + "h ago";
            } else if (minutes > 0) {
                return minutes + "m ago";
            } else {
                return "now";
            }
        }
    }
}
