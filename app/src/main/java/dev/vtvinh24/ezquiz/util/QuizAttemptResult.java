package dev.vtvinh24.ezquiz.util;

import java.io.Serializable;

public class QuizAttemptResult implements Serializable { // Implement Serializable để truyền qua Intent
  public final long quizId;
  public final boolean isCorrect;
  // Bạn có thể thêm các trường khác sau này, ví dụ: List<Integer> userAnswers;

  public QuizAttemptResult(long quizId, boolean isCorrect) {
    this.quizId = quizId;
    this.isCorrect = isCorrect;
  }
}