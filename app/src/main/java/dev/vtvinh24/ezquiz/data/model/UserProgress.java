package dev.vtvinh24.ezquiz.data.model;

/**
 * Represents user progress data for spaced repetition learning.
 * Tracks metrics needed for effective quiz scheduling.
 */
public class UserProgress {
    private String userId;
    private String quizId;
    private int timesAttempted;
    private int timesCorrect;
    private long lastAttemptTime;
    private int consecutiveCorrectAnswers;
    private long nextReviewTime;

    public UserProgress() {
    }

    public UserProgress(String userId, String quizId) {
        this.userId = userId;
        this.quizId = quizId;
        this.timesAttempted = 0;
        this.timesCorrect = 0;
        this.lastAttemptTime = 0;
        this.consecutiveCorrectAnswers = 0;
        this.nextReviewTime = System.currentTimeMillis();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getQuizId() {
        return quizId;
    }

    public void setQuizId(String quizId) {
        this.quizId = quizId;
    }

    public int getTimesAttempted() {
        return timesAttempted;
    }

    public void setTimesAttempted(int timesAttempted) {
        this.timesAttempted = timesAttempted;
    }

    public int getTimesCorrect() {
        return timesCorrect;
    }

    public void setTimesCorrect(int timesCorrect) {
        this.timesCorrect = timesCorrect;
    }

    public long getLastAttemptTime() {
        return lastAttemptTime;
    }

    public void setLastAttemptTime(long lastAttemptTime) {
        this.lastAttemptTime = lastAttemptTime;
    }

    public int getConsecutiveCorrectAnswers() {
        return consecutiveCorrectAnswers;
    }

    public void setConsecutiveCorrectAnswers(int consecutiveCorrectAnswers) {
        this.consecutiveCorrectAnswers = consecutiveCorrectAnswers;
    }

    public long getNextReviewTime() {
        return nextReviewTime;
    }

    public void setNextReviewTime(long nextReviewTime) {
        this.nextReviewTime = nextReviewTime;
    }

    /**
     * Updates the progress data based on a new quiz attempt.
     *
     * @param isCorrect Whether the answer was correct
     */
    public void updateAfterAttempt(boolean isCorrect) {
        this.timesAttempted++;
        this.lastAttemptTime = System.currentTimeMillis();

        if (isCorrect) {
            this.timesCorrect++;
            this.consecutiveCorrectAnswers++;
            // Calculate next review time using spaced repetition algorithm
            calculateNextReviewTime();
        } else {
            this.consecutiveCorrectAnswers = 0;
            // Review soon if answer was wrong
            this.nextReviewTime = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes
        }
    }

    /**
     * Calculates the next review time based on spaced repetition principles.
     * Uses a simplified version of the SM-2 algorithm.
     */
    private void calculateNextReviewTime() {
        // Base interval in milliseconds (1 day)
        long baseInterval = 24 * 60 * 60 * 1000;

        // Calculate interval based on consecutive correct answers
        // 1: 1 day, 2: 3 days, 3: 7 days, 4: 14 days, 5+: 30 days
        int multiplier;
        switch (this.consecutiveCorrectAnswers) {
            case 1:
                multiplier = 1;
                break;
            case 2:
                multiplier = 3;
                break;
            case 3:
                multiplier = 7;
                break;
            case 4:
                multiplier = 14;
                break;
            default:
                multiplier = 30;
                break;
        }

        this.nextReviewTime = System.currentTimeMillis() + (baseInterval * multiplier);
    }

    /**
     * Check if this quiz is due for review.
     *
     * @return true if the quiz should be reviewed, false otherwise
     */
    public boolean isDueForReview() {
        return System.currentTimeMillis() >= this.nextReviewTime;
    }

    /**
     * Calculate the success rate for this quiz.
     *
     * @return Success rate as a percentage between 0 and 100
     */
    public int getSuccessRate() {
        if (timesAttempted == 0) {
            return 0;
        }
        return (timesCorrect * 100) / timesAttempted;
    }
}
