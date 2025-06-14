package dev.vtvinh24.ezquiz.ui.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.repository.InMemoryQuizRepository;
import dev.vtvinh24.ezquiz.domain.QuizManager;
import dev.vtvinh24.ezquiz.domain.model.Question;
import dev.vtvinh24.ezquiz.domain.model.Quiz;

/**
 * Fragment for managing the list of questions in a quiz.
 */
public class QuestionsListFragment extends Fragment {

    private QuizManager quizManager;
    private EditorViewModel viewModel;
    private Quiz currentQuiz;

    private RecyclerView questionsRecyclerView;
    private TextView emptyStateTextView;
    private FloatingActionButton addQuestionFab;
    private Button saveQuizButton;
    private QuestionsAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize quiz manager
        // Note: In a real app, this would be injected with Hilt
        quizManager = new QuizManager(new InMemoryQuizRepository());

        // In a real app, we'd get the shared ViewModel, but for now we'll simulate
        viewModel = new EditorViewModel(new InMemoryQuizRepository());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_questions_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        questionsRecyclerView = view.findViewById(R.id.questions_recycler_view);
        emptyStateTextView = view.findViewById(R.id.empty_state_text);
        addQuestionFab = view.findViewById(R.id.add_question_fab);
        saveQuizButton = view.findViewById(R.id.save_quiz_button);

        // Setup RecyclerView
        questionsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new QuestionsAdapter(new ArrayList<>());
        questionsRecyclerView.setAdapter(adapter);

        // Set click listeners
        addQuestionFab.setOnClickListener(v -> navigateToEditQuestion(-1)); // -1 indicates new question
        saveQuizButton.setOnClickListener(v -> saveQuiz());

        // Observe quiz data
        viewModel.getCurrentQuiz().observe(getViewLifecycleOwner(), quiz -> {
            currentQuiz = quiz;
            updateUI();
        });
    }

    private void navigateToEditQuestion(int questionIndex) {
        // Navigate to edit question fragment
        NavController navController = Navigation.findNavController(requireView());
        Bundle args = new Bundle();
        args.putInt("question_index", questionIndex);
        navController.navigate(R.id.action_questions_to_edit_question, args);
    }

    private void saveQuiz() {
        if (currentQuiz == null) {
            Toast.makeText(requireContext(), "No quiz to save", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate quiz
        List<String> validationErrors = quizManager.validateQuiz(currentQuiz);
        if (!validationErrors.isEmpty()) {
            // In a real app, we'd show all errors
            Toast.makeText(requireContext(), validationErrors.get(0), Toast.LENGTH_SHORT).show();
            return;
        }

        // Save quiz
        Quiz savedQuiz = quizManager.saveQuiz(currentQuiz);
        if (savedQuiz != null) {
            Toast.makeText(requireContext(), "Quiz saved successfully", Toast.LENGTH_SHORT).show();
            // In a real app, navigate back to collections
        } else {
            Toast.makeText(requireContext(), "Failed to save quiz", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        if (currentQuiz != null && currentQuiz.getQuestions() != null &&
                !currentQuiz.getQuestions().isEmpty()) {
            // Show questions
            emptyStateTextView.setVisibility(View.GONE);
            questionsRecyclerView.setVisibility(View.VISIBLE);
            adapter.updateQuestions(currentQuiz.getQuestions());
        } else {
            // Show empty state
            emptyStateTextView.setVisibility(View.VISIBLE);
            questionsRecyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Adapter for questions list
     */
    private class QuestionsAdapter extends RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder> {

        private List<Question> questions;

        public QuestionsAdapter(List<Question> questions) {
            this.questions = questions;
        }

        public void updateQuestions(List<Question> questions) {
            this.questions = questions;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_question, parent, false);
            return new QuestionViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
            Question question = questions.get(position);
            holder.bind(question, position);
        }

        @Override
        public int getItemCount() {
            return questions.size();
        }

        class QuestionViewHolder extends RecyclerView.ViewHolder {
            private final TextView questionTextView;
            private final TextView questionTypeTextView;
            private final TextView pointsTextView;
            private final Button editButton;

            public QuestionViewHolder(@NonNull View itemView) {
                super(itemView);
                questionTextView = itemView.findViewById(R.id.question_text);
                questionTypeTextView = itemView.findViewById(R.id.question_type);
                pointsTextView = itemView.findViewById(R.id.points_text);
                editButton = itemView.findViewById(R.id.edit_button);
            }

            public void bind(Question question, int position) {
                questionTextView.setText(question.getText());
                questionTypeTextView.setText(question.getType().toString());
                pointsTextView.setText(String.format("Points: %d", question.getPoints()));

                editButton.setOnClickListener(v -> navigateToEditQuestion(position));
            }
        }
    }
}
