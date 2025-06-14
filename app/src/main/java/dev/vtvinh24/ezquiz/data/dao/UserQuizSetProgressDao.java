package dev.vtvinh24.ezquiz.data.dao;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import dev.vtvinh24.ezquiz.data.model.UserQuizSetProgress;

public class UserQuizSetProgressDao {
  private static final String PREF_NAME = "user_quiz_set_progress";
  private final SharedPreferences prefs;
  private final Gson gson = new Gson();

  public UserQuizSetProgressDao(Context context) {
    this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
  }

  public UserQuizSetProgress getProgress(long setId) {
    String json = prefs.getString(String.valueOf(setId), null);
    if (json == null) return new UserQuizSetProgress();
    return gson.fromJson(json, UserQuizSetProgress.class);
  }

  public void setProgress(long setId, UserQuizSetProgress progress) {
    prefs.edit().putString(String.valueOf(setId), gson.toJson(progress)).apply();
  }

  public void deleteProgress(long setId) {
    prefs.edit().remove(String.valueOf(setId)).apply();
  }
}

