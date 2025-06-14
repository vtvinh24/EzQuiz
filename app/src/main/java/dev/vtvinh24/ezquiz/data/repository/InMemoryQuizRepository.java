package dev.vtvinh24.ezquiz.data.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.vtvinh24.ezquiz.domain.model.Quiz;
import dev.vtvinh24.ezquiz.domain.repository.QuizRepository;

/**
 * In-memory implementation of QuizRepository.
 * This will be replaced with a proper database implementation later.
 */
public class InMemoryQuizRepository implements QuizRepository {

    private final Map<String, Quiz> quizzes = new HashMap<>();

    @Override
    public List<Quiz> getAllQuizzes() {
        return new ArrayList<>(quizzes.values());
    }

    @Override
    public Quiz getQuizById(String quizId) {
        return quizzes.get(quizId);
    }

    @Override
    public Quiz saveQuiz(Quiz quiz) {
        if (quiz != null) {
            quiz.setUpdatedAt(System.currentTimeMillis());
            quizzes.put(quiz.getId(), quiz);
        }
        return quiz;
    }

    @Override
    public boolean deleteQuiz(String quizId) {
        return quizzes.remove(quizId) != null;
    }
}
