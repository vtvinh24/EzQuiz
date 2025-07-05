package dev.vtvinh24.ezquiz.ui;

import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

public class QuizSetWithCounts {
  public final QuizSetEntity set;
  public final int quizCount;
  public final int flashcardCount;

  public QuizSetWithCounts(QuizSetEntity set, int quizCount, int flashcardCount) {
    this.set = set;
    this.quizCount = quizCount;
    this.flashcardCount = flashcardCount;
  }
}
