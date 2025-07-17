package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;

public class QuizSaveResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("quiz")
    private QuizInfo quiz;

    public static class QuizInfo {
        @SerializedName("id")
        private String id;

        @SerializedName("title")
        private String title;

        @SerializedName("description")
        private String description;

        @SerializedName("questionsCount")
        private int questionsCount;

        // Getters and setters
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }

        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public int getQuestionsCount() { return questionsCount; }
        public void setQuestionsCount(int questionsCount) { this.questionsCount = questionsCount; }
    }

    // Getters and setters
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public QuizInfo getQuiz() { return quiz; }
    public void setQuiz(QuizInfo quiz) { this.quiz = quiz; }
}
