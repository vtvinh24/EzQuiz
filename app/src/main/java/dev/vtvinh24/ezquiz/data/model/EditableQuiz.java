package dev.vtvinh24.ezquiz.data.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Mutable model for editing quiz questions
 * Supports dynamic modification of questions, answers, and types
 */
public class EditableQuiz implements Serializable {
    private String question;
    private List<String> answers;
    private List<Integer> correctAnswerIndices;
    private Quiz.Type type;
    private long id;
    private boolean isNew;

    public EditableQuiz() {
        this.question = "";
        this.answers = new ArrayList<>();
        this.correctAnswerIndices = new ArrayList<>();
        this.type = Quiz.Type.SINGLE_CHOICE;
        this.isNew = true;
        this.id = -1;
    }

    public EditableQuiz(GeneratedQuizItem item) {
        this.question = item.question != null ? item.question : "";
        this.answers = new ArrayList<>(item.answers != null ? item.answers : new ArrayList<>());
        this.correctAnswerIndices = new ArrayList<>(item.correctAnswerIndices != null ? item.correctAnswerIndices : new ArrayList<>());
        this.type = item.type != null ? item.type : Quiz.Type.SINGLE_CHOICE;
        this.isNew = false;
        this.id = -1;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers != null ? answers : new ArrayList<>();
    }

    public void addAnswer(String answer) {
        if (answers == null) {
            answers = new ArrayList<>();
        }
        answers.add(answer != null ? answer : "");
    }

    public void removeAnswer(int index) {
        if (answers != null && index >= 0 && index < answers.size()) {
            answers.remove(index);
            // Update correct answer indices after removal
            updateCorrectIndicesAfterRemoval(index);
        }
    }

    public void updateAnswer(int index, String newAnswer) {
        if (answers != null && index >= 0 && index < answers.size()) {
            answers.set(index, newAnswer != null ? newAnswer : "");
        }
    }

    public List<Integer> getCorrectAnswerIndices() {
        return correctAnswerIndices;
    }

    public void setCorrectAnswerIndices(List<Integer> correctAnswerIndices) {
        this.correctAnswerIndices = correctAnswerIndices != null ? correctAnswerIndices : new ArrayList<>();
    }

    public void addCorrectAnswerIndex(int index) {
        if (correctAnswerIndices == null) {
            correctAnswerIndices = new ArrayList<>();
        }
        if (!correctAnswerIndices.contains(index)) {
            correctAnswerIndices.add(index);
        }
    }

    public void removeCorrectAnswerIndex(int index) {
        if (correctAnswerIndices != null) {
            correctAnswerIndices.remove(Integer.valueOf(index));
        }
    }

    public void clearCorrectAnswerIndices() {
        if (correctAnswerIndices != null) {
            correctAnswerIndices.clear();
        }
    }

    public Quiz.Type getType() {
        return type;
    }

    public void setType(Quiz.Type type) {
        Quiz.Type oldType = this.type;
        this.type = type;

        // Handle type conversion logic
        if (oldType != type) {
            handleTypeConversion(oldType, type);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    /**
     * Handles the conversion between different question types
     */
    private void handleTypeConversion(Quiz.Type oldType, Quiz.Type newType) {
        if (correctAnswerIndices == null) {
            correctAnswerIndices = new ArrayList<>();
        }

        switch (newType) {
            case SINGLE_CHOICE:
                // For single choice, keep only the first correct answer
                if (correctAnswerIndices.size() > 1) {
                    int firstCorrect = correctAnswerIndices.get(0);
                    correctAnswerIndices.clear();
                    correctAnswerIndices.add(firstCorrect);
                }
                break;
            case MULTIPLE_CHOICE:
                // Multiple choice can keep all correct answers
                break;
            case FLASHCARD:
                // Flashcards don't need answer indices
                correctAnswerIndices.clear();
                break;
        }
    }

    /**
     * Updates correct answer indices after an answer is removed
     */
    private void updateCorrectIndicesAfterRemoval(int removedIndex) {
        if (correctAnswerIndices == null) return;

        List<Integer> updatedIndices = new ArrayList<>();
        for (Integer index : correctAnswerIndices) {
            if (index < removedIndex) {
                updatedIndices.add(index);
            } else if (index > removedIndex) {
                updatedIndices.add(index - 1);
            }
            // Skip the removed index
        }
        correctAnswerIndices = updatedIndices;
    }

    /**
     * Converts this editable quiz back to a GeneratedQuizItem for saving
     */
    public GeneratedQuizItem toGeneratedQuizItem() {
        GeneratedQuizItem item = new GeneratedQuizItem();
        item.question = this.question;
        item.answers = new ArrayList<>(this.answers);
        item.correctAnswerIndices = new ArrayList<>(this.correctAnswerIndices);
        item.type = this.type;
        return item;
    }

    /**
     * Validates the quiz data
     */
    public boolean isValid() {
        if (question == null || question.trim().isEmpty()) {
            return false;
        }

        if (type == Quiz.Type.FLASHCARD) {
            return true; // Flashcards only need a question
        }

        if (answers == null || answers.isEmpty()) {
            return false;
        }

        // Check if at least one answer is not empty
        boolean hasValidAnswer = false;
        for (String answer : answers) {
            if (answer != null && !answer.trim().isEmpty()) {
                hasValidAnswer = true;
                break;
            }
        }

        if (!hasValidAnswer) {
            return false;
        }

        // Check correct answers
        if (correctAnswerIndices == null || correctAnswerIndices.isEmpty()) {
            return false;
        }

        // Validate correct answer indices are within bounds
        for (Integer index : correctAnswerIndices) {
            if (index < 0 || index >= answers.size()) {
                return false;
            }
        }

        return true;
    }
}
