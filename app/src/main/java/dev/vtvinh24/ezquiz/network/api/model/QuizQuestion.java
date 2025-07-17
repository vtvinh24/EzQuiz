package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class QuizQuestion {
    @SerializedName("question")
    private String question;
    
    @SerializedName("answers")
    private List<String> answers;
    
    @SerializedName("correctAnswerIndices")
    private List<Integer> correctAnswerIndices;
    
    @SerializedName("type")
    private String type; // "SINGLE_CHOICE" or "MULTIPLE_CHOICE"
    
    // Getters and setters
    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
    
    public List<String> getAnswers() { return answers; }
    public void setAnswers(List<String> answers) { this.answers = answers; }
    
    public List<Integer> getCorrectAnswerIndices() { return correctAnswerIndices; }
    public void setCorrectAnswerIndices(List<Integer> correctAnswerIndices) { 
        this.correctAnswerIndices = correctAnswerIndices; 
    }
    
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
