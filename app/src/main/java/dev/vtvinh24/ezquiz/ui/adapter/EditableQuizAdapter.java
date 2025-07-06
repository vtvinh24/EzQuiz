package dev.vtvinh24.ezquiz.ui.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.EditableQuiz;
import dev.vtvinh24.ezquiz.data.model.GeneratedQuizItem;
import dev.vtvinh24.ezquiz.data.model.Quiz;

/**
 * Enhanced adapter supporting comprehensive quiz editing with Material Design 3
 */
public class EditableQuizAdapter extends RecyclerView.Adapter<EditableQuizAdapter.QuizViewHolder> {

    private final List<EditableQuiz> quizItems;
    private final Context context;
    private OnQuizChangeListener changeListener;
    private ItemTouchHelper itemTouchHelper;

    public interface OnQuizChangeListener {
        void onQuizChanged();
        void onQuizDeleted(int position);
        void onQuizAdded(int position);
    }

    public EditableQuizAdapter(Context context, List<GeneratedQuizItem> generatedItems) {
        this.context = context;
        this.quizItems = new ArrayList<>();

        // Convert GeneratedQuizItems to EditableQuiz
        for (GeneratedQuizItem item : generatedItems) {
            quizItems.add(new EditableQuiz(item));
        }
    }

    public void setOnQuizChangeListener(OnQuizChangeListener listener) {
        this.changeListener = listener;
    }

    public void setItemTouchHelper(ItemTouchHelper helper) {
        this.itemTouchHelper = helper;
    }

