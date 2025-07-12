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

  public static AppDatabase getDatabase(Context context) {
    if (instance == null) {
      instance = Room.databaseBuilder(
              context.getApplicationContext(),
              AppDatabase.class,
              "ezquiz-db"
      ).addMigrations(MIGRATION_1_2).allowMainThreadQueries().build();
    }
    return instance;
  }
}
