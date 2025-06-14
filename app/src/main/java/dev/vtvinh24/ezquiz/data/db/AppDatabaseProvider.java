package dev.vtvinh24.ezquiz.data.db;

import android.content.Context;

import androidx.room.Room;

public class AppDatabaseProvider {
  private static AppDatabase instance;

  public static AppDatabase getDatabase(Context context) {
    if (instance == null) {
      instance = Room.databaseBuilder(
              context.getApplicationContext(),
              AppDatabase.class,
              "ezquiz-db"
      ).allowMainThreadQueries().build();
    }
    return instance;
  }
}

