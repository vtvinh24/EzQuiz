package dev.vtvinh24.ezquiz.ui.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.repository.InMemoryQuizRepository;
import dev.vtvinh24.ezquiz.domain.model.Answer;
import dev.vtvinh24.ezquiz.domain.model.Question;
import dev.vtvinh24.ezquiz.domain.model.Quiz;

/**
 * Fragment for creating or editing a question within a quiz.
 */
public class EditQuestionFragment extends Fragment {

    private EditorViewModel viewModel;
    private Quiz currentQuiz;
    private Question currentQuestion;
    private int questionIndex = -1;

    private TextInputEditText questionTextInput;
    private Spinner questionTypeSpinner;
    private TextInputEditText pointsInput;
    private RecyclerView answersRecyclerView;
    private Button addAnswerButton;
    private Button saveQuestionButton;
    private AnswersAdapter answersAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get question index from arguments
        if (getArguments() != null) {
            questionIndex = getArguments().getInt("question_index", -1);
        }

        // Initialize ViewModel
        // Note: In a real app, this would be injected with Hilt or shared with parent
        viewModel = new EditorViewModel(new InMemoryQuizRepository());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_question, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        questionTextInput = view.findViewById(R.id.question_text_input);
        questionTypeSpinner = view.findViewById(R.id.question_type_spinner);
        pointsInput = view.findViewById(R.id.points_input);
        answersRecyclerView = view.findViewById(R.id.answers_recycler_view);
        addAnswerButton = view.findViewById(R.id.add_answer_button);
        saveQuestionButton = view.findViewById(R.id.save_question_button);

        // Setup question type spinner
        ArrayAdapter<CharSequence> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                new String[] {"SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE", "TEXT_INPUT"}
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        questionTypeSpinner.setAdapter(adapter);

        // Setup answers RecyclerView
        answersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        answersAdapter = new AnswersAdapter(new ArrayList<>());
        answersRecyclerView.setAdapter(answersAdapter);

        // Set click listeners
        addAnswerButton.setOnClickListener(v -> addNewAnswer());
        saveQuestionButton.setOnClickListener(v -> saveQuestion());

