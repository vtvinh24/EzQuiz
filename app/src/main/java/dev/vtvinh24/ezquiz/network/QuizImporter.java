package dev.vtvinh24.ezquiz.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.vtvinh24.ezquiz.data.model.Quiz;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class QuizImporter {
  private final Gson gson = new Gson();

  public String getRemoteJSON(String url) {
    PasteService service = RetrofitClient.getPasteService(PasteService.class);
    Call<ResponseBody> call = service.getRawPaste(url);
    try {
      Response<ResponseBody> response = call.execute();
      if (response.isSuccessful() && response.body() != null) {
        return response.body().string();
      }
    } catch (IOException e) {
      Log.e("QuizImporter", "Error fetching remote JSON: " + e.getMessage());
    }
    return null;
  }

  public List<Quiz> importFlashcards(String url) {
    String json = getRemoteJSON(url);
    if (json == null) return List.of();
    try {
      JsonArray arr = gson.fromJson(json, JsonArray.class);
      List<Quiz> quizzes = new ArrayList<>();
      long now = System.currentTimeMillis();
      for (JsonElement el : arr) {
        if (!el.isJsonObject()) continue;
        JsonObject obj = el.getAsJsonObject();
        String question = obj.has("front") ? obj.get("front").getAsString() : null;
        String answer = obj.has("back") ? obj.get("back").getAsString() : null;
        if (question == null || answer == null) continue;
        List<String> answers = Collections.singletonList(answer);
        List<Integer> correct = Collections.singletonList(0);
        quizzes.add(new Quiz(
          question,
          answers,
          correct,
          Quiz.Type.FLASHCARD,
          now,
          now,
          false,
          0
        ));
      }
      return quizzes;
    } catch (Exception e) {
      Log.e("QuizImporter", "Error parsing JSON: " + e.getMessage());
      return List.of();
    }
  }

  // TODO: Implement quiz importing functionality
  public List<Quiz> importQuizzes(String url) {
    // TODO: Add logic to parse the source and convert it into a list of Quiz objects
    return List.of();
  }

  public List<Quiz> importQuizzesFromJson(String json) {
    // TODO: Add logic to parse the JSON string and convert it into a list of Quiz objects
    return List.of();
  }

  public List<Quiz> importFlashcardsFromJson(String json) {
    if (json == null) return List.of();
    try {
      JsonArray arr = gson.fromJson(json, JsonArray.class);
      List<Quiz> quizzes = new ArrayList<>();
      long now = System.currentTimeMillis();
      for (JsonElement el : arr) {
        if (!el.isJsonObject()) continue;
        JsonObject obj = el.getAsJsonObject();
        String question = obj.has("front") ? obj.get("front").getAsString() : null;
        String answer = obj.has("back") ? obj.get("back").getAsString() : null;
        if (question == null || answer == null) continue;
        List<String> answers = Collections.singletonList(answer);
        List<Integer> correct = Collections.singletonList(0);
        quizzes.add(new Quiz(
          question,
          answers,
          correct,
          Quiz.Type.FLASHCARD,
          now,
          now,
          false,
          0
        ));
      }
      return quizzes;
    } catch (Exception e) {
      Log.e("QuizImporter", "Error parsing JSON: " + e.getMessage());
      return List.of();
    }
  }
}
