import { ExtractorInterface } from "./extractorInterface.js";
import { getCurrentURL } from "./utils.js";

class QuizletExtractor extends ExtractorInterface {
  constructor() {
    super();
    this.flashcardJson = null;
    this.flashcards = [];
    this._extract();
  }

  _extract() {
    const ldJsonScripts = document.querySelectorAll('script[type="application/ld+json"]');
    ldJsonScripts.forEach((ldJson) => {
      try {
        let data = JSON.parse(ldJson.textContent);
        if (Array.isArray(data)) {
          data.forEach((item) => {
            if (item && Array.isArray(item.hasPart)) {
              item.hasPart.forEach((q) => {
                if (q.eduQuestionType === "Flashcard" && q.text && q.acceptedAnswer && q.acceptedAnswer.text) {
                  this.flashcards.push({ front: q.text, back: q.acceptedAnswer.text });
                }
              });
            }
          });
        } else if (data && Array.isArray(data.hasPart)) {
          data.hasPart.forEach((q) => {
            if (q.eduQuestionType === "Flashcard" && q.text && q.acceptedAnswer && q.acceptedAnswer.text) {
              this.flashcards.push({ front: q.text, back: q.acceptedAnswer.text });
            }
          });
        }
        if (!this.flashcardJson && data && Array.isArray(data.hasPart)) {
          this.flashcardJson = data;
        }
      } catch (e) {}
    });
  }

  getFlashcards() {
    return this.flashcards;
  }

  getQuizzes() {
    // Quizlet does not have quizzes in this context
    return [];
  }

  getURL() {
    return getCurrentURL();
  }
}

export default QuizletExtractor;
