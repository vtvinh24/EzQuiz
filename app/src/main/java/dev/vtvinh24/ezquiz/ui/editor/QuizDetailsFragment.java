package dev.vtvinh24.ezquiz.ui.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.repository.InMemoryQuizRepository;
import dev.vtvinh24.ezquiz.domain.QuizManager;
import dev.vtvinh24.ezquiz.domain.model.Quiz;

/**
 * Fragment for editing basic quiz details like title and description.
 */
public class QuizDetailsFragment extends Fragment {

    private QuizManager quizManager;
    private String quizId;
    private Quiz currentQuiz;

    private EditText titleEditText;
    private EditText descriptionEditText;
    private Button nextButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize quiz manager
        // Note: In a real app, this would be injected with Hilt
        quizManager = new QuizManager(new InMemoryQuizRepository());

        // Get quiz ID from arguments if available
        if (getArguments() != null) {
            quizId = getArguments().getString("quiz_id");
        }

        // Load or create quiz
        if (quizId != null) {
            // Load existing quiz
            currentQuiz = quizManager.getQuizById(quizId);
        }

        if (currentQuiz == null) {
            // Create new quiz
            currentQuiz = quizManager.createNewQuiz("User"); // In a real app, get real user
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize UI components
        titleEditText = view.findViewById(R.id.quiz_title_edit_text);
        descriptionEditText = view.findViewById(R.id.quiz_description_edit_text);
        nextButton = view.findViewById(R.id.next_button);

        // Set initial values if editing existing quiz
        if (currentQuiz != null) {
            titleEditText.setText(currentQuiz.getTitle());
            descriptionEditText.setText(currentQuiz.getDescription());
        }

        // Set click listeners
        nextButton.setOnClickListener(v -> saveAndNavigate());
    }

    private void saveAndNavigate() {
        // Get values from UI
        String title = titleEditText.getText().toString().trim();
        String description = descriptionEditText.getText().toString().trim();

        // Validate input
        if (title.isEmpty()) {
            Toast.makeText(requireContext(), "Title is required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Update quiz
        currentQuiz.setTitle(title);
        currentQuiz.setDescription(description);

        // Save quiz
        Quiz savedQuiz = quizManager.saveQuiz(currentQuiz);
        if (savedQuiz == null) {
            Toast.makeText(requireContext(), "Failed to save quiz", Toast.LENGTH_SHORT).show();
            return;
        }

        // Navigate to questions list
        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.action_details_to_questions);
    }
}
