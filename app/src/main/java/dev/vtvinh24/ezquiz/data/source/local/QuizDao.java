package dev.vtvinh24.ezquiz.data.source.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.model.UserProgress;

/**
 * Data Access Object for Quiz entity.
 * Provides methods to interact with the quizzes table in the database.
 */
@Dao
public interface QuizDao {

    @Query("SELECT * FROM quizzes WHERE collectionId = :collectionId")
    List<Quiz> getQuizzesForCollection(String collectionId);

    @Query("SELECT * FROM quizzes WHERE id = :quizId")
    Quiz getQuizById(String quizId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuiz(Quiz quiz);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertQuizzes(List<Quiz> quizzes);

    @Update
    void updateQuiz(Quiz quiz);

    @Delete
    void deleteQuiz(Quiz quiz);

    @Query("DELETE FROM quizzes WHERE id = :quizId")
    void deleteQuizById(String quizId);

    @Query("DELETE FROM quizzes WHERE collectionId = :collectionId")
    void deleteQuizzesByCollectionId(String collectionId);

    @Query("SELECT COUNT(*) FROM quizzes WHERE collectionId = :collectionId")
    int getQuizCountForCollection(String collectionId);

    // User progress related queries

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUserProgress(UserProgress userProgress);

    @Update
    void updateUserProgress(UserProgress userProgress);

    @Query("SELECT * FROM user_progress WHERE quizId = :quizId AND userId = :userId")
    UserProgress getUserProgress(String quizId, String userId);

    @Query("SELECT * FROM user_progress WHERE userId = :userId")
    List<UserProgress> getAllUserProgress(String userId);

    @Query("SELECT * FROM quizzes WHERE id IN " +
           "(SELECT quizId FROM user_progress WHERE userId = :userId AND nextReviewTime <= :currentTime) " +
           "ORDER BY lastReviewTime ASC")
    List<Quiz> getQuizzesDueForReview(String userId, long currentTime);
}
