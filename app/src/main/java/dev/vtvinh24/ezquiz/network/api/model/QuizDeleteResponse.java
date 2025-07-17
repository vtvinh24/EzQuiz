package dev.vtvinh24.ezquiz.network.api.model;

import com.google.gson.annotations.SerializedName;

public class QuizDeleteResponse {
    @SerializedName("message")
    private String message;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
