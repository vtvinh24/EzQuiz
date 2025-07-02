// extractorInterface.js
// Unified interface for all extractors

export class ExtractorInterface {
  /**
   * Returns JSON of flashcards, or [] if not available
   */
  getFlashcards() {
    throw new Error("getFlashcards() not implemented");
  }

  /**
   * Returns JSON of quizzes, or [] if not available
   */
  getQuizzes() {
    throw new Error("getQuizzes() not implemented");
  }

  /**
   * Returns the URL of the current site
   */
  getURL() {
    throw new Error("getURL() not implemented");
  }
}
