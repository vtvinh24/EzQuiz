package dev.vtvinh24.ezquiz.ui.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.EditableQuiz;
import dev.vtvinh24.ezquiz.data.model.Quiz;

/**
 * Adapter for managing answer options within a question
 * Supports both single and multiple choice with dynamic add/remove
 */
public class AnswerOptionsAdapter extends RecyclerView.Adapter<AnswerOptionsAdapter.AnswerViewHolder> {

    private final EditableQuiz quiz;
    private final Context context;
    private final Runnable changeCallback;

    public AnswerOptionsAdapter(Context context, EditableQuiz quiz, Runnable changeCallback) {
        this.context = context;
        this.quiz = quiz;
        this.changeCallback = changeCallback;
    }

    @NonNull
    @Override
    public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_answer_option, parent, false);
        return new AnswerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return quiz.getAnswers().size();
    }

    class AnswerViewHolder extends RecyclerView.ViewHolder {
        private final TextInputEditText editAnswer;
        private final CheckBox checkboxMultiple;
        private final RadioButton radioSingle;
        private final MaterialButton btnDelete;

        private int currentPosition;
        private boolean isBinding = false;

        public AnswerViewHolder(@NonNull View itemView) {
            super(itemView);

            editAnswer = itemView.findViewById(R.id.edit_answer);
            checkboxMultiple = itemView.findViewById(R.id.checkbox_multiple);
            radioSingle = itemView.findViewById(R.id.radio_single);
            btnDelete = itemView.findViewById(R.id.btn_delete_answer);

            setupListeners();
        }

        private void setupListeners() {
            // Answer text change listener
            editAnswer.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (!isBinding && currentPosition >= 0 && currentPosition < quiz.getAnswers().size()) {
                        quiz.updateAnswer(currentPosition, s.toString());
                        notifyChange();
                    }
                }
            });

            // Delete button listener
            btnDelete.setOnClickListener(v -> {
                if (quiz.getAnswers().size() > 2) { // Keep minimum 2 answers
                    quiz.removeAnswer(currentPosition);
                    notifyItemRemoved(currentPosition);
                    notifyItemRangeChanged(currentPosition, getItemCount());
                    notifyChange();
                }
            });

            // Checkbox for multiple choice
            checkboxMultiple.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isBinding) {
                    if (isChecked) {
                        quiz.addCorrectAnswerIndex(currentPosition);
                    } else {
                        quiz.removeCorrectAnswerIndex(currentPosition);
                    }
                    notifyChange();
                }
            });

            // Radio button for single choice
            radioSingle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (!isBinding && isChecked) {
                    quiz.clearCorrectAnswerIndices();
                    quiz.addCorrectAnswerIndex(currentPosition);

                    // Post the update to avoid layout conflicts
                    itemView.post(() -> {
                        if (getBindingAdapter() != null) {
                            notifyItemRangeChanged(0, getItemCount());
                        }
                    });
                    notifyChange();
                }
            });
        }

        public void bind(int position) {
            isBinding = true; // Prevent listener callbacks during binding
            this.currentPosition = position;

            String answerText = quiz.getAnswers().get(position);
            editAnswer.setText(answerText);

            boolean isCorrect = quiz.getCorrectAnswerIndices().contains(position);

            // Show appropriate selection UI based on quiz type
            if (quiz.getType() == Quiz.Type.MULTIPLE_CHOICE) {
                checkboxMultiple.setVisibility(View.VISIBLE);
                radioSingle.setVisibility(View.GONE);
                checkboxMultiple.setChecked(isCorrect);
            } else {
                checkboxMultiple.setVisibility(View.GONE);
                radioSingle.setVisibility(View.VISIBLE);
                radioSingle.setChecked(isCorrect);
            }

            // Enable delete only if more than 2 answers
            btnDelete.setEnabled(quiz.getAnswers().size() > 2);
            btnDelete.setAlpha(quiz.getAnswers().size() > 2 ? 1.0f : 0.5f);

            isBinding = false; // Re-enable listener callbacks
        }

        private void notifyChange() {
            if (changeCallback != null) {
                changeCallback.run();
            }
        }
    }
}
