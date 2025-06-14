package dev.vtvinh24.ezquiz.data.model;

import java.util.List;

public class QuizCollection {
  private final List<QuizSet> quizSets;
  private final long createdAt;
  private final long updatedAt;
  private final boolean archived;
  private final int difficulty;

  public QuizCollection(List<QuizSet> quizSets, long createdAt, long updatedAt, boolean archived, int difficulty) {
    this.quizSets = quizSets;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.archived = archived;
    this.difficulty = difficulty;
  }

  public List<QuizSet> getQuizSets() {
    return quizSets;
  }

  public long getCreatedAt() {
    return createdAt;
  }

  public long getUpdatedAt() {
    return updatedAt;
  }

  public boolean isArchived() {
    return archived;
  }

  public int getDifficulty() {
    return difficulty;
  }
}
