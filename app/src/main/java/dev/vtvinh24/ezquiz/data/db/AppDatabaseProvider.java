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

  static final Migration MIGRATION_3_4 = new Migration(3, 4) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      database.execSQL("CREATE TABLE IF NOT EXISTS `user` (" +
          "`id` TEXT PRIMARY KEY NOT NULL, " +
          "`email` TEXT, " +
          "`name` TEXT, " +
          "`token` TEXT, " +
          "`isPremium` INTEGER NOT NULL, " +
          "`premiumExpiryDate` TEXT, " +
          "`createdAt` INTEGER NOT NULL, " +
          "`lastLoginAt` INTEGER NOT NULL, " +
          "`isLoggedIn` INTEGER NOT NULL)");
    }
  };

  static final Migration MIGRATION_4_5 = new Migration(4, 5) {
    @Override
    public void migrate(SupportSQLiteDatabase database) {
      // Add premium fields to existing user table if they don't exist
      database.execSQL("ALTER TABLE user ADD COLUMN isPremium INTEGER NOT NULL DEFAULT 0");
      database.execSQL("ALTER TABLE user ADD COLUMN premiumExpiryDate TEXT");
    }
  };

  public static AppDatabase getDatabase(Context context) {
    if (instance == null) {
      instance = Room.databaseBuilder(
              context.getApplicationContext(),
              AppDatabase.class,
              "ezquiz-db-v5"
      ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
       .fallbackToDestructiveMigration()
       .allowMainThreadQueries().build();
    }
    return instance;
  }
}
