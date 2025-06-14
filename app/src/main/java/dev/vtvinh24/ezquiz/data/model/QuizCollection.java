package dev.vtvinh24.ezquiz.data.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a collection of quizzes that are grouped together.
 * For example, a collection imported from Quizlet or created by the user.
 */
public class QuizCollection {
    private String id;
    private String title;
    private String description;
    private List<Quiz> quizzes;
    private String sourceUrl; // Optional URL if imported from external source
    private long createdAt;
    private long lastModifiedAt;

    public QuizCollection() {
        this.id = UUID.randomUUID().toString();
        this.quizzes = new ArrayList<>();
        this.createdAt = System.currentTimeMillis();
        this.lastModifiedAt = this.createdAt;
    }

    public QuizCollection(String title, String description) {
        this();
        this.title = title;
        this.description = description;
    }

    // Getters and setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        updateLastModified();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        updateLastModified();
    }

    public List<Quiz> getQuizzes() {
        return quizzes;
    }

    public void setQuizzes(List<Quiz> quizzes) {
        this.quizzes = quizzes;
        updateLastModified();
    }

    public void addQuiz(Quiz quiz) {
        this.quizzes.add(quiz);
        updateLastModified();
    }

    public void removeQuiz(Quiz quiz) {
        this.quizzes.remove(quiz);
        updateLastModified();
    }

    public void removeQuiz(String quizId) {
        this.quizzes.removeIf(q -> q.getId().equals(quizId));
        updateLastModified();
    }

    public String getSourceUrl() {
        return sourceUrl;
    }

    public void setSourceUrl(String sourceUrl) {
        this.sourceUrl = sourceUrl;
        updateLastModified();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(long lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    private void updateLastModified() {
        this.lastModifiedAt = System.currentTimeMillis();
    }

    public int getQuizCount() {
        return this.quizzes.size();
    }

    /**
     * Creates a clone of this collection without the quizzes
     *
     * @return A new QuizCollection with the same metadata but no quizzes
     */
    public QuizCollection cloneWithoutQuizzes() {
        QuizCollection clone = new QuizCollection();
        clone.setId(this.id);
        clone.setTitle(this.title);
        clone.setDescription(this.description);
        clone.setSourceUrl(this.sourceUrl);
        clone.setCreatedAt(this.createdAt);
        clone.setLastModifiedAt(this.lastModifiedAt);
        return clone;
    }
}
