package dev.vtvinh24.ezquiz.data.model;

import java.util.List;

public class Quiz {
  private final String question;
  private final List<String> answers;
  private final List<Integer> correctAnswerIndices;
  private final Type type;
  private final long createdAt;
  private final long updatedAt;
  private final boolean archived;
  private final int difficulty;

  public Quiz(String question, List<String> answers, List<Integer> correctAnswerIndices, Type type,
              long createdAt, long updatedAt, boolean archived, int difficulty) {
    this.question = question;
    this.answers = answers;
    this.correctAnswerIndices = correctAnswerIndices;
    this.type = type;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.archived = archived;
    this.difficulty = difficulty;
  }

  public String getQuestion() {
    return question;
  }

  public List<String> getAnswers() {
    return answers;
  }

  public List<Integer> getCorrectAnswerIndices() {
    return correctAnswerIndices;
  }

  public Type getType() {
    return type;
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

  public enum Type {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE
  }
}
