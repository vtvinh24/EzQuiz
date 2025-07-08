import { ExtractorInterface } from "./extractorInterface.js";
import { extractTextFromElement, waitForElement, getCurrentURL, sleep } from "./utils.js";

class QuizizzExtractor extends ExtractorInterface {
  constructor() {
    super();
    this.quizzes = [];
    this.isExtracting = false;
  }

  async _extract() {
    if (this.isExtracting) return;
    this.isExtracting = true;

    try {
      console.log("QuizizzExtractor: Starting extraction...");

      // Find all question containers
      const questionCards = document.querySelectorAll('[data-testid="adp-qd-card"]');
      console.log(`QuizizzExtractor: Found ${questionCards.length} question cards`);

      for (let i = 0; i < questionCards.length; i++) {
        const questionCard = questionCards[i];
        console.log(`QuizizzExtractor: Processing question ${i + 1}/${questionCards.length}`);

        // Extract question text
        const questionElement = questionCard.querySelector('[data-testid="qdc-inner-card-question"] p');
        if (!questionElement) {
          console.log(`QuizizzExtractor: No question element found for question ${i + 1}`);
          continue;
        }

        const questionText = extractTextFromElement(questionElement);
        if (!questionText) {
          console.log(`QuizizzExtractor: No question text extracted for question ${i + 1}`);
          continue;
        }

        console.log(`QuizizzExtractor: Question ${i + 1}: "${questionText.substring(0, 50)}..."`);

        // Check if answers are already loaded
        const answerContainer = questionCard.querySelector('[data-testid="qdc-inner-card-question-answer"]');

        if (!answerContainer) {
          console.log(`QuizizzExtractor: No answers loaded for question ${i + 1}, clicking to load...`);
          // Need to click to load answers
          const clickableElement = questionCard.querySelector('[data-testid="qdc-inner-card-question"]');
          if (clickableElement) {
            clickableElement.click();
            // Wait for answers to load
            await waitForElement(questionCard, '[data-testid="qdc-inner-card-question-answer"]');
            await sleep(200); // Additional wait for content to fully load
          }
        } else {
          console.log(`QuizizzExtractor: Answers already loaded for question ${i + 1}`);
        }

        // Extract answers
        const answers = this._extractAnswers(questionCard);
        console.log(`QuizizzExtractor: Extracted ${answers.length} answers for question ${i + 1}`);

        if (answers && answers.length > 0) {
          this.quizzes.push({
            question: questionText,
            answers: answers,
            questionType: "multiple_choice",
          });
          console.log(`QuizizzExtractor: Added question ${i + 1} to quizzes`);
        }
      }

      console.log(`QuizizzExtractor: Extraction complete. Total questions: ${this.quizzes.length}`);
    } catch (error) {
      console.error("QuizizzExtractor: Error during extraction:", error);
    } finally {
      this.isExtracting = false;
    }
  }

  _extractAnswers(questionCard) {
    const answers = [];
    const answerElements = questionCard.querySelectorAll('[data-testid^="option-"]');

    console.log(`QuizizzExtractor: Found ${answerElements.length} answer elements`);

    answerElements.forEach((answerElement, index) => {
      const textElement = answerElement.querySelector("p");
      if (textElement) {
        const answerText = extractTextFromElement(textElement);

        // Check if this is the correct answer (has check-circle icon)
        const isCorrect = answerElement.closest(".flex").querySelector(".fa-check-circle") !== null;

        console.log(`QuizizzExtractor: Answer ${index + 1}: "${answerText}" (${isCorrect ? "correct" : "incorrect"})`);

        answers.push({
          text: answerText,
          isCorrect: isCorrect,
          index: index,
        });
      }
    });

    return answers;
  }

  async getQuizzes() {
    if (this.quizzes.length === 0) {
      await this._extract();
    }
    return this.quizzes;
  }

  getFlashcards() {
    // Quizizz doesn't have flashcards
    return [];
  }

  getURL() {
    return getCurrentURL();
  }
}

export default QuizizzExtractor;
