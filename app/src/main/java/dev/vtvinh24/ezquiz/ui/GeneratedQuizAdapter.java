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
        View view = LayoutInflater.from(context).inflate(R.layout.item_generated_quiz, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GeneratedQuizItem item = quizItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return quizItems != null ? quizItems.size() : 0;
    }

    public List<GeneratedQuizItem> getUpdatedQuizItems() {
        return quizItems;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView questionNumber;
        private final Chip chipQuestionType;
        private final MaterialButton btnEditQuestion;
        private final TextInputEditText editQuestion;
        private final LinearLayout containerAnswers;
        private final MaterialButton btnAddAnswer;
        private final MaterialSwitch switchMultipleChoice;

        private RadioGroup radioGroup;
        private final List<View> answerViews = new ArrayList<>();
        private GeneratedQuizItem currentItem;
        private boolean isMultipleChoice = false;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionNumber = itemView.findViewById(R.id.text_question_number);
            chipQuestionType = itemView.findViewById(R.id.chip_question_type);
            btnEditQuestion = itemView.findViewById(R.id.btn_edit_question);
            editQuestion = itemView.findViewById(R.id.edit_question);
            containerAnswers = itemView.findViewById(R.id.container_answers);
            btnAddAnswer = itemView.findViewById(R.id.btn_add_answer);
            switchMultipleChoice = itemView.findViewById(R.id.switch_multiple_choice);

            radioGroup = new RadioGroup(context);
            radioGroup.setOrientation(RadioGroup.VERTICAL);

            setupListeners();
        }

        private void setupListeners() {
            // Question text change listener
            editQuestion.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (currentItem != null) {
                        currentItem.question = s.toString();
                    }
                }
            });

            // Multiple choice toggle
            switchMultipleChoice.setOnCheckedChangeListener((buttonView, isChecked) -> {
                isMultipleChoice = isChecked;
                currentItem.type = isChecked ? Quiz.Type.MULTIPLE_CHOICE : Quiz.Type.SINGLE_CHOICE;
                chipQuestionType.setText(isChecked ? "Multiple Choice" : "Single Choice");
                updateAnswerSelectionMode();
            });

            // Add answer button
            btnAddAnswer.setOnClickListener(v -> addNewAnswer());

            // Edit question toggle
            btnEditQuestion.setOnClickListener(v -> {
                boolean isEnabled = !editQuestion.isEnabled();
                editQuestion.setEnabled(isEnabled);
                btnEditQuestion.setText(isEnabled ? "Save" : "Edit");
                btnEditQuestion.setIconResource(isEnabled ? R.drawable.ic_save : R.drawable.ic_edit);
            });
        }

        void bind(GeneratedQuizItem item, int position) {
            currentItem = item;

            // Set question number
            questionNumber.setText(String.valueOf(position + 1));

            // Set question text
            editQuestion.setText(item.question);
            editQuestion.setEnabled(false);

            // Set question type
            isMultipleChoice = item.type == Quiz.Type.MULTIPLE_CHOICE;
            switchMultipleChoice.setChecked(isMultipleChoice);
            chipQuestionType.setText(isMultipleChoice ? "Multiple Choice" : "Single Choice");

            // Clear previous answers
            containerAnswers.removeAllViews();
            answerViews.clear();

            // Add answer options
            if (item.answers != null) {
                for (int i = 0; i < item.answers.size(); i++) {
                    addAnswerOption(item.answers.get(i), i);
                }
            }

            // Set correct answers
            updateCorrectAnswers();
        }

        private void addAnswerOption(String answerText, int index) {
            View answerView = LayoutInflater.from(context).inflate(R.layout.item_answer_option, containerAnswers, false);

            RadioButton radioButton = answerView.findViewById(R.id.radio_answer);
            CheckBox checkBox = answerView.findViewById(R.id.checkbox_answer);
            TextInputEditText editAnswerText = answerView.findViewById(R.id.edit_answer_text);
            MaterialButton btnRemove = answerView.findViewById(R.id.btn_remove_answer);

            // Set answer text
            editAnswerText.setText(answerText);

            // Setup answer text change listener
            editAnswerText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    updateAnswerInList(answerView, s.toString());
                }
            });

            // Setup selection listeners
            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked && !isMultipleChoice) {
                    updateSingleChoiceSelection(answerView);
                }
            });

            checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isMultipleChoice) {
                    updateMultipleChoiceSelection(answerView, isChecked);
                }
            });

            // Setup remove button
            btnRemove.setOnClickListener(v -> removeAnswerOption(answerView));

            // Set selection mode
            if (isMultipleChoice) {
                radioButton.setVisibility(View.GONE);
                checkBox.setVisibility(View.VISIBLE);
            } else {
                radioButton.setVisibility(View.VISIBLE);
                checkBox.setVisibility(View.GONE);
            }

            // Add to container and track
            containerAnswers.addView(answerView);
            answerViews.add(answerView);
        }

        private void addNewAnswer() {
            String newAnswerText = "New option " + (answerViews.size() + 1);

            // Add to data model
            if (currentItem.answers == null) {
                currentItem.answers = new ArrayList<>();
            }
            currentItem.answers.add(newAnswerText);

            // Add to UI
            addAnswerOption(newAnswerText, answerViews.size());
        }

        private void removeAnswerOption(View answerView) {
            if (answerViews.size() <= 2) {
                // Don't allow removal if only 2 answers left
                return;
            }

            int index = answerViews.indexOf(answerView);
            if (index != -1 && currentItem.answers != null) {
                // Remove from data model
                currentItem.answers.remove(index);

                // Update correct answer indices
                if (currentItem.correctAnswerIndices != null) {
                    List<Integer> newCorrectIndices = new ArrayList<>();
                    for (Integer correctIndex : currentItem.correctAnswerIndices) {
                        if (correctIndex < index) {
                            newCorrectIndices.add(correctIndex);
                        } else if (correctIndex > index) {
                            newCorrectIndices.add(correctIndex - 1);
                        }
                    }
                    currentItem.correctAnswerIndices = newCorrectIndices;
                }

                // Remove from UI
                containerAnswers.removeView(answerView);
                answerViews.remove(answerView);
            }
        }

        private void updateAnswerInList(View answerView, String newText) {
            int index = answerViews.indexOf(answerView);
            if (index != -1 && currentItem.answers != null && index < currentItem.answers.size()) {
                currentItem.answers.set(index, newText);
            }
        }

        private void updateSingleChoiceSelection(View selectedView) {
            // Clear all other selections
            for (View answerView : answerViews) {
                if (answerView != selectedView) {
                    RadioButton radio = answerView.findViewById(R.id.radio_answer);
                    radio.setChecked(false);
                }
            }

            // Update correct answer index
            int selectedIndex = answerViews.indexOf(selectedView);
            if (selectedIndex != -1) {
                currentItem.correctAnswerIndices = new ArrayList<>();
                currentItem.correctAnswerIndices.add(selectedIndex);
            }
        }

        private void updateMultipleChoiceSelection(View answerView, boolean isChecked) {
            int index = answerViews.indexOf(answerView);
            if (index == -1) return;

            if (currentItem.correctAnswerIndices == null) {
                currentItem.correctAnswerIndices = new ArrayList<>();
            }

            if (isChecked) {
                if (!currentItem.correctAnswerIndices.contains(index)) {
                    currentItem.correctAnswerIndices.add(index);
                }
            } else {
                currentItem.correctAnswerIndices.remove(Integer.valueOf(index));
            }
        }

        private void updateAnswerSelectionMode() {
            for (View answerView : answerViews) {
                RadioButton radioButton = answerView.findViewById(R.id.radio_answer);
                CheckBox checkBox = answerView.findViewById(R.id.checkbox_answer);

                if (isMultipleChoice) {
                    radioButton.setVisibility(View.GONE);
                    radioButton.setChecked(false);
                    checkBox.setVisibility(View.VISIBLE);
                } else {
                    radioButton.setVisibility(View.VISIBLE);
                    checkBox.setVisibility(View.GONE);
                    checkBox.setChecked(false);
                }
            }

            // Clear and reset correct answers
            if (currentItem.correctAnswerIndices != null) {
                if (!isMultipleChoice && currentItem.correctAnswerIndices.size() > 1) {
                    // Convert to single choice - keep only first selection
                    List<Integer> newIndices = new ArrayList<>();
                    newIndices.add(currentItem.correctAnswerIndices.get(0));
                    currentItem.correctAnswerIndices = newIndices;
                }
            }

            updateCorrectAnswers();
        }

        private void updateCorrectAnswers() {
            if (currentItem.correctAnswerIndices == null) return;

            for (int i = 0; i < answerViews.size(); i++) {
                View answerView = answerViews.get(i);
                RadioButton radioButton = answerView.findViewById(R.id.radio_answer);
                CheckBox checkBox = answerView.findViewById(R.id.checkbox_answer);

                boolean isCorrect = currentItem.correctAnswerIndices.contains(i);

                if (isMultipleChoice) {
                    checkBox.setChecked(isCorrect);
                } else {
                    radioButton.setChecked(isCorrect);
                }
            }
        }
    }
}