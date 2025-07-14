package dev.vtvinh24.ezquiz.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import dev.vtvinh24.ezquiz.data.converter.IntegerListConverter;
import dev.vtvinh24.ezquiz.data.converter.QuizTypeConverter;
import dev.vtvinh24.ezquiz.data.converter.StringListConverter;
import dev.vtvinh24.ezquiz.data.dao.PracticeProgressDao;
import dev.vtvinh24.ezquiz.data.dao.QuizCollectionDao;
import dev.vtvinh24.ezquiz.data.dao.QuizDao;
import dev.vtvinh24.ezquiz.data.dao.QuizSetDao;
import dev.vtvinh24.ezquiz.data.dao.QuizSessionHistoryDao;
import dev.vtvinh24.ezquiz.data.dao.UserDao;
import dev.vtvinh24.ezquiz.data.entity.PracticeProgressEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSessionHistoryEntity;
import dev.vtvinh24.ezquiz.data.entity.UserEntity;

@Database(
        entities = {QuizEntity.class, QuizSetEntity.class, QuizCollectionEntity.class, PracticeProgressEntity.class, QuizSessionHistoryEntity.class, UserEntity.class},
        version = 5,
        exportSchema = false
)
@TypeConverters({StringListConverter.class, IntegerListConverter.class, QuizTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
  public abstract QuizDao quizDao();

  public abstract QuizSetDao quizSetDao();

  public abstract QuizCollectionDao quizCollectionDao();

  public abstract PracticeProgressDao practiceProgressDao();

  public abstract QuizSessionHistoryDao quizSessionHistoryDao();

  public abstract UserDao userDao();

}
