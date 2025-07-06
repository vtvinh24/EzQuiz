package dev.vtvinh24.ezquiz.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.GeneratedQuizItem;
import dev.vtvinh24.ezquiz.data.model.Quiz;

public class GeneratedQuizAdapter extends RecyclerView.Adapter<GeneratedQuizAdapter.ViewHolder> {
    private final List<GeneratedQuizItem> quizItems;
    private final Context context;

    public GeneratedQuizAdapter(Context context, List<GeneratedQuizItem> quizItems) {
        this.context = context;
        this.quizItems = quizItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_edit_single_choice, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GeneratedQuizItem item = quizItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return quizItems != null ? quizItems.size() : 0;
    }

    public List<GeneratedQuizItem> getUpdatedQuizItems() {
        return quizItems;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final TextInputEditText editQuestionText;
        private final TextInputEditText editOptionA;
        private final TextInputEditText editOptionB;
        private final TextInputEditText editOptionC;
        private final TextInputEditText editOptionD;
        private final CheckBox checkboxOptionA;
        private final CheckBox checkboxOptionB;
        private final CheckBox checkboxOptionC;
        private final CheckBox checkboxOptionD;
        private final MaterialButton btnDeleteQuestion;
        private final MaterialButton btnChangeType;
        private final LinearLayout layoutAnswerOptions;

        private GeneratedQuizItem currentItem;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            editQuestionText = itemView.findViewById(R.id.edit_question_text);
            editOptionA = itemView.findViewById(R.id.edit_option_a);
            editOptionB = itemView.findViewById(R.id.edit_option_b);
            editOptionC = itemView.findViewById(R.id.edit_option_c);
            editOptionD = itemView.findViewById(R.id.edit_option_d);
            checkboxOptionA = itemView.findViewById(R.id.checkbox_option_a);
            checkboxOptionB = itemView.findViewById(R.id.checkbox_option_b);
            checkboxOptionC = itemView.findViewById(R.id.checkbox_option_c);
            checkboxOptionD = itemView.findViewById(R.id.checkbox_option_d);
            btnDeleteQuestion = itemView.findViewById(R.id.btn_delete_question);
            btnChangeType = itemView.findViewById(R.id.btn_change_type);
            layoutAnswerOptions = itemView.findViewById(R.id.layout_answer_options);
        }

        void bind(GeneratedQuizItem item) {
            currentItem = item;

            if (editQuestionText != null) {
                editQuestionText.setText(item.question);
            }

            // Set answer options if they exist
            if (item.answers != null && !item.answers.isEmpty()) {
                if (editOptionA != null && item.answers.size() > 0) {
                    editOptionA.setText(item.answers.get(0));
                }
                if (editOptionB != null && item.answers.size() > 1) {
                    editOptionB.setText(item.answers.get(1));
                }
                if (editOptionC != null && item.answers.size() > 2) {
                    editOptionC.setText(item.answers.get(2));
                }
                if (editOptionD != null && item.answers.size() > 3) {
                    editOptionD.setText(item.answers.get(3));
                }
            }

            // Set correct answer indicators
            if (item.correctAnswerIndices != null) {
                setCorrectAnswerCheckboxes(item.correctAnswerIndices);
            }
        }

        private void setCorrectAnswerCheckboxes(List<Integer> correctAnswerIndices) {
            // Reset all checkboxes
            if (checkboxOptionA != null) checkboxOptionA.setChecked(false);
            if (checkboxOptionB != null) checkboxOptionB.setChecked(false);
            if (checkboxOptionC != null) checkboxOptionC.setChecked(false);
            if (checkboxOptionD != null) checkboxOptionD.setChecked(false);

            // Set the correct answers based on indices
            for (Integer index : correctAnswerIndices) {
                switch (index) {
                    case 0:
                        if (checkboxOptionA != null) checkboxOptionA.setChecked(true);
                        break;
                    case 1:
                        if (checkboxOptionB != null) checkboxOptionB.setChecked(true);
                        break;
                    case 2:
                        if (checkboxOptionC != null) checkboxOptionC.setChecked(true);
                        break;
                    case 3:
                        if (checkboxOptionD != null) checkboxOptionD.setChecked(true);
                        break;
                }
            }
        }
    }
}