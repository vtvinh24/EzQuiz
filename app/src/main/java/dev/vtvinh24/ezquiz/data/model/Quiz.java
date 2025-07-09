package dev.vtvinh24.ezquiz.data.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class Quiz implements Serializable {
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

  public String getAnswer() {
    if (answers != null && !answers.isEmpty()) {
      if (correctAnswerIndices != null && !correctAnswerIndices.isEmpty()) {
        int correctIndex = correctAnswerIndices.get(0);
        if (correctIndex >= 0 && correctIndex < answers.size()) {
          return answers.get(correctIndex);
        }
      }
      return answers.get(0);
    }
    return "";
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
  @Override
  public boolean equals(Object o) {
    // 1. Kiểm tra xem có phải là chính nó không
    if (this == o) return true;
    // 2. Kiểm tra xem có null không hoặc có khác lớp không
    if (o == null || getClass() != o.getClass()) return false;
    // 3. Ép kiểu và so sánh từng trường
    Quiz quiz = (Quiz) o;
    return archived == quiz.archived &&
            difficulty == quiz.difficulty &&
            Objects.equals(question, quiz.question) &&
            Objects.equals(answers, quiz.answers) &&
            Objects.equals(correctAnswerIndices, quiz.correctAnswerIndices) &&
            type == quiz.type &&
            Objects.equals(createdAt, quiz.createdAt) &&
            Objects.equals(updatedAt, quiz.updatedAt);
  }

  @Override
  public int hashCode() {
    // Tạo một hash code duy nhất dựa trên các trường đã dùng trong equals()
    return Objects.hash(question, answers, correctAnswerIndices, type, createdAt, updatedAt, archived, difficulty);
  }

  public enum Type {
    SINGLE_CHOICE,
    MULTIPLE_CHOICE,
    FLASHCARD,

  }
}
