package dev.vtvinh24.ezquiz.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
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
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return historyItems.size();
    }

    public void setHistoryItems(List<HistoryItem> items) {
        this.historyItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextView quizSetNameText;
        private final TextView collectionNameText;
        private final TextView statusText;
        private final TextView lastUpdatedText;
        private final ProgressBar progressBar;
        private final TextView progressText;
        private final ImageView statusIcon;
        private final MaterialButton actionButton;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_history_item);
            quizSetNameText = itemView.findViewById(R.id.text_quiz_set_name);
            collectionNameText = itemView.findViewById(R.id.text_collection_name);
            statusText = itemView.findViewById(R.id.text_status);
            lastUpdatedText = itemView.findViewById(R.id.text_last_updated);
            progressBar = itemView.findViewById(R.id.progress_bar);
            progressText = itemView.findViewById(R.id.text_progress);
            statusIcon = itemView.findViewById(R.id.icon_status);
            actionButton = itemView.findViewById(R.id.button_action);
        }

        public void bind(HistoryItem item) {
            quizSetNameText.setText(item.getQuizSet().name);
            collectionNameText.setText(item.getCollectionName() + (item.isAIGenerated() ? " • AI Generated" : ""));
            statusText.setText(item.getStatusText());
            lastUpdatedText.setText(dateFormat.format(new Date(item.getLastUpdated())));

            // Hiển thị thống kê chi tiết thay vì chỉ progress cơ bản
            String detailedStats = item.getDetailedStats();
            if (!detailedStats.equals("No attempts yet")) {
                progressText.setText(detailedStats);
                progressBar.setVisibility(View.GONE);
            } else {
                progressBar.setMax(item.getTotalQuestions());
                progressBar.setProgress(item.getCompletedQuestions());
                progressText.setText(item.getCompletedQuestions() + "/" + item.getTotalQuestions());
                progressBar.setVisibility(View.VISIBLE);
            }

            if (item.isCompleted()) {
                statusIcon.setImageResource(R.drawable.ic_check_circle);
                statusIcon.setColorFilter(context.getColor(R.color.correct_answer));
                actionButton.setText("View Results");
                actionButton.setOnClickListener(v -> viewResults(item));
            } else if (item.canContinue()) {
                statusIcon.setImageResource(R.drawable.ic_play_circle);
                statusIcon.setColorFilter(context.getColor(R.color.practice_title));
                actionButton.setText("Continue");
                actionButton.setOnClickListener(v -> continueQuiz(item));
            } else {
                statusIcon.setImageResource(R.drawable.ic_quiz);
                statusIcon.setColorFilter(context.getColor(R.color.text_secondary));
                actionButton.setText("Start Quiz");
                actionButton.setOnClickListener(v -> startQuiz(item));
            }

            cardView.setOnClickListener(v -> {
                if (item.isCompleted()) {
                    viewResults(item);
                } else {
                    continueQuiz(item);
                }
            });
        }

        private void continueQuiz(HistoryItem item) {
            Intent intent = new Intent(context, PracticeActivity.class);
            intent.putExtra("quizSetId", item.getQuizSet().id);
            intent.putExtra("continueSession", true);
            context.startActivity(intent);
        }

        private void startQuiz(HistoryItem item) {
            Intent intent = new Intent(context, PracticeActivity.class);
            intent.putExtra("quizSetId", item.getQuizSet().id);
            intent.putExtra("continueSession", false);
            context.startActivity(intent);
        }

        private void viewResults(HistoryItem item) {
            Intent intent = new Intent(context, TestResultActivity.class);
            intent.putExtra("quizSetId", item.getQuizSet().id);
            intent.putExtra("sessionResults", item.getSessionResults());
            context.startActivity(intent);
        }
    }
}
