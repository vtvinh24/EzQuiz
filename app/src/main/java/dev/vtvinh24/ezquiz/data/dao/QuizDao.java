package dev.vtvinh24.ezquiz.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dev.vtvinh24.ezquiz.data.entity.QuizEntity;

@Dao
public interface QuizDao {
  @Insert
  long insert(QuizEntity quiz);

  @Update
  int update(QuizEntity quiz);

  @Delete
  int delete(QuizEntity quiz);

  @Query("SELECT * FROM quiz WHERE id = :id")
  QuizEntity getById(long id);

  @Query("SELECT * FROM quiz WHERE quizSetId = :quizSetId")
  List<QuizEntity> getByQuizSetId(long quizSetId);

  @Query("SELECT * FROM quiz")
  List<QuizEntity> getAll();

  @Query("SELECT * FROM quiz WHERE archived = 1")
  List<QuizEntity> getArchived();

  @Query("SELECT * FROM quiz WHERE difficulty = :difficulty")
  List<QuizEntity> getByDifficulty(int difficulty);
}
