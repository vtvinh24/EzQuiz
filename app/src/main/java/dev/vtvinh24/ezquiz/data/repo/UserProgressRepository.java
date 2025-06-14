package dev.vtvinh24.ezquiz.data.repo;

import android.content.Context;

import dev.vtvinh24.ezquiz.data.dao.UserQuizCollectionProgressDao;
import dev.vtvinh24.ezquiz.data.dao.UserQuizProgressDao;
import dev.vtvinh24.ezquiz.data.dao.UserQuizSetProgressDao;
import dev.vtvinh24.ezquiz.data.model.UserQuizCollectionProgress;
import dev.vtvinh24.ezquiz.data.model.UserQuizProgress;
import dev.vtvinh24.ezquiz.data.model.UserQuizSetProgress;

public class UserProgressRepository {
  private final UserQuizProgressDao quizDao;
  private final UserQuizSetProgressDao setDao;
  private final UserQuizCollectionProgressDao collectionDao;

  public UserProgressRepository(Context context) {
    this.quizDao = new UserQuizProgressDao(context);
    this.setDao = new UserQuizSetProgressDao(context);
    this.collectionDao = new UserQuizCollectionProgressDao(context);
  }

  public UserQuizProgress getQuizProgress(long quizId) {
    return quizDao.getProgress(quizId);
  }

  public void setQuizProgress(long quizId, UserQuizProgress progress) {
    quizDao.setProgress(quizId, progress);
  }

  public void deleteQuizProgress(long quizId) {
    quizDao.deleteProgress(quizId);
  }

  public UserQuizSetProgress getQuizSetProgress(long setId) {
    return setDao.getProgress(setId);
  }

  public void setQuizSetProgress(long setId, UserQuizSetProgress progress) {
    setDao.setProgress(setId, progress);
  }

  public void deleteQuizSetProgress(long setId) {
    setDao.deleteProgress(setId);
  }
  
  public UserQuizCollectionProgress getQuizCollectionProgress(long collectionId) {
    return collectionDao.getProgress(collectionId);
  }

  public void setQuizCollectionProgress(long collectionId, UserQuizCollectionProgress progress) {
    collectionDao.setProgress(collectionId, progress);
  }

  public void deleteQuizCollectionProgress(long collectionId) {
    collectionDao.deleteProgress(collectionId);
  }
}

