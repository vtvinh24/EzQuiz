package dev.vtvinh24.ezquiz.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.model.Quiz;

public class QuizAdapter extends RecyclerView.Adapter<QuizAdapter.ViewHolder> {
  private final List<QuizEntity> quizzes;
  private final OnAnswerListener listener;
  private OnItemClickListener itemClickListener;

  public QuizAdapter(List<QuizEntity> quizzes, OnAnswerListener listener) {
    this.quizzes = quizzes;
    this.listener = listener;
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_quiz, parent, false);
    return new ViewHolder(view);
  }

  @Override
  public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
    QuizEntity quiz = quizzes.get(position);
    holder.textQuestion.setText(quiz.question);
    holder.answersContainer.removeAllViews();
    if (quiz.type == dev.vtvinh24.ezquiz.data.model.Quiz.Type.SINGLE_CHOICE) {
      RadioGroup radioGroup = new RadioGroup(holder.itemView.getContext());
      for (int i = 0; i < quiz.answers.size(); i++) {
        RadioButton radioButton = new RadioButton(holder.itemView.getContext());
        radioButton.setText(quiz.answers.get(i));
        int index = i;
        radioButton.setOnClickListener(v -> {
          List<Integer> selected = new ArrayList<>();
          selected.add(index);
          listener.onAnswer(quiz, selected);
        });
        radioGroup.addView(radioButton);
      }
      holder.answersContainer.addView(radioGroup);
    } else if (quiz.type == Quiz.Type.MULTIPLE_CHOICE) {
      Set<Integer> selectedIndices = new HashSet<>();
      for (int i = 0; i < quiz.answers.size(); i++) {
        CheckBox checkBox = new CheckBox(holder.itemView.getContext());
        checkBox.setText(quiz.answers.get(i));
        int index = i;
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
          if (isChecked) selectedIndices.add(index);
          else selectedIndices.remove(index);
          listener.onAnswer(quiz, new ArrayList<>(selectedIndices));
        });
        holder.answersContainer.addView(checkBox);
      }
    } else if (quiz.type == Quiz.Type.FLASHCARD) {
      // For flashcards, just show the answer(s) below the question
      for (String answer : quiz.answers) {
        TextView answerView = new TextView(holder.itemView.getContext());
        answerView.setText(answer);
        answerView.setPadding(16, 8, 16, 8);
        holder.answersContainer.addView(answerView);
      }
    }
    if (itemClickListener != null) {
      holder.itemView.setOnClickListener(v -> itemClickListener.onItemClick(quiz));
    }
  }

  @Override
  public int getItemCount() {
    return quizzes.size();
  }

  public void setOnItemClickListener(OnItemClickListener listener) {
    this.itemClickListener = listener;
  }

  public interface OnAnswerListener {
    void onAnswer(QuizEntity quiz, List<Integer> selectedIndices);
  }

  public interface OnItemClickListener {
    void onItemClick(QuizEntity quiz);
  }

  static class ViewHolder extends RecyclerView.ViewHolder {
    TextView textQuestion;
    LinearLayout answersContainer;

    ViewHolder(View itemView) {
      super(itemView);
      textQuestion = itemView.findViewById(R.id.textQuestion);
      answersContainer = itemView.findViewById(R.id.answersContainer);
    }
  }
}
