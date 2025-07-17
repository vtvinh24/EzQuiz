package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SaveQuizRequest {
    @SerializedName("title")
    private String title;

    @SerializedName("description")
    private String description;

    @SerializedName("quizzes")
    private List<QuizQuestion> quizzes;

    @SerializedName("isPublic")
    private boolean isPublic;

    @SerializedName("tags")
    private List<String> tags;

    public SaveQuizRequest(String title, String description, List<QuizQuestion> quizzes,
                         boolean isPublic, List<String> tags) {
        this.title = title;
        this.description = description;
        this.quizzes = quizzes;
        this.isPublic = isPublic;
        this.tags = tags;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public List<QuizQuestion> getQuizzes() { return quizzes; }
    public boolean isPublic() { return isPublic; }
    public List<String> getTags() { return tags; }
}
