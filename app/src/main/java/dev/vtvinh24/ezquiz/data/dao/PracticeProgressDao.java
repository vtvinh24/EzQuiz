package dev.vtvinh24.ezquiz.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import dev.vtvinh24.ezquiz.data.entity.PracticeProgressEntity;

import java.util.List;

@Dao
public interface PracticeProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertPracticeProgress(PracticeProgressEntity progress);

    @Update
    void updatePracticeProgress(PracticeProgressEntity progress);

    @Delete
    void deletePracticeProgress(PracticeProgressEntity progress);

    @Query("SELECT * FROM practice_progress WHERE quizSetId = :quizSetId LIMIT 1")
    PracticeProgressEntity getPracticeProgress(long quizSetId);

    @Query("DELETE FROM practice_progress WHERE quizSetId = :quizSetId")
    void deletePracticeProgressBySetId(long quizSetId);

    @Query("SELECT EXISTS(SELECT 1 FROM practice_progress WHERE quizSetId = :quizSetId)")
    boolean hasPracticeProgress(long quizSetId);

    @Query("SELECT * FROM practice_progress ORDER BY updatedAt DESC")
    List<PracticeProgressEntity> getAllPracticeProgress();

    @Query("SELECT * FROM practice_progress WHERE currentPosition < LENGTH(quizIds) - LENGTH(REPLACE(quizIds, ',', '')) ORDER BY updatedAt DESC")
    List<PracticeProgressEntity> getIncompletePracticeProgress();

    @Query("SELECT * FROM practice_progress WHERE currentPosition >= LENGTH(quizIds) - LENGTH(REPLACE(quizIds, ',', '')) ORDER BY updatedAt DESC")
    List<PracticeProgressEntity> getCompletedPracticeProgress();
}
