package dev.vtvinh24.ezquiz.ui.play;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import dev.vtvinh24.ezquiz.domain.model.Answer;
import dev.vtvinh24.ezquiz.domain.model.Question;
import dev.vtvinh24.ezquiz.domain.model.Quiz;
import dev.vtvinh24.ezquiz.domain.repository.QuizRepository;

/**
 * ViewModel for Quiz Play screen.
 * Manages UI-related data and business logic for playing quizzes.
 */
public class PlayViewModel extends ViewModel {

    private final QuizRepository quizRepository;
    private final MutableLiveData<Quiz> currentQuiz = new MutableLiveData<>();
    private final MutableLiveData<Integer> currentQuestionIndex = new MutableLiveData<>(0);
    private final MutableLiveData<Question> currentQuestion = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> isQuizFinished = new MutableLiveData<>(false);

    // Track user answers
    private final List<Answer> userAnswers = new ArrayList<>();

    public PlayViewModel(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    /**
     * Load a quiz for playing.
     *
     * @param quizId ID of the quiz to play.
     */
    public void loadQuiz(String quizId) {
        isLoading.setValue(true);
        try {
            Quiz quiz = quizRepository.getQuizById(quizId);
            if (quiz != null) {
                currentQuiz.setValue(quiz);
                currentQuestionIndex.setValue(0);
                score.setValue(0);
                userAnswers.clear();
                isQuizFinished.setValue(false);

                if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
                    currentQuestion.setValue(quiz.getQuestions().get(0));
                }
            }
        } finally {
            isLoading.setValue(false);
        }
    }

    /**
     * Submit an answer for the current question.
     *
     * @param answerId ID of the selected answer.
     */
    public void submitAnswer(String answerId) {
        Quiz quiz = currentQuiz.getValue();
        Integer questionIndex = currentQuestionIndex.getValue();

        if (quiz != null && questionIndex != null && quiz.getQuestions() != null &&
                questionIndex >= 0 && questionIndex < quiz.getQuestions().size()) {

            Question question = quiz.getQuestions().get(questionIndex);
            if (question.getAnswers() != null) {
                for (Answer answer : question.getAnswers()) {
                    if (answer.getId().equals(answerId)) {
                        userAnswers.add(answer);
                        if (answer.isCorrect()) {
                            int currentScore = score.getValue() != null ? score.getValue() : 0;
                            score.setValue(currentScore + question.getPoints());
                        }
                        break;
                    }
                }
            }

            // Move to next question or finish quiz
            if (questionIndex + 1 < quiz.getQuestions().size()) {
                currentQuestionIndex.setValue(questionIndex + 1);
                currentQuestion.setValue(quiz.getQuestions().get(questionIndex + 1));
            } else {
                isQuizFinished.setValue(true);
            }
        }
    }

    /**
     * Get the current quiz being played.
     *
     * @return LiveData containing the current quiz.
     */
    public LiveData<Quiz> getCurrentQuiz() {
        return currentQuiz;
    }

    /**
     * Get the current question.
     *
     * @return LiveData containing the current question.
     */
    public LiveData<Question> getCurrentQuestion() {
        return currentQuestion;
    }

    /**
     * Get the loading state.
     *
     * @return LiveData containing the loading state.
     */
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    /**
     * Get the current score.
     *
     * @return LiveData containing the current score.
     */
    public LiveData<Integer> getScore() {
        return score;
    }

    /**
     * Check if the quiz is finished.
     *
     * @return LiveData indicating whether the quiz is finished.
     */
    public LiveData<Boolean> getIsQuizFinished() {
        return isQuizFinished;
    }
}
