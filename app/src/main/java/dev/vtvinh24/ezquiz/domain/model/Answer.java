package dev.vtvinh24.ezquiz.domain.model;

import java.util.UUID;

/**
 * Model class representing an Answer to a Question.
 */
public class Answer {
    private String id;
    private String text;
    private boolean isCorrect;

    public Answer() {
        this.id = UUID.randomUUID().toString();
    }

    public Answer(String text, boolean isCorrect) {
        this();
        this.text = text;
        this.isCorrect = isCorrect;
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

    public boolean isCorrect() {
        return isCorrect;
    }

    public void setCorrect(boolean correct) {
        isCorrect = correct;
    }
}
