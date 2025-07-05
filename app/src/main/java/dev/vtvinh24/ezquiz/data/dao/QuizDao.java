package dev.vtvinh24.ezquiz.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.model.Quiz;

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

  @Query("SELECT * FROM quiz WHERE quizSetId = :quizSetId AND type != 'FLASHCARD'")
  List<QuizEntity> getByQuizSetId(long quizSetId);

  @Query("SELECT * FROM quiz")
  List<QuizEntity> getAll();

  @Query("SELECT * FROM quiz WHERE archived = 1")
  List<QuizEntity> getArchived();

  @Query("SELECT * FROM quiz WHERE difficulty = :difficulty")
  List<QuizEntity> getByDifficulty(int difficulty);

  @Query("SELECT * FROM quiz WHERE type = :type")
  List<QuizEntity> getByType(Quiz.Type type);

  @Query("SELECT * FROM quiz WHERE type = 'FLASHCARD'")
  List<QuizEntity> getFlashcards();

  ;

  @Query("SELECT * FROM quiz WHERE quizSetId = :quizSetId AND type = 'FLASHCARD'")
  List<QuizEntity> getFlashcardsByQuizSetId(long quizSetId); // Phương thức cũ gây ra lỗi

  // ====================================================================
  // === THÊM PHƯƠNG THỨC MỚI NÀY VÀO ===
  // Lấy TẤT CẢ các quiz trong một set để dùng cho màn hình Flashcard
  @Query("SELECT * FROM quiz WHERE quizSetId = :quizSetId")
  List<QuizEntity> getAllQuizzesBySetIdForFlashcard(long quizSetId);
  // ====================================================================
}