    @NonNull
    @Override
    public QuizViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_editable_quiz, parent, false);
        return new QuizViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuizViewHolder holder, int position) {
        EditableQuiz quiz = quizItems.get(position);
        holder.bind(quiz, position);
    }

    @Override
    public int getItemCount() {
        return quizItems.size();
    }

    public List<GeneratedQuizItem> getUpdatedQuizItems() {
        List<GeneratedQuizItem> items = new ArrayList<>();
        for (EditableQuiz quiz : quizItems) {
            if (quiz.isValid()) {
                items.add(quiz.toGeneratedQuizItem());
            }
        }
        return items;
    }

    public void addNewQuiz(int position) {
        EditableQuiz newQuiz = new EditableQuiz();
        newQuiz.setQuestion("New Question");
        newQuiz.addAnswer("Option 1");
        newQuiz.addAnswer("Option 2");
        newQuiz.addCorrectAnswerIndex(0);

        quizItems.add(position, newQuiz);
        notifyItemInserted(position);

        if (changeListener != null) {
            changeListener.onQuizAdded(position);
        }
    }

    public void removeQuiz(int position) {
        if (position >= 0 && position < quizItems.size()) {
            quizItems.remove(position);
            notifyItemRemoved(position);

            if (changeListener != null) {
                changeListener.onQuizDeleted(position);
            }
        }
    }

    public void moveQuiz(int fromPosition, int toPosition) {
        Collections.swap(quizItems, fromPosition, toPosition);
        notifyItemMoved(fromPosition, toPosition);

        if (changeListener != null) {
            changeListener.onQuizChanged();
        }
    }

    class QuizViewHolder extends RecyclerView.ViewHolder {
        private final MaterialCardView cardView;
        private final TextInputEditText editQuestion;
        private final TextInputLayout layoutQuestionType;
        private final MaterialAutoCompleteTextView spinnerQuestionType;
        private final RecyclerView recyclerAnswers;
        private final MaterialButton btnAddAnswer;
        private final MaterialButton btnDeleteQuiz;
        private final MaterialButton btnMoveUp;
        private final MaterialButton btnMoveDown;
        private final Chip chipQuestionNumber;

        private AnswerOptionsAdapter answerAdapter;
        private EditableQuiz currentQuiz;
        private int currentPosition;

        public QuizViewHolder(@NonNull View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_quiz);
            editQuestion = itemView.findViewById(R.id.edit_question);
            layoutQuestionType = itemView.findViewById(R.id.layout_question_type);
            spinnerQuestionType = itemView.findViewById(R.id.spinner_question_type);
            recyclerAnswers = itemView.findViewById(R.id.recycler_answers);
            btnAddAnswer = itemView.findViewById(R.id.btn_add_answer);
            btnDeleteQuiz = itemView.findViewById(R.id.btn_delete_quiz);
            btnMoveUp = itemView.findViewById(R.id.btn_move_up);
            btnMoveDown = itemView.findViewById(R.id.btn_move_down);
            chipQuestionNumber = itemView.findViewById(R.id.chip_question_number);

            setupQuestionTypeSpinner();
            setupRecyclerView();
            setupClickListeners();
        }

        private void setupQuestionTypeSpinner() {
            String[] types = {"Single Choice", "Multiple Choice", "Flashcard"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                android.R.layout.simple_dropdown_item_1line, types);
            spinnerQuestionType.setAdapter(adapter);
        }

        private void setupRecyclerView() {
            recyclerAnswers.setLayoutManager(new LinearLayoutManager(context));
        }

        private void setupClickListeners() {
            btnAddAnswer.setOnClickListener(v -> addNewAnswer());
            btnDeleteQuiz.setOnClickListener(v -> showDeleteConfirmation());
            btnMoveUp.setOnClickListener(v -> moveQuizUp());
            btnMoveDown.setOnClickListener(v -> moveQuizDown());

            // Question text change listener
            editQuestion.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override
                public void afterTextChanged(Editable s) {
                    if (currentQuiz != null) {
                        currentQuiz.setQuestion(s.toString());
                        notifyChangeListener();
                    }
                }
            });

            // Question type change listener
            spinnerQuestionType.setOnItemClickListener((parent, view, position, id) -> {
                if (currentQuiz != null) {
                    Quiz.Type newType = getQuizTypeFromPosition(position);
                    if (newType != currentQuiz.getType()) {
                        showTypeChangeConfirmation(newType);
                    }
                }
            });
        }

        public void bind(EditableQuiz quiz, int position) {
            this.currentQuiz = quiz;
            this.currentPosition = position;

            // Update question number
            chipQuestionNumber.setText("Q" + (position + 1));

            // Set question text
            editQuestion.setText(quiz.getQuestion());

            // Set question type
            spinnerQuestionType.setText(getTypeDisplayName(quiz.getType()), false);

            // Setup answer options
            setupAnswerOptions(quiz);

            // Update move buttons visibility
            btnMoveUp.setVisibility(position > 0 ? View.VISIBLE : View.GONE);
            btnMoveDown.setVisibility(position < getItemCount() - 1 ? View.VISIBLE : View.GONE);

            // Show/hide answer section based on type
            boolean showAnswers = quiz.getType() != Quiz.Type.FLASHCARD;
            recyclerAnswers.setVisibility(showAnswers ? View.VISIBLE : View.GONE);
            btnAddAnswer.setVisibility(showAnswers ? View.VISIBLE : View.GONE);
        }

        private void setupAnswerOptions(EditableQuiz quiz) {
            if (quiz.getType() == Quiz.Type.FLASHCARD) {
                return; // No answers for flashcards
            }

            answerAdapter = new AnswerOptionsAdapter(context, quiz,
                () -> notifyChangeListener());
            recyclerAnswers.setAdapter(answerAdapter);
        }

        private void addNewAnswer() {
            if (currentQuiz != null && currentQuiz.getType() != Quiz.Type.FLASHCARD) {
                currentQuiz.addAnswer("New Option");
                answerAdapter.notifyItemInserted(currentQuiz.getAnswers().size() - 1);
                notifyChangeListener();
            }
        }

        private void showDeleteConfirmation() {
            new MaterialAlertDialogBuilder(context)
                .setTitle("Delete Question")
                .setMessage("Are you sure you want to delete this question?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    removeQuiz(currentPosition);
                })
                .setNegativeButton("Cancel", null)
                .show();
        }

        private void showTypeChangeConfirmation(Quiz.Type newType) {
            String message = getTypeChangeMessage(currentQuiz.getType(), newType);

            new MaterialAlertDialogBuilder(context)
                .setTitle("Change Question Type")
                .setMessage(message)
                .setPositiveButton("Change", (dialog, which) -> {
                    currentQuiz.setType(newType);
                    setupAnswerOptions(currentQuiz);
                    bind(currentQuiz, currentPosition); // Rebind to update UI
                    notifyChangeListener();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Reset spinner to current type
                    spinnerQuestionType.setText(getTypeDisplayName(currentQuiz.getType()), false);
                })
                .show();
        }

        private void moveQuizUp() {
            if (currentPosition > 0) {
                moveQuiz(currentPosition, currentPosition - 1);
            }
        }

        private void moveQuizDown() {
            if (currentPosition < getItemCount() - 1) {
                moveQuiz(currentPosition, currentPosition + 1);
            }
        }

        private Quiz.Type getQuizTypeFromPosition(int position) {
            switch (position) {
                case 0: return Quiz.Type.SINGLE_CHOICE;
                case 1: return Quiz.Type.MULTIPLE_CHOICE;
                case 2: return Quiz.Type.FLASHCARD;
                default: return Quiz.Type.SINGLE_CHOICE;
            }
        }

        private String getTypeDisplayName(Quiz.Type type) {
            switch (type) {
                case SINGLE_CHOICE: return "Single Choice";
                case MULTIPLE_CHOICE: return "Multiple Choice";
                case FLASHCARD: return "Flashcard";
                default: return "Single Choice";
            }
        }

        private String getTypeChangeMessage(Quiz.Type oldType, Quiz.Type newType) {
            if (newType == Quiz.Type.FLASHCARD) {
                return "Changing to Flashcard will remove all answer options. Continue?";
            } else if (oldType == Quiz.Type.MULTIPLE_CHOICE && newType == Quiz.Type.SINGLE_CHOICE) {
                return "Changing to Single Choice will keep only the first correct answer. Continue?";
            } else if (oldType == Quiz.Type.FLASHCARD) {
                return "Changing from Flashcard will add default answer options. Continue?";
            }
            return "Change question type?";
        }

        private void notifyChangeListener() {
            if (changeListener != null) {
                changeListener.onQuizChanged();
            }
        }
    }
}
