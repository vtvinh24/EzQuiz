  package dev.vtvinh24.ezquiz.data.dao;

  import androidx.room.Dao;
  import androidx.room.Delete;
  import androidx.room.Insert;
  import androidx.room.Query;
  import androidx.room.Update;

  import java.util.List;

  import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

  @Dao
  public interface QuizSetDao {
    @Insert
    long insert(QuizSetEntity quizSet);

    @Update
    int update(QuizSetEntity quizSet);

    @Delete
    int delete(QuizSetEntity quizSet);

    @Query("SELECT * FROM quiz_set WHERE id = :id")
    QuizSetEntity getById(long id);

    @Query("SELECT * FROM quiz_set")
    List<QuizSetEntity> getAll();

    @Query("SELECT * FROM quiz_set WHERE collectionId = :collectionId")
    List<QuizSetEntity> getByCollectionId(long collectionId);

    @Query("SELECT * FROM quiz_set WHERE archived = 1")
    List<QuizSetEntity> getArchived();
  }
