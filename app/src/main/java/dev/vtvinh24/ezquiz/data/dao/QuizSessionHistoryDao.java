package dev.vtvinh24.ezquiz.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import dev.vtvinh24.ezquiz.data.entity.QuizSessionHistoryEntity;

import java.util.List;

@Dao
public interface QuizSessionHistoryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertSession(QuizSessionHistoryEntity session);

    @Update
    void updateSession(QuizSessionHistoryEntity session);

    @Delete
    void deleteSession(QuizSessionHistoryEntity session);

    @Query("SELECT * FROM quiz_session_history WHERE quizSetId = :quizSetId ORDER BY createdAt DESC")
    List<QuizSessionHistoryEntity> getSessionsByQuizSetId(long quizSetId);

    @Query("SELECT * FROM quiz_session_history WHERE sessionType = :sessionType ORDER BY createdAt DESC")
    List<QuizSessionHistoryEntity> getSessionsByType(String sessionType);

    @Query("SELECT * FROM quiz_session_history WHERE isCompleted = 1 ORDER BY createdAt DESC")
    List<QuizSessionHistoryEntity> getCompletedSessions();

    @Query("SELECT * FROM quiz_session_history WHERE isCompleted = 0 ORDER BY createdAt DESC")
    List<QuizSessionHistoryEntity> getIncompleteSessions();

    @Query("SELECT * FROM quiz_session_history ORDER BY createdAt DESC")
    List<QuizSessionHistoryEntity> getAllSessions();

    @Query("SELECT * FROM quiz_session_history WHERE quizSetId = :quizSetId AND sessionType = :sessionType ORDER BY createdAt DESC LIMIT 1")
    QuizSessionHistoryEntity getLatestSession(long quizSetId, String sessionType);

    @Query("SELECT AVG(scorePercentage) FROM quiz_session_history WHERE quizSetId = :quizSetId AND isCompleted = 1")
    Double getAverageScoreByQuizSetId(long quizSetId);

    @Query("SELECT COUNT(*) FROM quiz_session_history WHERE quizSetId = :quizSetId AND isCompleted = 1")
    int getCompletedSessionCountByQuizSetId(long quizSetId);

    @Query("DELETE FROM quiz_session_history WHERE quizSetId = :quizSetId")
    void deleteSessionsByQuizSetId(long quizSetId);
}
