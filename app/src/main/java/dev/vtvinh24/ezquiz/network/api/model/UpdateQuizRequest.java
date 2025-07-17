package dev.vtvinh24.ezquiz.network.api.model;

import java.util.List;

public class UpdateQuizRequest extends SaveQuizRequest {
    public UpdateQuizRequest(String title, String description, List<QuizQuestion> quizzes,
                           boolean isPublic, List<String> tags) {
        super(title, description, quizzes, isPublic, tags);
    }
}
