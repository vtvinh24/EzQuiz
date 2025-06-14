package dev.vtvinh24.ezquiz.data.dao;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import dev.vtvinh24.ezquiz.data.model.UserQuizProgress;

public class UserQuizProgressDao {
  private static final String PREF_NAME = "user_quiz_progress";
  private final SharedPreferences prefs;
  private final Gson gson = new Gson();

  public UserQuizProgressDao(Context context) {
    this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
  }

  public UserQuizProgress getProgress(long quizId) {
    String json = prefs.getString(String.valueOf(quizId), null);
    if (json == null) return new UserQuizProgress();
    return gson.fromJson(json, UserQuizProgress.class);
  }

  public void setProgress(long quizId, UserQuizProgress progress) {
    prefs.edit().putString(String.valueOf(quizId), gson.toJson(progress)).apply();
  }

  public void deleteProgress(long quizId) {
    prefs.edit().remove(String.valueOf(quizId)).apply();
  }
}

