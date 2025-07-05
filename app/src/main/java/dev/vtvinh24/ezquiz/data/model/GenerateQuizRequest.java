package dev.vtvinh24.ezquiz.data.model;

import com.google.gson.annotations.SerializedName;

public class GenerateQuizRequest {
  @SerializedName("prompt") // Khớp với key "prompt" mà backend yêu cầu
  private final String prompt;

  public GenerateQuizRequest(String prompt) {
    this.prompt = prompt;
  }
}