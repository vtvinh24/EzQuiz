package dev.vtvinh24.ezquiz.data.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import dev.vtvinh24.ezquiz.data.converter.IntegerListConverter;
import dev.vtvinh24.ezquiz.data.converter.QuizTypeConverter;
import dev.vtvinh24.ezquiz.data.converter.StringListConverter;
import dev.vtvinh24.ezquiz.data.dao.QuizCollectionDao;
import dev.vtvinh24.ezquiz.data.dao.QuizDao;
import dev.vtvinh24.ezquiz.data.dao.QuizSetDao;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

@Database(
        entities = {QuizEntity.class, QuizSetEntity.class, QuizCollectionEntity.class},
        version = 1,
        exportSchema = false
)
@TypeConverters({StringListConverter.class, IntegerListConverter.class, QuizTypeConverter.class})
public abstract class AppDatabase extends RoomDatabase {
  public abstract QuizDao quizDao();

  public abstract QuizSetDao quizSetDao();

  public abstract QuizCollectionDao quizCollectionDao();

}
