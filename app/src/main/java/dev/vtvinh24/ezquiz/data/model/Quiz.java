package dev.vtvinh24.ezquiz.data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a quiz item in the application.
 */
public class Quiz {
    private String id;
    private String question;
    private QuizType type;
    private List<Option> options;
    private List<String> correctAnswers; // For text input and multiple choice
    private long lastReviewTime;
    private int consecutiveCorrectAnswers;
    private int difficulty; // 1-5 scale, with 5 being most difficult

    public Quiz() {
        this.id = UUID.randomUUID().toString();
        this.options = new ArrayList<>();
        this.correctAnswers = new ArrayList<>();
        this.lastReviewTime = System.currentTimeMillis();
        this.consecutiveCorrectAnswers = 0;
        this.difficulty = 3; // Default medium difficulty
    }

    public Quiz(String question, QuizType type) {
        this();
        this.question = question;
        this.type = type;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public QuizType getType() {
        return type;
    }

    public void setType(QuizType type) {
        this.type = type;
    }

    public List<Option> getOptions() {
        return options;
    }

    public void setOptions(List<Option> options) {
        this.options = options;
    }

    public void addOption(Option option) {
        this.options.add(option);
    }

    public List<String> getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(List<String> correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public void addCorrectAnswer(String correctAnswer) {
        this.correctAnswers.add(correctAnswer);
    }

    public long getLastReviewTime() {
        return lastReviewTime;
    }

    public void setLastReviewTime(long lastReviewTime) {
        this.lastReviewTime = lastReviewTime;
    }

    public int getConsecutiveCorrectAnswers() {
        return consecutiveCorrectAnswers;
    }

    public void setConsecutiveCorrectAnswers(int consecutiveCorrectAnswers) {
        this.consecutiveCorrectAnswers = consecutiveCorrectAnswers;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Checks if the provided answer(s) are correct.
     *
     * @param userAnswers List of answer strings provided by the user
     * @return true if the answer is correct, false otherwise
     */
    public boolean checkAnswer(List<String> userAnswers) {
        if (type == QuizType.TEXT_INPUT) {
            // For text input, we check if any correct answer matches (case insensitive)
            if (userAnswers.size() != 1) return false;

            String userAnswer = userAnswers.get(0).trim().toLowerCase();
            for (String correctAnswer : correctAnswers) {
                if (correctAnswer.trim().toLowerCase().equals(userAnswer)) {
                    return true;
                }
            }
            return false;
        } else if (type == QuizType.SINGLE_CHOICE) {
            // For single choice, we expect exactly one answer that matches the correct one
            return userAnswers.size() == 1 && correctAnswers.contains(userAnswers.get(0));
        } else {
            // For multiple choice, all correct answers must be selected and no incorrect ones
            return userAnswers.size() == correctAnswers.size() &&
                   userAnswers.containsAll(correctAnswers);
        }
    }

    /**
     * Updates spaced repetition data based on answer correctness
     *
     * @param correct whether the answer was correct
     */
    public void updateSpacedRepetitionData(boolean correct) {
        this.lastReviewTime = System.currentTimeMillis();
        if (correct) {
            this.consecutiveCorrectAnswers++;
            if (this.difficulty > 1) {
                this.difficulty--;
            }
        } else {
            this.consecutiveCorrectAnswers = 0;
            if (this.difficulty < 5) {
                this.difficulty++;
            }
        }
    }
}
