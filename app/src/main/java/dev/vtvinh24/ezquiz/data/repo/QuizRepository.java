package dev.vtvinh24.ezquiz.data.repo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.model.Quiz;

public class QuizRepository {
  private final AppDatabase db;

  public QuizRepository(AppDatabase db) {
    this.db = db;
  }

  public QuizCollectionEntity getCollectionOfQuiz(long quizId) {
    QuizEntity quiz = db.quizDao().getById(quizId);
    if (quiz == null) return null;
    QuizSetEntity set = db.quizSetDao().getById(quiz.quizSetId);
    if (set == null) return null;
    return db.quizCollectionDao().getById(set.collectionId);
  }

  public QuizSetEntity getSetOfQuiz(long quizId) {
    QuizEntity quiz = db.quizDao().getById(quizId);
    if (quiz == null) return null;
    return db.quizSetDao().getById(quiz.quizSetId);
  }

  public QuizEntity getQuiz(long quizId) {
    return db.quizDao().getById(quizId);
  }

  public List<QuizEntity> getQuizzesOfSet(long setId) {
    return db.quizDao().getByQuizSetId(setId);
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

  public List<QuizEntity> getFlashcards() {
    return db.quizDao().getFlashcards();
  }

  public List<QuizEntity> getFlashcardsOfSet(long setId) {
    return db.quizDao().getFlashcardsByQuizSetId(setId);
  }




}
