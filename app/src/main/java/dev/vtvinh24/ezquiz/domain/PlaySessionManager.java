package dev.vtvinh24.ezquiz.domain;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import dev.vtvinh24.ezquiz.domain.model.Answer;
import dev.vtvinh24.ezquiz.domain.model.Question;
import dev.vtvinh24.ezquiz.domain.model.Quiz;

/**
 * Manager class responsible for handling quiz play sessions.
 * Tracks user progress, answers, and scoring during a quiz session.
 */
public class PlaySessionManager {
    private static final String TAG = "PlaySessionManager";

    private String sessionId;
    private Quiz currentQuiz;
    private int currentQuestionIndex;
    private int totalScore;
    private long startTime;
    private long endTime;
    private final Map<String, List<String>> userAnswers; // Question ID -> Selected Answer IDs
    private final Map<String, Boolean> questionResults; // Question ID -> Correct (true/false)

    /**
     * Constructs a new play session manager.
     */
    public PlaySessionManager() {
        this.sessionId = UUID.randomUUID().toString();
        this.currentQuestionIndex = 0;
        this.totalScore = 0;
        this.userAnswers = new HashMap<>();
        this.questionResults = new HashMap<>();
    }

    /**
     * Starts a new quiz session.
     *
     * @param quiz The quiz to play.
     * @return True if session started successfully, false otherwise.
     */
    public boolean startQuiz(Quiz quiz) {
        if (quiz == null || quiz.getQuestions() == null || quiz.getQuestions().isEmpty()) {
            Log.e(TAG, "Cannot start quiz: quiz is null or has no questions");
            return false;
        }

        this.sessionId = UUID.randomUUID().toString();
        this.currentQuiz = quiz;
        this.currentQuestionIndex = 0;
        this.totalScore = 0;
        this.userAnswers.clear();
        this.questionResults.clear();
        this.startTime = System.currentTimeMillis();
        this.endTime = 0;

        Log.d(TAG, "Started quiz session: " + sessionId + " for quiz: " + quiz.getTitle());
        return true;
    }

    /**
     * Submits an answer for the current question.
     *
     * @param answerIds List of selected answer IDs.
     * @return True if the answer was correct, false otherwise.
     */
    public boolean submitAnswer(List<String> answerIds) {
        if (currentQuiz == null || currentQuestionIndex >= currentQuiz.getQuestions().size()) {
            Log.e(TAG, "Cannot submit answer: no active quiz or invalid question index");
            return false;
        }

        Question currentQuestion = currentQuiz.getQuestions().get(currentQuestionIndex);
        String questionId = currentQuestion.getId();

        // Store user's answer
        userAnswers.put(questionId, new ArrayList<>(answerIds));

        // Evaluate if the answer is correct
        boolean isCorrect = checkAnswerCorrectness(currentQuestion, answerIds);
        questionResults.put(questionId, isCorrect);

        // Update score if correct
        if (isCorrect) {
            totalScore += currentQuestion.getPoints();
        }

        // Move to next question
        currentQuestionIndex++;

        // Check if quiz is completed
        if (currentQuestionIndex >= currentQuiz.getQuestions().size()) {
            endTime = System.currentTimeMillis();
            Log.d(TAG, "Quiz completed. Final score: " + totalScore);
        }

        return isCorrect;
    }

