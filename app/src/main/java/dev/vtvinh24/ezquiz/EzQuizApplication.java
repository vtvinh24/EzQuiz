package dev.vtvinh24.ezquiz;

import android.app.Application;

/**
 * Application class for EzQuiz.
 * Will be used for app-wide initialization like Dependency Injection.
 */
public class EzQuizApplication extends Application {
  @Override
  public void onCreate() {
    super.onCreate();
    // Ensure a default quiz collection exists on app install
    new Thread(() -> {
      var db = dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider.getDatabase(this);
      var dao = db.quizCollectionDao();
      if (dao.getByName("Default") == null) {
        dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity defaultCollection = new dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity();
        defaultCollection.name = "Default";
        defaultCollection.description = "Default collection for imported quizzes";
        defaultCollection.createdAt = System.currentTimeMillis();
        defaultCollection.updatedAt = System.currentTimeMillis();
        dao.insert(defaultCollection);
      }
    }).start();
  }
}
