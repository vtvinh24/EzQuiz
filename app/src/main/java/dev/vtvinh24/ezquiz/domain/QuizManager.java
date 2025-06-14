package dev.vtvinh24.ezquiz.domain;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import dev.vtvinh24.ezquiz.domain.model.Answer;
import dev.vtvinh24.ezquiz.domain.model.Question;
import dev.vtvinh24.ezquiz.domain.model.Quiz;
import dev.vtvinh24.ezquiz.domain.repository.QuizRepository;

/**
 * Manager class responsible for quiz operations like creating, validating,
 * and processing quizzes before persistence.
 */
public class QuizManager {
    private static final String TAG = "QuizManager";

    private final QuizRepository quizRepository;

    /**
     * Constructs a new QuizManager.
     *
     * @param quizRepository Repository for quiz data operations.
     */
    public QuizManager(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    /**
     * Creates a new empty quiz with default values.
     *
     * @param author The author of the quiz.
     * @return A new quiz instance.
     */
    public Quiz createNewQuiz(String author) {
        Quiz quiz = new Quiz();
        quiz.setTitle("New Quiz");
        quiz.setDescription("Quiz description");
        quiz.setAuthor(author);
        quiz.setQuestions(new ArrayList<>());

        Log.d(TAG, "Created new quiz with ID: " + quiz.getId());
        return quiz;
    }

    /**
     * Saves a quiz to the repository.
     *
     * @param quiz The quiz to save.
     * @return The saved quiz with updated information.
     */
    public Quiz saveQuiz(Quiz quiz) {
        if (quiz == null) {
            Log.e(TAG, "Cannot save null quiz");
            return null;
        }

        // Update timestamps
        quiz.setUpdatedAt(System.currentTimeMillis());

        // Validate quiz before saving
        List<String> validationErrors = validateQuiz(quiz);
        if (!validationErrors.isEmpty()) {
            Log.e(TAG, "Quiz validation failed: " + validationErrors);
            return null;
        }

        // Save to repository
        Quiz savedQuiz = quizRepository.saveQuiz(quiz);
        Log.d(TAG, "Saved quiz with ID: " + quiz.getId());
        return savedQuiz;
    }

    /**
     * Validates a quiz for completeness and correctness.
     *
     * @param quiz The quiz to validate.
     * @return List of validation error messages (empty if valid).
     */
    public List<String> validateQuiz(Quiz quiz) {
        List<String> errors = new ArrayList<>();

        // Check basic quiz properties
        if (quiz == null) {
            errors.add("Quiz is null");
            return errors;
        }

        if (quiz.getTitle() == null || quiz.getTitle().trim().isEmpty()) {
            errors.add("Quiz must have a title");
        }

        // Check questions
        if (quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            errors.add("Quiz must have at least one question");
            return errors; // Return early as other validations need questions
        }

        // Validate each question
        for (int i = 0; i < quiz.getQuestions().size(); i++) {
            Question question = quiz.getQuestions().get(i);

            // Question text is required
            if (question.getText() == null || question.getText().trim().isEmpty()) {
                errors.add("Question " + (i + 1) + ": Text is required");
            }

            // Points must be positive
            if (question.getPoints() <= 0) {
                errors.add("Question " + (i + 1) + ": Points must be greater than 0");
            }

            // Check answers based on question type
            if (question.getAnswers() == null || question.getAnswers().isEmpty()) {
                errors.add("Question " + (i + 1) + ": Must have at least one answer");
                continue; // Skip remaining answer checks
            }

            boolean hasCorrectAnswer = false;
            for (Answer answer : question.getAnswers()) {
                if (answer.isCorrect()) {
                    hasCorrectAnswer = true;
                }

                // Answer text is required
                if (answer.getText() == null || answer.getText().trim().isEmpty()) {
                    errors.add("Question " + (i + 1) + ": Answer text is required");
                }
            }

            // Check for at least one correct answer
            if (!hasCorrectAnswer) {
                errors.add("Question " + (i + 1) + ": Must have at least one correct answer");
            }

            // Specific checks based on question type
            if (question.getType() == Question.QuestionType.SINGLE_CHOICE) {
                int correctCount = 0;
                for (Answer answer : question.getAnswers()) {
                    if (answer.isCorrect()) {
                        correctCount++;
                    }
                }

                if (correctCount != 1) {
                    errors.add("Question " + (i + 1) + ": Single choice questions must have exactly one correct answer");
                }
            }
            else if (question.getType() == Question.QuestionType.TRUE_FALSE) {
                if (question.getAnswers().size() != 2) {
                    errors.add("Question " + (i + 1) + ": True/False questions must have exactly two answers");
                }
            }
        }

        return errors;
    }

    /**
     * Adds a new question to a quiz.
     *
     * @param quiz The quiz to add the question to.
     * @param questionText The text of the question.
     * @param questionType The type of question.
     * @param points The points value of the question.
     * @return The updated quiz with the new question.
     */
    public Quiz addQuestion(Quiz quiz, String questionText, Question.QuestionType questionType, int points) {
        if (quiz == null) {
            Log.e(TAG, "Cannot add question to null quiz");
            return null;
        }

        // Create a new question
        Question question = new Question();
        question.setId(UUID.randomUUID().toString());
        question.setText(questionText);
        question.setType(questionType);
        question.setPoints(points);
        question.setAnswers(new ArrayList<>());

        // Add to quiz
        if (quiz.getQuestions() == null) {
            quiz.setQuestions(new ArrayList<>());
        }
        quiz.getQuestions().add(question);

        Log.d(TAG, "Added question to quiz " + quiz.getId() + ": " + question.getId());
        return quiz;
    }

    /**
     * Adds an answer to a question in a quiz.
     *
     * @param quiz The quiz containing the question.
     * @param questionIndex The index of the question to add the answer to.
     * @param answerText The text of the answer.
     * @param isCorrect Whether this answer is correct.
     * @return The updated quiz with the new answer.
     */
    public Quiz addAnswer(Quiz quiz, int questionIndex, String answerText, boolean isCorrect) {
        if (quiz == null || quiz.getQuestions() == null ||
            questionIndex < 0 || questionIndex >= quiz.getQuestions().size()) {
            Log.e(TAG, "Cannot add answer: invalid quiz or question index");
            return null;
        }

        // Create a new answer
        Answer answer = new Answer();
        answer.setId(UUID.randomUUID().toString());
        answer.setText(answerText);
        answer.setCorrect(isCorrect);

        // Add to question
        Question question = quiz.getQuestions().get(questionIndex);
        if (question.getAnswers() == null) {
            question.setAnswers(new ArrayList<>());
        }
        question.getAnswers().add(answer);

        Log.d(TAG, "Added answer to question " + question.getId() + ": " + answer.getId());
        return quiz;
    }

    /**
     * Gets a quiz by its ID.
     *
     * @param quizId The ID of the quiz to retrieve.
     * @return The quiz with the specified ID or null if not found.
     */
    public Quiz getQuizById(String quizId) {
        return quizRepository.getQuizById(quizId);
    }

    /**
     * Gets all quizzes from the repository.
     *
     * @return List of all quizzes.
     */
    public List<Quiz> getAllQuizzes() {
        return quizRepository.getAllQuizzes();
    }

    /**
     * Deletes a quiz by its ID.
     *
     * @param quizId The ID of the quiz to delete.
     * @return True if deletion was successful, false otherwise.
     */
    public boolean deleteQuiz(String quizId) {
        return quizRepository.deleteQuiz(quizId);
    }

    /**
     * Duplicates an existing quiz with a new ID.
     *
     * @param quizId The ID of the quiz to duplicate.
     * @return The new duplicate quiz or null if the original wasn't found.
     */
    public Quiz duplicateQuiz(String quizId) {
        Quiz originalQuiz = quizRepository.getQuizById(quizId);
        if (originalQuiz == null) {
            Log.e(TAG, "Cannot duplicate: quiz not found with ID: " + quizId);
            return null;
        }

        // Create new quiz with same properties but new ID
        Quiz duplicateQuiz = new Quiz();
        duplicateQuiz.setTitle(originalQuiz.getTitle() + " (Copy)");
        duplicateQuiz.setDescription(originalQuiz.getDescription());
        duplicateQuiz.setAuthor(originalQuiz.getAuthor());
        duplicateQuiz.setCreatedAt(System.currentTimeMillis());
        duplicateQuiz.setUpdatedAt(System.currentTimeMillis());

        // Duplicate all questions and answers
        List<Question> duplicatedQuestions = new ArrayList<>();
        if (originalQuiz.getQuestions() != null) {
            for (Question originalQuestion : originalQuiz.getQuestions()) {
                Question duplicatedQuestion = new Question();
                duplicatedQuestion.setId(UUID.randomUUID().toString());
                duplicatedQuestion.setText(originalQuestion.getText());
                duplicatedQuestion.setType(originalQuestion.getType());
                duplicatedQuestion.setPoints(originalQuestion.getPoints());

                // Duplicate answers
                List<Answer> duplicatedAnswers = new ArrayList<>();
                if (originalQuestion.getAnswers() != null) {
                    for (Answer originalAnswer : originalQuestion.getAnswers()) {
                        Answer duplicatedAnswer = new Answer();
                        duplicatedAnswer.setId(UUID.randomUUID().toString());
                        duplicatedAnswer.setText(originalAnswer.getText());
                        duplicatedAnswer.setCorrect(originalAnswer.isCorrect());
                        duplicatedAnswers.add(duplicatedAnswer);
                    }
                }
                duplicatedQuestion.setAnswers(duplicatedAnswers);
                duplicatedQuestions.add(duplicatedQuestion);
            }
        }
        duplicateQuiz.setQuestions(duplicatedQuestions);

        // Save duplicate to repository
        duplicateQuiz = quizRepository.saveQuiz(duplicateQuiz);
        Log.d(TAG, "Duplicated quiz " + quizId + " to new quiz " + duplicateQuiz.getId());
        return duplicateQuiz;
    }

    /**
     * Creates a simplified version of a quiz (e.g., for previews or listings).
     *
     * @param quiz The quiz to simplify.
     * @return A simplified version of the quiz.
     */
    public Quiz createQuizPreview(Quiz quiz) {
        if (quiz == null) {
            return null;
        }

        Quiz previewQuiz = new Quiz();
        previewQuiz.setId(quiz.getId());
        previewQuiz.setTitle(quiz.getTitle());
        previewQuiz.setDescription(quiz.getDescription());
        previewQuiz.setAuthor(quiz.getAuthor());
        previewQuiz.setCreatedAt(quiz.getCreatedAt());
        previewQuiz.setUpdatedAt(quiz.getUpdatedAt());

        // Include question count but not actual questions
        if (quiz.getQuestions() != null && !quiz.getQuestions().isEmpty()) {
            previewQuiz.setQuestions(Collections.emptyList());
        } else {
            previewQuiz.setQuestions(null);
        }

        return previewQuiz;
    }
}
