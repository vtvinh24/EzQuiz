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

  // Prevent deleting the Default collection
  @Query("DELETE FROM quiz_collection WHERE id = :id AND name != 'Default'")
  int deleteIfNotDefault(long id);

  // Prevent updating the Default collection
  @Query("UPDATE quiz_collection SET name = :name, description = :description, updatedAt = :updatedAt, archived = :archived, difficulty = :difficulty, `order` = :order WHERE id = :id AND name != 'Default'")
  int updateIfNotDefault(long id, String name, String description, long updatedAt, boolean archived, int difficulty, int order);

  @Query("SELECT * FROM quiz_collection WHERE id = :id")
  QuizCollectionEntity getById(long id);

  @Query("SELECT * FROM quiz_collection")
  List<QuizCollectionEntity> getAll();

  @Query("SELECT * FROM quiz_collection WHERE archived = 1")
  List<QuizCollectionEntity> getArchived();

  @Query("SELECT * FROM quiz_collection WHERE name = :name LIMIT 1")
  QuizCollectionEntity getByName(String name);
}
