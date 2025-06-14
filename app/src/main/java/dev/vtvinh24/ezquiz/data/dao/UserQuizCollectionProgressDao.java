package dev.vtvinh24.ezquiz.data.dao;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;

import dev.vtvinh24.ezquiz.data.model.UserQuizCollectionProgress;

public class UserQuizCollectionProgressDao {
  private static final String PREF_NAME = "user_quiz_collection_progress";
  private final SharedPreferences prefs;
  private final Gson gson = new Gson();

  public UserQuizCollectionProgressDao(Context context) {
    this.prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
  }

  public UserQuizCollectionProgress getProgress(long collectionId) {
    String json = prefs.getString(String.valueOf(collectionId), null);
    if (json == null) return new UserQuizCollectionProgress();
    return gson.fromJson(json, UserQuizCollectionProgress.class);
  }

  public void setProgress(long collectionId, UserQuizCollectionProgress progress) {
    prefs.edit().putString(String.valueOf(collectionId), gson.toJson(progress)).apply();
  }

  public void deleteProgress(long collectionId) {
    prefs.edit().remove(String.valueOf(collectionId)).apply();
  }
}

