package dev.vtvinh24.ezquiz.data.model;

import dev.vtvinh24.ezquiz.data.entity.QuizSessionHistoryEntity;

import java.util.List;

public class DetailedSessionResult {
    private final String sessionType;
    private final int totalQuestions;
    private final int correctAnswers;
    private final int incorrectAnswers;
    private final int skippedAnswers;
    private final double scorePercentage;
    private final long timeSpent;
    private final boolean isCompleted;
    private final long startTime;
    private final long endTime;
    private final List<QuestionResult> questionResults;

    public DetailedSessionResult(QuizSessionHistoryEntity session, List<QuestionResult> questionResults) {
        this.sessionType = session.sessionType;
        this.totalQuestions = session.totalQuestions;
        this.correctAnswers = session.correctAnswers;
        this.incorrectAnswers = session.incorrectAnswers;
        this.skippedAnswers = session.skippedAnswers;
        this.scorePercentage = session.scorePercentage;
        this.timeSpent = session.timeSpent;
        this.isCompleted = session.isCompleted;
        this.startTime = session.startTime;
        this.endTime = session.endTime;
        this.questionResults = questionResults;
    }

    public String getSessionType() { return sessionType; }
    public int getTotalQuestions() { return totalQuestions; }
    public int getCorrectAnswers() { return correctAnswers; }
    public int getIncorrectAnswers() { return incorrectAnswers; }
    public int getSkippedAnswers() { return skippedAnswers; }
    public double getScorePercentage() { return scorePercentage; }
    public long getTimeSpent() { return timeSpent; }
    public boolean isCompleted() { return isCompleted; }
    public long getStartTime() { return startTime; }
    public long getEndTime() { return endTime; }
    public List<QuestionResult> getQuestionResults() { return questionResults; }

    public String getFormattedScore() {
        return String.format("%.1f%% (%d/%d)", scorePercentage, correctAnswers, totalQuestions);
    }

    public String getFormattedTime() {
        long seconds = timeSpent / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    public static class QuestionResult {
        private final long questionId;
        private final String questionText;
        private final String userAnswer;
        private final String correctAnswer;
        private final boolean isCorrect;
        private final long timeSpent;

        public QuestionResult(long questionId, String questionText, String userAnswer,
                            String correctAnswer, boolean isCorrect, long timeSpent) {
            this.questionId = questionId;
            this.questionText = questionText;
            this.userAnswer = userAnswer;
            this.correctAnswer = correctAnswer;
            this.isCorrect = isCorrect;
            this.timeSpent = timeSpent;
        }

        public long getQuestionId() { return questionId; }
        public String getQuestionText() { return questionText; }
        public String getUserAnswer() { return userAnswer; }
        public String getCorrectAnswer() { return correctAnswer; }
        public boolean isCorrect() { return isCorrect; }
        public long getTimeSpent() { return timeSpent; }
    }
}