    /**
     * Checks if the selected answers are correct for a question.
     *
     * @param question The question being answered.
     * @param answerIds The IDs of the selected answers.
     * @return True if the answer is correct, false otherwise.
     */
    private boolean checkAnswerCorrectness(Question question, List<String> answerIds) {
        // This is a simple placeholder implementation
        // In a real app, this would have more sophisticated logic based on question type

        if (question.getType() == Question.QuestionType.SINGLE_CHOICE) {
            // For single choice, there should be exactly one answer selected
            if (answerIds.size() != 1) {
                return false;
            }

            // Find the selected answer and check if it's correct
            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(answerIds.get(0))) {
                    return answer.isCorrect();
                }
            }
            return false;
        }
        else if (question.getType() == Question.QuestionType.MULTIPLE_CHOICE) {
            // For multiple choice, all correct answers must be selected and no incorrect ones
            Set<String> correctAnswerIds = new HashSet<>();
            Set<String> incorrectAnswerIds = new HashSet<>();

            // Categorize all answers as correct or incorrect
            for (Answer answer : question.getAnswers()) {
                if (answer.isCorrect()) {
                    correctAnswerIds.add(answer.getId());
                } else {
                    incorrectAnswerIds.add(answer.getId());
                }
            }

            // Check if user selected all correct answers and no incorrect ones
            Set<String> selectedAnswerIds = new HashSet<>(answerIds);
            return selectedAnswerIds.containsAll(correctAnswerIds) &&
                   !selectedAnswerIds.removeAll(incorrectAnswerIds);
        }
        else if (question.getType() == Question.QuestionType.TRUE_FALSE) {
            // Treat this similar to single choice
            if (answerIds.size() != 1) {
                return false;
            }

            for (Answer answer : question.getAnswers()) {
                if (answer.getId().equals(answerIds.get(0))) {
                    return answer.isCorrect();
                }
            }
            return false;
        }

        // Default case: incorrect
        Log.d(TAG, "Question type not fully implemented: " + question.getType());
        return false;
    }

    /**
     * Gets the current question.
     *
     * @return The current question or null if no active quiz or quiz is completed.
     */
    public Question getCurrentQuestion() {
        if (currentQuiz == null ||
            currentQuestionIndex < 0 ||
            currentQuestionIndex >= currentQuiz.getQuestions().size()) {
            return null;
        }
        return currentQuiz.getQuestions().get(currentQuestionIndex);
    }

    /**
     * Gets the total score accumulated so far.
     *
     * @return The current total score.
     */
    public int getTotalScore() {
        return totalScore;
    }

    /**
     * Gets the maximum possible score for the quiz.
     *
     * @return The maximum possible score or 0 if no active quiz.
     */
    public int getMaxPossibleScore() {
        if (currentQuiz == null || currentQuiz.getQuestions() == null) {
            return 0;
        }

        int maxScore = 0;
        for (Question question : currentQuiz.getQuestions()) {
            maxScore += question.getPoints();
        }
        return maxScore;
    }

    /**
     * Calculates the percentage score (0-100).
     *
     * @return The percentage score or 0 if no active quiz.
     */
    public int getScorePercentage() {
        int maxScore = getMaxPossibleScore();
        if (maxScore == 0) {
            return 0;
        }
        return (int) (((double) totalScore / maxScore) * 100);
    }

    /**
     * Checks if the quiz is completed.
     *
     * @return True if the quiz is completed, false otherwise.
     */
    public boolean isQuizCompleted() {
        return currentQuiz != null &&
               currentQuestionIndex >= currentQuiz.getQuestions().size();
    }

    /**
     * Gets the time elapsed in milliseconds.
     *
     * @return The time elapsed or 0 if no active quiz.
     */
    public long getTimeElapsed() {
        if (startTime == 0) {
            return 0;
        }

        if (endTime > 0) {
            return endTime - startTime;
        }

        return System.currentTimeMillis() - startTime;
    }

    /**
     * Gets the session ID.
     *
     * @return The current session ID.
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Gets the current quiz.
     *
     * @return The current quiz.
     */
    public Quiz getCurrentQuiz() {
        return currentQuiz;
    }

    /**
     * Gets the progress as a percentage (0-100).
     *
     * @return The progress percentage or 0 if no active quiz.
     */
    public int getProgressPercentage() {
        if (currentQuiz == null || currentQuiz.getQuestions() == null ||
            currentQuiz.getQuestions().isEmpty()) {
            return 0;
        }

        return (int) (((double) currentQuestionIndex / currentQuiz.getQuestions().size()) * 100);
    }
}
