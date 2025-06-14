package dev.vtvinh24.ezquiz.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;

@Dao
public interface QuizCollectionDao {
  @Insert
  long insert(QuizCollectionEntity collection);

  @Update
  int update(QuizCollectionEntity collection);

  @Delete
  int delete(QuizCollectionEntity collection);

  @Query("SELECT * FROM quiz_collection WHERE id = :id")
  QuizCollectionEntity getById(long id);

  @Query("SELECT * FROM quiz_collection")
  List<QuizCollectionEntity> getAll();

  @Query("SELECT * FROM quiz_collection WHERE archived = 1")
  List<QuizCollectionEntity> getArchived();
}
