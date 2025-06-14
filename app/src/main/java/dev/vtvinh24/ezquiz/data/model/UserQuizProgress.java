package dev.vtvinh24.ezquiz.data.model;

public class UserQuizProgress {
  public boolean isPinned;
  public boolean isFavorite;
  public long lastAttemptedAt;
  public int attemptCount;
  public int correctAttemptCount;
  public float successRate;
  public boolean archived;
  public String notes;
  public int difficulty;

  public UserQuizProgress() {
  }

  public UserQuizProgress(boolean isPinned, boolean isFavorite, long lastAttemptedAt, int attemptCount, int correctAttemptCount, float successRate, boolean archived, String notes, int difficulty) {
    this.isPinned = isPinned;
    this.isFavorite = isFavorite;
    this.lastAttemptedAt = lastAttemptedAt;
    this.attemptCount = attemptCount;
    this.correctAttemptCount = correctAttemptCount;
    this.successRate = successRate;
    this.archived = archived;
    this.notes = notes;
    this.difficulty = difficulty;
  }
}

