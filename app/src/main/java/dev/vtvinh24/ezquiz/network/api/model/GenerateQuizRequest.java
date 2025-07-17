package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;

public class GenerateQuizRequest {
    @SerializedName("prompt")
    private String prompt;

    @SerializedName("saveQuiz")
    private String saveQuiz;

    @SerializedName("quizTitle")
    private String quizTitle;

    @SerializedName("quizDescription")
    private String quizDescription;

    @SerializedName("tags")
    private String tags;

    @SerializedName("isPublic")
    private String isPublic;

    public GenerateQuizRequest(String prompt, boolean saveQuiz, String quizTitle,
                             String quizDescription, String tags, boolean isPublic) {
        this.prompt = prompt;
        this.saveQuiz = String.valueOf(saveQuiz);
        this.quizTitle = quizTitle;
        this.quizDescription = quizDescription;
        this.tags = tags;
        this.isPublic = String.valueOf(isPublic);
    }

    // Getters
    public String getPrompt() { return prompt; }
    public String getSaveQuiz() { return saveQuiz; }
    public String getQuizTitle() { return quizTitle; }
    public String getQuizDescription() { return quizDescription; }
    public String getTags() { return tags; }
    public String getIsPublic() { return isPublic; }
}
