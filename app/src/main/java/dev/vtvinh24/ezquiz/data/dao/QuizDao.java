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

  @Query("SELECT * FROM quiz WHERE id IN (:quizIds)")
  List<QuizEntity> getQuizzesByIds(List<Long> quizIds);
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
  List<QuizEntity> getFlashcardsByQuizSetId(long quizSetId);

  @Query("SELECT COUNT(*) FROM quiz WHERE quizSetId = :quizSetId")
  int countByQuizSetId(long quizSetId);

  // ====================================================================
  // === THÊM PHƯƠNG THỨC MỚI NÀY VÀO ===
  // Lấy TẤT CẢ các quiz trong một set để dùng cho màn hình Flashcard
  @Query("SELECT * FROM quiz WHERE quizSetId = :quizSetId")
  List<QuizEntity> getAllQuizzesBySetIdForFlashcard(long quizSetId);
  // ====================================================================
  // Trong QuizDao.java

  @Query("SELECT * FROM quiz WHERE quizSetId = :quizSetId AND (type = 'SINGLE_CHOICE' OR type = 'MULTIPLE_CHOICE')")
  List<QuizEntity> getMultipleChoiceQuizzesOfSet(long quizSetId);

  @Query("SELECT * FROM quiz WHERE quizSetId = :quizSetId AND type = 'TRUE_FALSE'")
  List<QuizEntity> getTrueFalseQuizzesOfSet(long quizSetId);

  @Query("SELECT * FROM quiz WHERE quizSetId = :quizSetId AND type != 'FLASHCARD'")
  List<QuizEntity> getAllTestableQuizzesOfSet(long quizSetId);

  /**
   * Đếm số lượng câu hỏi Trắc nghiệm (cả đơn và đa lựa chọn) trong một bộ.
   */
  @Query("SELECT COUNT(id) FROM quiz WHERE quizSetId = :quizSetId AND (type = 'SINGLE_CHOICE' OR type = 'MULTIPLE_CHOICE')")
  int countMultipleChoiceQuizzes(long quizSetId);

  /**
   * Đếm số lượng câu hỏi Đúng/Sai trong một bộ.
   */
  @Query("SELECT COUNT(id) FROM quiz WHERE quizSetId = :quizSetId AND type = 'TRUE_FALSE'")
  int countTrueFalseQuizzes(long quizSetId);

  /**
   * Lấy các câu hỏi trong một bộ dựa trên một danh sách các loại (types) được chỉ định.
   */
  @Query("SELECT * FROM quiz WHERE quizSetId = :quizSetId AND type IN (:types)")
  List<QuizEntity> getQuizzesOfSetByTypes(long quizSetId, List<Quiz.Type> types);

  /**
   * Đếm số lượng câu hỏi trong một bộ dựa trên một danh sách các loại.
   */
  @Query("SELECT COUNT(id) FROM quiz WHERE quizSetId = :quizSetId AND type IN (:types)")
  int countQuizzesOfSetByTypes(long quizSetId, List<Quiz.Type> types);


}
