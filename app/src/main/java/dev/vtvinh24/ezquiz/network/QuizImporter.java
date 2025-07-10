package dev.vtvinh24.ezquiz.network;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.vtvinh24.ezquiz.data.model.Quiz;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;

public class QuizImporter {
  private final Gson gson = new Gson();
  private static final String TAG = "QuizImporter";

  public String getRemoteJSON(String pasteId) {
    PasteService service = RetrofitClient.getPasteService(PasteService.class);
    Call<ResponseBody> call = service.getRawPaste(pasteId);
    try {
      Response<ResponseBody> response = call.execute();
      if (response.isSuccessful() && response.body() != null) {
        return response.body().string();
      }
    } catch (IOException e) {
      Log.e(TAG, "Error fetching remote JSON: " + e.getMessage());
    }
    return null;
  }

  public List<Quiz> importFlashcards(String pasteId) {
    String json = getRemoteJSON(pasteId);
    if (json == null) return List.of();
    return importFlashcardsFromJson(json);
  }

  public List<Quiz> importQuizzes(String pasteId) {
    String json = getRemoteJSON(pasteId);
    if (json == null) return List.of();
    return importQuizzesFromJson(json);
  }

  public List<Quiz> importQuizzesFromJson(String json) {
    if (json == null) return List.of();
    try {
      JsonArray arr = gson.fromJson(json, JsonArray.class);
      List<Quiz> quizzes = new ArrayList<>();
      long now = System.currentTimeMillis();

      for (JsonElement el : arr) {
        if (!el.isJsonObject()) continue;

        JsonObject obj = el.getAsJsonObject();
        if (!obj.has("question") || !obj.has("answers")) continue;

        String question = obj.get("question").getAsString();
        JsonArray answersArray = obj.getAsJsonArray("answers");
        List<String> answers = new ArrayList<>();
        List<Integer> correctIndices = new ArrayList<>();

        for (int i = 0; i < answersArray.size(); i++) {
          JsonObject answerObj = answersArray.get(i).getAsJsonObject();
          answers.add(answerObj.get("text").getAsString());

          if (answerObj.has("isCorrect") && answerObj.get("isCorrect").getAsBoolean()) {
            correctIndices.add(i);
          }
        }

        if (answers.isEmpty() || correctIndices.isEmpty()) continue;

        Quiz.Type quizType = Quiz.Type.SINGLE_CHOICE;
        if (correctIndices.size() > 1) {
          quizType = Quiz.Type.MULTIPLE_CHOICE;
        }

        // If specified in JSON, use that type
        if (obj.has("questionType")) {
          String typeStr = obj.get("questionType").getAsString();
          if ("multiple_choice".equals(typeStr)) {
            quizType = Quiz.Type.SINGLE_CHOICE;
          }
        }

        quizzes.add(new Quiz(
                question,
                answers,
                correctIndices,
                quizType,
                now,
                now,
                false,
                0
        ));
      }

      return quizzes;
    } catch (Exception e) {
      Log.e(TAG, "Error parsing quizzes JSON: " + e.getMessage(), e);
      return List.of();
    }
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
      Log.e(TAG, "Error parsing flashcards JSON: " + e.getMessage(), e);
      return List.of();
    }
  }
}
