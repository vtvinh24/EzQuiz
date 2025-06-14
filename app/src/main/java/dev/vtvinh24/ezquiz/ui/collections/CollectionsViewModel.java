package dev.vtvinh24.ezquiz.ui.collections;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dev.vtvinh24.ezquiz.domain.model.Quiz;
import dev.vtvinh24.ezquiz.domain.repository.QuizRepository;

/**
 * ViewModel for Quiz Collections screen.
 * Manages UI-related data and business logic for the quiz collections feature.
 */
public class CollectionsViewModel extends ViewModel {

    private final QuizRepository quizRepository;
    private final MutableLiveData<List<Quiz>> quizzes = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public CollectionsViewModel(QuizRepository quizRepository) {
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
}
