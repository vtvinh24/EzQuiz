package dev.vtvinh24.ezquiz.data.repo;

import java.util.ArrayList;
import java.util.List;

import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

public class QuizCollectionRepository {
  private final AppDatabase db;

  public QuizCollectionRepository(AppDatabase db) {
    this.db = db;
  }

  public QuizCollectionEntity getCollection(long collectionId) {
    return db.quizCollectionDao().getById(collectionId);
  }

  public List<QuizSetEntity> getSetsOfCollection(long collectionId) {
    return db.quizSetDao().getByCollectionId(collectionId);
  }

  public List<QuizEntity> getQuizzesOfCollection(long collectionId) {
    List<QuizSetEntity> sets = db.quizSetDao().getByCollectionId(collectionId);
    List<QuizEntity> quizzes = new ArrayList<>();
    for (QuizSetEntity set : sets) {
      quizzes.addAll(db.quizDao().getByQuizSetId(set.id));
    }
    return quizzes;
  }

  public List<QuizCollectionEntity> getAllCollections() {
    return db.quizCollectionDao().getAll();
  }
}
