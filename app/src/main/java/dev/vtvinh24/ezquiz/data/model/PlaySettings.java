package dev.vtvinh24.ezquiz.data.model;

/**
 * Represents settings configuration for a quiz play session.
 */
public class PlaySettings {
    private boolean retryFailedQuizzes;
    private boolean shuffleQuizzes;
    private boolean useSpacedRepetition;
    private int repeatInterval; // Minimum time in minutes before repeating a quiz

    // Default constructor with recommended settings
    public PlaySettings() {
        this.retryFailedQuizzes = true;
        this.shuffleQuizzes = true;
        this.useSpacedRepetition = true;
        this.repeatInterval = 5; // Default 5 minutes
    }

    // Getters and setters
    public boolean isRetryFailedQuizzes() {
        return retryFailedQuizzes;
    }

    public void setRetryFailedQuizzes(boolean retryFailedQuizzes) {
        this.retryFailedQuizzes = retryFailedQuizzes;
    }

    public boolean isShuffleQuizzes() {
        return shuffleQuizzes;
    }

    public void setShuffleQuizzes(boolean shuffleQuizzes) {
        this.shuffleQuizzes = shuffleQuizzes;
    }

    public boolean isUseSpacedRepetition() {
        return useSpacedRepetition;
    }

    public void setUseSpacedRepetition(boolean useSpacedRepetition) {
        this.useSpacedRepetition = useSpacedRepetition;
    }

    public int getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(int repeatInterval) {
        this.repeatInterval = repeatInterval;
    }
}
