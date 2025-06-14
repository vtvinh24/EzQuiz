package dev.vtvinh24.ezquiz.data.repository;

import java.util.List;

import dev.vtvinh24.ezquiz.data.model.QuizCollection;

/**
 * Repository interface for Quiz Collection operations.
 * Follows the repository pattern to abstract data sources.
 */
public interface QuizCollectionRepository {

    /**
     * Get all available quiz collections.
     *
     * @return List of all quiz collections
     */
    List<QuizCollection> getAllCollections();

    /**
     * Get a specific quiz collection by its ID.
     *
     * @param collectionId The ID of the collection
     * @return The quiz collection or null if not found
     */
    QuizCollection getCollectionById(String collectionId);

    /**
     * Save a new quiz collection or update an existing one.
     *
     * @param collection The collection to save
     * @return The saved collection with any auto-generated data (like IDs)
     */
    QuizCollection saveCollection(QuizCollection collection);

    /**
     * Delete a quiz collection.
     *
     * @param collectionId The ID of the collection to delete
     * @return true if deletion was successful, false otherwise
     */
    boolean deleteCollection(String collectionId);

    /**
     * Search for collections matching a query string.
     *
     * @param query The search query
     * @return List of matching collections
     */
    List<QuizCollection> searchCollections(String query);

    /**
     * Count the total number of collections available.
     *
     * @return The total count of collections
     */
    int getCollectionCount();
}
