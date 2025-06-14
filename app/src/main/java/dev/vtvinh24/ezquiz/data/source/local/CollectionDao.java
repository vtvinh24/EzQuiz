package dev.vtvinh24.ezquiz.data.source.local;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import java.util.List;

import dev.vtvinh24.ezquiz.data.model.QuizCollection;

/**
 * Data Access Object for QuizCollection entity.
 * Provides methods to interact with the collections table in the database.
 */
@Dao
public interface CollectionDao {

    @Query("SELECT * FROM collections")
    List<QuizCollection> getAllCollections();

    @Query("SELECT * FROM collections WHERE id = :collectionId")
    QuizCollection getCollectionById(String collectionId);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertCollection(QuizCollection collection);

    @Update
    void updateCollection(QuizCollection collection);

    @Delete
    void deleteCollection(QuizCollection collection);

    @Query("DELETE FROM collections WHERE id = :collectionId")
    void deleteCollectionById(String collectionId);

    @Query("SELECT * FROM collections WHERE title LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    List<QuizCollection> searchCollections(String query);

    @Query("SELECT COUNT(*) FROM collections")
    int getCollectionCount();

    @Transaction
    @Query("SELECT * FROM collections ORDER BY lastModifiedAt DESC LIMIT :limit")
    List<QuizCollection> getRecentCollections(int limit);
}
