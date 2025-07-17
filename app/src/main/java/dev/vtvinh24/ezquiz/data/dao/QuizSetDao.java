package dev.vtvinh24.ezquiz.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

@Dao
public interface QuizSetDao {
  @Insert
  long insert(QuizSetEntity quizSet);

  @Update
  int update(QuizSetEntity quizSet);

  @Delete
  int delete(QuizSetEntity quizSet);

  @Query("SELECT * FROM quiz_set WHERE id = :id")
  QuizSetEntity getById(long id);

  @Query("SELECT * FROM quiz_set")
  List<QuizSetEntity> getAll();

  // Queries mới cho user-specific quiz management
  @Query("SELECT * FROM quiz_set WHERE userId = :userId ORDER BY updatedAt DESC")
  List<QuizSetEntity> getQuizSetsByUserId(String userId);

  @Query("SELECT * FROM quiz_set WHERE userId = :userId AND archived = 0 ORDER BY updatedAt DESC")
  List<QuizSetEntity> getActiveQuizSetsByUserId(String userId);

  @Query("SELECT * FROM quiz_set WHERE serverId = :serverId AND userId = :userId")
  QuizSetEntity getQuizSetByServerIdAndUserId(String serverId, String userId);

  @Query("SELECT * FROM quiz_set WHERE needsUpload = 1 AND userId = :userId")
  List<QuizSetEntity> getQuizSetsNeedingUpload(String userId);

  @Query("SELECT * FROM quiz_set WHERE isFromServer = 1 AND userId = :userId")
  List<QuizSetEntity> getServerQuizSetsByUserId(String userId);

  @Query("SELECT * FROM quiz_set WHERE isFromServer = 0 AND userId = :userId")
  List<QuizSetEntity> getLocalQuizSetsByUserId(String userId);

  @Query("SELECT COUNT(*) FROM quiz_set WHERE userId = :userId AND archived = 0")
  int countActiveByUserId(String userId);

  @Query("UPDATE quiz_set SET needsUpload = 1 WHERE id = :quizSetId")
  void markForUpload(long quizSetId);

  @Query("UPDATE quiz_set SET serverId = :serverId, isFromServer = 1, needsUpload = 0, lastSyncTime = :syncTime WHERE id = :quizSetId")
  void markAsUploaded(long quizSetId, String serverId, long syncTime);

  @Query("DELETE FROM quiz_set WHERE userId = :userId")
  void deleteAllByUserId(String userId);

  // Legacy queries for backward compatibility
  @Query("SELECT * FROM quiz_set WHERE collectionId = :collectionId")
  List<QuizSetEntity> getByCollectionId(long collectionId);

  @Query("SELECT COUNT(*) FROM quiz_set WHERE collectionId = :collectionId AND archived = 0")
  int countByCollectionId(long collectionId);

  @Query("SELECT * FROM quiz_set WHERE archived = 1")
  List<QuizSetEntity> getArchived();
}
