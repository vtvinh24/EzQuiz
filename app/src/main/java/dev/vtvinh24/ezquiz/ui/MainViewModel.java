package dev.vtvinh24.ezquiz.ui;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dev.vtvinh24.ezquiz.domain.model.Quiz;
import dev.vtvinh24.ezquiz.domain.repository.QuizRepository;

/**
 * ViewModel for the MainActivity.
 * Handles UI-related data and business logic for the main screen.
 */
public class MainViewModel extends ViewModel {

    private final QuizRepository quizRepository;
    private final MutableLiveData<List<Quiz>> quizzes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public MainViewModel(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
        loadQuizzes();
    }

    /**
     * Load all quizzes from the repository.
     */
    public void loadQuizzes() {
        isLoading.setValue(true);
        try {
            List<Quiz> allQuizzes = quizRepository.getAllQuizzes();
            quizzes.setValue(allQuizzes);
        } finally {
            isLoading.setValue(false);
        }
    }

    /**
     * Get all quizzes as LiveData.
     *
     * @return LiveData containing list of quizzes.
     */
    public LiveData<List<Quiz>> getQuizzes() {
        return quizzes;
    }

    /**
     * Get loading state as LiveData.
     *
     * @return LiveData containing loading state.
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Delete a quiz by its ID.
     *
     * @param quizId ID of the quiz to delete.
     * @return True if deletion was successful, false otherwise.
     */
    public boolean deleteQuiz(String quizId) {
        boolean result = quizRepository.deleteQuiz(quizId);
        if (result) {
            loadQuizzes(); // Refresh the quiz list
        }
        return result;
    }
}
