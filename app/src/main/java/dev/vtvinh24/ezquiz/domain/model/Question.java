package dev.vtvinh24.ezquiz.domain.model;

import java.util.List;
import java.util.UUID;

/**
 * Model class representing a Question in a Quiz.
 */
public class Question {
    private String id;
    private String text;
    private List<Answer> answers;
    private QuestionType type;
    private int points;

    public Question() {
        this.id = UUID.randomUUID().toString();
    }

    public Question(String text, List<Answer> answers, QuestionType type, int points) {
        this();
        this.text = text;
        this.answers = answers;
        this.type = type;
        this.points = points;
    }

    public enum QuestionType {
        SINGLE_CHOICE,
        MULTIPLE_CHOICE,
        TRUE_FALSE,
        TEXT_INPUT
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }
}
