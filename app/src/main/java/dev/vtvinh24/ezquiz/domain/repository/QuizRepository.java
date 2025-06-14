package dev.vtvinh24.ezquiz.domain.repository;

import java.util.List;

import dev.vtvinh24.ezquiz.domain.model.Quiz;

/**
 * Repository interface for Quiz data operations.
 */
public interface QuizRepository {

    /**
     * Get all quizzes.
     *
     * @return List of all quizzes.
     */
    List<Quiz> getAllQuizzes();

    /**
     * Get a quiz by its ID.
     *
     * @param quizId The ID of the quiz to retrieve.
     * @return The quiz with the specified ID or null if not found.
     */
    Quiz getQuizById(String quizId);

    /**
     * Save or update a quiz.
     *
     * @param quiz The quiz to save or update.
     * @return The saved quiz with updated information.
     */
    Quiz saveQuiz(Quiz quiz);

    /**
     * Delete a quiz.
     *
     * @param quizId The ID of the quiz to delete.
     * @return True if the deletion was successful, false otherwise.
     */
    boolean deleteQuiz(String quizId);
}
