package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;

public class QuizDetailResponse {
    @SerializedName("quiz")
    private ServerQuiz quiz;

    public ServerQuiz getQuiz() { return quiz; }
    public void setQuiz(ServerQuiz quiz) { this.quiz = quiz; }
}
