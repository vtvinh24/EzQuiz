package dev.vtvinh24.ezquiz.ui;

import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;

public class QuizReviewAdapter extends RecyclerView.Adapter<QuizReviewAdapter.ViewHolder> {
    private final List<QuizEntity> quizzes;
    private OnItemLongClickListener longClickListener;

    public QuizReviewAdapter(List<QuizEntity> quizzes) {
        this.quizzes = quizzes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        QuizEntity quiz = quizzes.get(position);

        // Set question with numbering
        holder.textQuestion.setText((position + 1) + ". " + quiz.question);

        // Clear previous answers
        holder.answersContainer.removeAllViews();

        // Display all answers
        for (int i = 0; i < quiz.answers.size(); i++) {
            TextView answerView = new TextView(holder.itemView.getContext());
            String answerText = getAnswerPrefix(i) + " " + quiz.answers.get(i);
            answerView.setText(answerText);
            answerView.setTextSize(16);
            answerView.setPadding(16, 8, 16, 8);

            // Highlight correct answers
            if (quiz.correctAnswerIndices.contains(i)) {
                answerView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gradient_green_start));
                answerView.setTypeface(null, Typeface.BOLD);
                answerView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.gradient_green_start_alpha));
            } else {
                answerView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.colorOnSurface));
                answerView.setTypeface(null, Typeface.NORMAL);
            }

            holder.answersContainer.addView(answerView);
        }

        // Display correct answer summary
        StringBuilder correctAnswersText = new StringBuilder("Đáp án đúng: ");
        for (int i = 0; i < quiz.correctAnswerIndices.size(); i++) {
            int correctIndex = quiz.correctAnswerIndices.get(i);
            correctAnswersText.append(getAnswerPrefix(correctIndex));
            if (i < quiz.correctAnswerIndices.size() - 1) {
                correctAnswersText.append(", ");
            }
        }
        holder.textCorrectAnswers.setText(correctAnswersText.toString());

        // Set long click listener for edit/delete
        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                longClickListener.onItemLongClick(quiz);
                return true;
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return quizzes.size();
    }

    private String getAnswerPrefix(int index) {
        return String.valueOf((char) ('A' + index));
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(QuizEntity quiz);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textQuestion;
        LinearLayout answersContainer;
        TextView textCorrectAnswers;

        ViewHolder(View itemView) {
            super(itemView);
            textQuestion = itemView.findViewById(R.id.textQuestion);
            answersContainer = itemView.findViewById(R.id.answersContainer);
            textCorrectAnswers = itemView.findViewById(R.id.textCorrectAnswers);
        }
    }
}
