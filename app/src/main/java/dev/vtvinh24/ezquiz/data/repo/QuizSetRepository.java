package dev.vtvinh24.ezquiz.data.repo;

import java.util.ArrayList;
import java.util.List;

import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

public class QuizSetRepository {
  private final AppDatabase db;

  public QuizSetRepository(AppDatabase db) {
    this.db = db;
  }

  public QuizSetEntity getSet(long setId) {
    return db.quizSetDao().getById(setId);
  }

  public List<QuizEntity> getQuizzesOfSet(long setId) {
    return db.quizDao().getByQuizSetId(setId);
  }

  public QuizCollectionEntity getCollectionOfSet(long setId) {
    QuizSetEntity set = db.quizSetDao().getById(setId);
    if (set == null) return null;
    return db.quizCollectionDao().getById(set.collectionId);
  }

  public List<QuizSetEntity> getSetsOfCollection(long collectionId) {
    return db.quizSetDao().getByCollectionId(collectionId);
  }

  public List<QuizEntity> getAllQuizzes() {
    List<QuizSetEntity> sets = db.quizSetDao().getAll();
    List<QuizEntity> quizzes = new ArrayList<>();
    for (QuizSetEntity set : sets) {
      quizzes.addAll(db.quizDao().getByQuizSetId(set.id));
    }
    return quizzes;
  }
}
