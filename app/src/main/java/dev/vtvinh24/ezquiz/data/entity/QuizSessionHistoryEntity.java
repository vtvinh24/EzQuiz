package dev.vtvinh24.ezquiz.data.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "quiz_session_history")
public class QuizSessionHistoryEntity {
    @PrimaryKey(autoGenerate = true)
    public long id;

    public long quizSetId;
    public String sessionType; // "PRACTICE", "TEST", "FLASHCARD"
    public int totalQuestions;
    public int correctAnswers;
    public int incorrectAnswers;
    public int skippedAnswers;
    public double scorePercentage;
    public long timeSpent; // in milliseconds
    public String detailedResults; // JSON string với kết quả chi tiết
    public boolean isCompleted;
    public long startTime;
    public long endTime;
    public long createdAt;

    public QuizSessionHistoryEntity(long quizSetId, String sessionType, int totalQuestions,
                                   int correctAnswers, int incorrectAnswers, int skippedAnswers,
                                   double scorePercentage, long timeSpent, String detailedResults,
                                   boolean isCompleted, long startTime, long endTime, long createdAt) {
        this.quizSetId = quizSetId;
        this.sessionType = sessionType;
        this.totalQuestions = totalQuestions;
        this.correctAnswers = correctAnswers;
        this.incorrectAnswers = incorrectAnswers;
        this.skippedAnswers = skippedAnswers;
        this.scorePercentage = scorePercentage;
        this.timeSpent = timeSpent;
        this.detailedResults = detailedResults;
        this.isCompleted = isCompleted;
        this.startTime = startTime;
        this.endTime = endTime;
        this.createdAt = createdAt;
    }
}
