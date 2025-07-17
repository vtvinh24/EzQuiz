package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class GeneratedQuizResponse {
    @SerializedName("quizzes")
    private List<QuizQuestion> quizzes;

    @SerializedName("savedQuiz")
    private SavedQuizInfo savedQuiz;

    @SerializedName("saveError")
    private String saveError;

    public static class SavedQuizInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("title")
        private String title;

        @SerializedName("questionsCount")
        private int questionsCount;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public int getQuestionsCount() { return questionsCount; }
        public void setQuestionsCount(int questionsCount) { this.questionsCount = questionsCount; }
    }

    // Getters and setters
    public List<QuizQuestion> getQuizzes() { return quizzes; }
    public void setQuizzes(List<QuizQuestion> quizzes) { this.quizzes = quizzes; }

    public SavedQuizInfo getSavedQuiz() { return savedQuiz; }
    public void setSavedQuiz(SavedQuizInfo savedQuiz) { this.savedQuiz = savedQuiz; }

    public String getSaveError() { return saveError; }
    public void setSaveError(String saveError) { this.saveError = saveError; }
}
