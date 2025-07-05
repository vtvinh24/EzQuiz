package dev.vtvinh24.ezquiz.data.model;

import java.io.Serializable;

public class FlashcardResult implements Serializable {
  private final long quizId;
  private final String question;
  private final boolean wasKnown;

  public FlashcardResult(long quizId, String question, boolean wasKnown) {
    this.quizId = quizId;
    this.question = question;
    this.wasKnown = wasKnown;
  }

  public long getQuizId() {
    return quizId;
  }

  public String getQuestion() {
    return question;
  }

  public boolean wasKnown() {
    return wasKnown;
  }
}