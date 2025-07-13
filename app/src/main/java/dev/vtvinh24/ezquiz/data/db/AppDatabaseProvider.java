package dev.vtvinh24.ezquiz.data.db;

import android.content.Context;

import androidx.room.Room;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class AppDatabaseProvider {
  private static AppDatabase instance;

  static final Migration MIGRATION_1_2 = new Migration(1, 2) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("CREATE TABLE IF NOT EXISTS `practice_progress` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`quizSetId` INTEGER NOT NULL, " +
          "`currentPosition` INTEGER NOT NULL, " +
          "`quizIds` TEXT, " +
          "`userAnswers` TEXT, " +
          "`sessionResults` TEXT, " +
          "`createdAt` INTEGER NOT NULL, " +
          "`updatedAt` INTEGER NOT NULL)");
    }
  };

  static final Migration MIGRATION_2_3 = new Migration(2, 3) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("CREATE TABLE IF NOT EXISTS `quiz_session_history` (" +
          "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
          "`quizSetId` INTEGER NOT NULL, " +
          "`sessionType` TEXT, " +
          "`totalQuestions` INTEGER NOT NULL, " +
          "`correctAnswers` INTEGER NOT NULL, " +
          "`incorrectAnswers` INTEGER NOT NULL, " +
          "`skippedAnswers` INTEGER NOT NULL, " +
          "`scorePercentage` REAL NOT NULL, " +
          "`timeSpent` INTEGER NOT NULL, " +
          "`detailedResults` TEXT, " +
          "`isCompleted` INTEGER NOT NULL, " +
          "`startTime` INTEGER NOT NULL, " +
          "`endTime` INTEGER NOT NULL, " +
          "`createdAt` INTEGER NOT NULL)");
    }
  };

  public static AppDatabase getDatabase(Context context) {
    if (instance == null) {
      instance = Room.databaseBuilder(
              context.getApplicationContext(),
              AppDatabase.class,
              "ezquiz-db-v3"
      ).addMigrations(MIGRATION_1_2, MIGRATION_2_3)
       .allowMainThreadQueries().build();
    }
    return instance;
  }
}
