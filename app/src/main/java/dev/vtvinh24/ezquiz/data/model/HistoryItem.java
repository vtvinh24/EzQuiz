package dev.vtvinh24.ezquiz.data.model;

import dev.vtvinh24.ezquiz.data.entity.PracticeProgressEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSessionHistoryEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

import java.util.List;

public class HistoryItem {
    private final QuizSetEntity quizSet;
    private final PracticeProgressEntity progress;
    private final String collectionName;
    private final int totalQuestions;
    private final int completedQuestions;
    private final boolean isCompleted;
    private final String sessionResults;
    private final List<QuizSessionHistoryEntity> sessionHistory;
    private final boolean isAIGenerated;

    public HistoryItem(QuizSetEntity quizSet, PracticeProgressEntity progress, String collectionName,
                      int totalQuestions, List<QuizSessionHistoryEntity> sessionHistory, boolean isAIGenerated) {
        this.quizSet = quizSet;
        this.progress = progress;
        this.collectionName = collectionName;
        this.totalQuestions = totalQuestions;
        this.completedQuestions = progress != null ? progress.currentPosition : 0;
        this.isCompleted = progress != null && progress.currentPosition >= totalQuestions;
        this.sessionResults = progress != null ? progress.sessionResults : "";
        this.sessionHistory = sessionHistory;
        this.isAIGenerated = isAIGenerated;
    }

    public QuizSetEntity getQuizSet() { return quizSet; }
    public PracticeProgressEntity getProgress() { return progress; }
    public String getCollectionName() { return collectionName; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCompletedQuestions() { return completedQuestions; }
    public boolean isCompleted() { return isCompleted; }
    public String getSessionResults() { return sessionResults; }
    public List<QuizSessionHistoryEntity> getSessionHistory() { return sessionHistory; }
    public boolean isAIGenerated() { return isAIGenerated; }

    public int getProgressPercentage() {
        if (totalQuestions == 0) return 0;
        return (int) ((completedQuestions * 100.0) / totalQuestions);
    }

    public long getLastUpdated() {
        if (sessionHistory != null && !sessionHistory.isEmpty()) {
            return sessionHistory.get(0).createdAt;
        }
        return progress != null ? progress.updatedAt : quizSet.updatedAt;
    }

    public boolean canContinue() {
        return progress != null && !isCompleted;
    }

    public String getStatusText() {
        if (isCompleted) {
            return "Completed";
        } else if (progress != null && completedQuestions > 0) {
            return "In Progress (" + completedQuestions + "/" + totalQuestions + ")";
        } else {
            return "Not Started";
        }
    }

    public String getDetailedStats() {
        if (sessionHistory == null || sessionHistory.isEmpty()) {
            return "No attempts yet";
        }

        int practiceAttempts = 0;
        int testAttempts = 0;
        int flashcardAttempts = 0;
        double bestScore = 0.0;
        double averageScore = 0.0;
        int completedSessions = 0;

        for (QuizSessionHistoryEntity session : sessionHistory) {
            switch (session.sessionType) {
                case "PRACTICE":
                    practiceAttempts++;
                    break;
                case "TEST":
                    testAttempts++;
                    break;
                case "FLASHCARD":
                    flashcardAttempts++;
                    break;
            }

            if (session.isCompleted) {
                completedSessions++;
                bestScore = Math.max(bestScore, session.scorePercentage);
                averageScore += session.scorePercentage;
            }
        }

        if (completedSessions > 0) {
            averageScore /= completedSessions;
        }

        StringBuilder stats = new StringBuilder();
        if (practiceAttempts > 0) stats.append("Practice: ").append(practiceAttempts).append(" • ");
        if (testAttempts > 0) stats.append("Test: ").append(testAttempts).append(" • ");
        if (flashcardAttempts > 0) stats.append("Flashcard: ").append(flashcardAttempts).append(" • ");

        if (completedSessions > 0) {
            stats.append(String.format("Best: %.1f%% • Avg: %.1f%%", bestScore, averageScore));
        }

        return stats.toString().replaceAll(" • $", "");
    }

    public QuizSessionHistoryEntity getLatestSession() {
        if (sessionHistory == null || sessionHistory.isEmpty()) {
            return null;
        }
        return sessionHistory.get(0);
    }
}
