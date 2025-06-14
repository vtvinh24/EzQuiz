package dev.vtvinh24.ezquiz.data.model;

import java.util.List;

public class QuizSet {
  private final String name;
  private final String description;
  private final List<Quiz> quizzes;
  private final long createdAt;
  private final long updatedAt;
  private final boolean archived;
  private final int difficulty;

  public QuizSet(String name, String description, List<Quiz> quizzes,
                 long createdAt, long updatedAt, boolean archived, int difficulty) {
    this.name = name;
    this.description = description;
    this.quizzes = quizzes;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.archived = archived;
    this.difficulty = difficulty;
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public List<Quiz> getQuizzes() {
    return quizzes;
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