        // Observe current quiz
        viewModel.getCurrentQuiz().observe(getViewLifecycleOwner(), quiz -> {
            currentQuiz = quiz;

            if (quiz != null && quiz.getQuestions() != null) {
                // If editing existing question
                if (questionIndex >= 0 && questionIndex < quiz.getQuestions().size()) {
                    currentQuestion = quiz.getQuestions().get(questionIndex);
                    populateQuestionData();
                }
            }
        });
    }

    private void populateQuestionData() {
        if (currentQuestion == null) return;

        // Set question data to UI
        questionTextInput.setText(currentQuestion.getText());

        // Set question type
        String[] questionTypes = {"SINGLE_CHOICE", "MULTIPLE_CHOICE", "TRUE_FALSE", "TEXT_INPUT"};
        for (int i = 0; i < questionTypes.length; i++) {
            if (questionTypes[i].equals(currentQuestion.getType().name())) {
                questionTypeSpinner.setSelection(i);
                break;
            }
        }

        // Set points
        pointsInput.setText(String.valueOf(currentQuestion.getPoints()));

        // Set answers
        if (currentQuestion.getAnswers() != null) {
            answersAdapter.updateAnswers(currentQuestion.getAnswers());
        }
    }

    private void addNewAnswer() {
        // Create a new empty answer
        Answer answer = new Answer("New Answer", false);

        // Add to adapter
        List<Answer> answers = new ArrayList<>(answersAdapter.getAnswers());
        answers.add(answer);
        answersAdapter.updateAnswers(answers);
    }

    private void saveQuestion() {
        // Validate input
        String questionText = questionTextInput.getText() != null ?
                questionTextInput.getText().toString().trim() : "";

        if (questionText.isEmpty()) {
            Toast.makeText(requireContext(), "Question text cannot be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String pointsText = pointsInput.getText() != null ?
                pointsInput.getText().toString().trim() : "0";

        int points;
        try {
            points = Integer.parseInt(pointsText);
            if (points <= 0) {
                Toast.makeText(requireContext(), "Points must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Invalid points value", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get question type
        Question.QuestionType questionType = Question.QuestionType.valueOf(
                questionTypeSpinner.getSelectedItem().toString());

        // Validate answers based on question type
        List<Answer> answers = answersAdapter.getAnswers();

        if (answers.isEmpty()) {
            Toast.makeText(requireContext(), "Add at least one answer", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean hasCorrectAnswer = false;
        for (Answer answer : answers) {
            if (answer.isCorrect()) {
                hasCorrectAnswer = true;
                break;
            }
        }

        if (!hasCorrectAnswer) {
            Toast.makeText(requireContext(), "At least one answer must be correct", Toast.LENGTH_SHORT).show();
            return;
        }

        if (questionType == Question.QuestionType.SINGLE_CHOICE) {
            int correctCount = 0;
            for (Answer answer : answers) {
                if (answer.isCorrect()) {
                    correctCount++;
                }
            }

            if (correctCount != 1) {
                Toast.makeText(requireContext(),
                        "Single choice questions must have exactly one correct answer",
                        Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create or update question
        if (currentQuestion == null) {
            currentQuestion = new Question();
        }

        currentQuestion.setText(questionText);
        currentQuestion.setType(questionType);
        currentQuestion.setPoints(points);
        currentQuestion.setAnswers(answers);

        // Add or update question in quiz
        if (currentQuiz != null) {
            if (currentQuiz.getQuestions() == null) {
                currentQuiz.setQuestions(new ArrayList<>());
            }

            if (questionIndex >= 0 && questionIndex < currentQuiz.getQuestions().size()) {
                // Update existing question
                currentQuiz.getQuestions().set(questionIndex, currentQuestion);
            } else {
                // Add new question
                currentQuiz.getQuestions().add(currentQuestion);
            }

            viewModel.saveQuiz();
            Toast.makeText(requireContext(), "Question saved", Toast.LENGTH_SHORT).show();

            // Navigate back to questions list
            Navigation.findNavController(requireView()).navigateUp();
        }
    }

    /**
     * Adapter for answers list
     */
    private class AnswersAdapter extends RecyclerView.Adapter<AnswersAdapter.AnswerViewHolder> {

        private List<Answer> answers;

        public AnswersAdapter(List<Answer> answers) {
            this.answers = answers;
        }

        public void updateAnswers(List<Answer> answers) {
            this.answers = answers;
            notifyDataSetChanged();
        }

        public List<Answer> getAnswers() {
            return answers;
        }

        @NonNull
        @Override
        public AnswerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_answer, parent, false);
            return new AnswerViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AnswerViewHolder holder, int position) {
            Answer answer = answers.get(position);
            holder.bind(answer, position);
        }

        @Override
        public int getItemCount() {
            return answers.size();
        }

        class AnswerViewHolder extends RecyclerView.ViewHolder {
            private final TextInputLayout answerTextLayout;
            private final TextInputEditText answerTextInput;
            private final SwitchMaterial correctSwitch;
            private final MaterialButton deleteButton;

            public AnswerViewHolder(@NonNull View itemView) {
                super(itemView);
                answerTextLayout = itemView.findViewById(R.id.answer_text_layout);
                answerTextInput = itemView.findViewById(R.id.answer_text_input);
                correctSwitch = itemView.findViewById(R.id.correct_switch);
                deleteButton = itemView.findViewById(R.id.delete_button);
            }

            public void bind(final Answer answer, final int position) {
                answerTextInput.setText(answer.getText());
                correctSwitch.setChecked(answer.isCorrect());

                // Update answer text on change
                answerTextInput.setOnFocusChangeListener((v, hasFocus) -> {
                    if (!hasFocus && answerTextInput.getText() != null) {
                        String newText = answerTextInput.getText().toString().trim();
                        if (!newText.equals(answer.getText())) {
                            answer.setText(newText);
                        }
                    }
                });

                // Update correct status on change
                correctSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked != answer.isCorrect()) {
                        answer.setCorrect(isChecked);
                    }
                });

                // Handle delete
                deleteButton.setOnClickListener(v -> {
                    answers.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, answers.size() - position);
                });
            }
        }
    }
}
