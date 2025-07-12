package dev.vtvinh24.ezquiz.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import dev.vtvinh24.ezquiz.data.converter.IntegerListConverter;

import java.util.List;

@Entity(tableName = "practice_progress")
@TypeConverters(IntegerListConverter.class)
public class PracticeProgressEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long quizSetId;
    public int currentPosition;
    public String quizIds;
    public String userAnswers;
    public String sessionResults;
    public long createdAt;
    public long updatedAt;

    public PracticeProgressEntity(long quizSetId, int currentPosition, String quizIds,
                                String userAnswers, String sessionResults, long createdAt, long updatedAt) {
        this.quizSetId = quizSetId;
        this.currentPosition = currentPosition;
        this.quizIds = quizIds;
        this.userAnswers = userAnswers;
        this.sessionResults = sessionResults;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
