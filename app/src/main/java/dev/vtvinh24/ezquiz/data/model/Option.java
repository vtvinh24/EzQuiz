package dev.vtvinh24.ezquiz.data.model;

/**
 * Represents an option for choice-based quizzes.
 */
public class Option {
    private String id;
    private String text;
    private boolean isCorrect;

    public Option() {
    }

    public Option(String text, boolean isCorrect) {
        this.text = text;
        this.isCorrect = isCorrect;
    }

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
