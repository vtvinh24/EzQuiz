package dev.vtvinh24.ezquiz.data.repo;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import dev.vtvinh24.ezquiz.data.dao.PracticeProgressDao;
import dev.vtvinh24.ezquiz.data.dao.QuizCollectionDao;
import dev.vtvinh24.ezquiz.data.dao.QuizDao;
import dev.vtvinh24.ezquiz.data.dao.QuizSetDao;
import dev.vtvinh24.ezquiz.data.dao.QuizSessionHistoryDao;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.PracticeProgressEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSessionHistoryEntity;
import dev.vtvinh24.ezquiz.data.model.HistoryItem;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryRepository {
    private final AppDatabase db;
    private final ExecutorService executor;

    public HistoryRepository(Context context) {
        this.db = AppDatabaseProvider.getDatabase(context);
        this.executor = Executors.newFixedThreadPool(4);
    }

    public LiveData<List<HistoryItem>> getAllHistoryItems() {
        MutableLiveData<List<HistoryItem>> liveData = new MutableLiveData<>();

        executor.execute(() -> {
            List<HistoryItem> historyItems = new ArrayList<>();

            List<PracticeProgressEntity> allProgress = db.practiceProgressDao().getAllPracticeProgress();
            List<QuizSetEntity> allQuizSets = db.quizSetDao().getAll();
            List<QuizSessionHistoryEntity> allSessionHistory = db.quizSessionHistoryDao().getAllSessions();

            for (QuizSetEntity quizSet : allQuizSets) {
                PracticeProgressEntity progress = findProgressForSet(allProgress, quizSet.id);
                String collectionName = getCollectionName(quizSet.collectionId);
                int totalQuestions = getQuizCountBySetId(quizSet.id);
                List<QuizSessionHistoryEntity> sessionHistory = getSessionHistoryForSet(allSessionHistory, quizSet.id);
                boolean isAIGenerated = isAIGeneratedQuizSet(quizSet);

                // Hiển thị tất cả quiz sets có progress hoặc session history hoặc có questions
                if (progress != null || !sessionHistory.isEmpty() || totalQuestions > 0) {
                    historyItems.add(new HistoryItem(quizSet, progress, collectionName, totalQuestions, sessionHistory, isAIGenerated));
                }
            }

            historyItems.sort((a, b) -> Long.compare(b.getLastUpdated(), a.getLastUpdated()));
            liveData.postValue(historyItems);
        });

        return liveData;
    }

    public LiveData<List<HistoryItem>> getInProgressItems() {
        MutableLiveData<List<HistoryItem>> liveData = new MutableLiveData<>();

        executor.execute(() -> {
            List<HistoryItem> historyItems = new ArrayList<>();

            // Lấy cả practice progress và incomplete sessions
            List<PracticeProgressEntity> incompleteProgress = db.practiceProgressDao().getIncompletePracticeProgress();
            List<QuizSessionHistoryEntity> incompleteSessions = db.quizSessionHistoryDao().getIncompleteSessions();

            // Từ practice progress
            for (PracticeProgressEntity progress : incompleteProgress) {
                QuizSetEntity quizSet = db.quizSetDao().getById(progress.quizSetId);
                if (quizSet != null) {
                    String collectionName = getCollectionName(quizSet.collectionId);
                    int totalQuestions = getQuizCountBySetId(quizSet.id);
                    List<QuizSessionHistoryEntity> sessionHistory = db.quizSessionHistoryDao().getSessionsByQuizSetId(quizSet.id);
                    boolean isAIGenerated = isAIGeneratedQuizSet(quizSet);
                    historyItems.add(new HistoryItem(quizSet, progress, collectionName, totalQuestions, sessionHistory, isAIGenerated));
                }
            }

            // Từ incomplete sessions
            for (QuizSessionHistoryEntity session : incompleteSessions) {
                QuizSetEntity quizSet = db.quizSetDao().getById(session.quizSetId);
                if (quizSet != null && !containsQuizSet(historyItems, quizSet.id)) {
                    String collectionName = getCollectionName(quizSet.collectionId);
                    int totalQuestions = getQuizCountBySetId(quizSet.id);
                    List<QuizSessionHistoryEntity> sessionHistory = db.quizSessionHistoryDao().getSessionsByQuizSetId(quizSet.id);
                    PracticeProgressEntity progress = db.practiceProgressDao().getPracticeProgress(quizSet.id);
                    boolean isAIGenerated = isAIGeneratedQuizSet(quizSet);
                    historyItems.add(new HistoryItem(quizSet, progress, collectionName, totalQuestions, sessionHistory, isAIGenerated));
                }
            }

            liveData.postValue(historyItems);
        });

        return liveData;
    }

    public LiveData<List<HistoryItem>> getCompletedItems() {
        MutableLiveData<List<HistoryItem>> liveData = new MutableLiveData<>();

        executor.execute(() -> {
            List<HistoryItem> historyItems = new ArrayList<>();

            // Lấy cả completed practice progress và completed sessions
            List<PracticeProgressEntity> completedProgress = db.practiceProgressDao().getCompletedPracticeProgress();
            List<QuizSessionHistoryEntity> completedSessions = db.quizSessionHistoryDao().getCompletedSessions();

            // Từ practice progress
            for (PracticeProgressEntity progress : completedProgress) {
                QuizSetEntity quizSet = db.quizSetDao().getById(progress.quizSetId);
                if (quizSet != null) {
                    String collectionName = getCollectionName(quizSet.collectionId);
                    int totalQuestions = getQuizCountBySetId(quizSet.id);
                    List<QuizSessionHistoryEntity> sessionHistory = db.quizSessionHistoryDao().getSessionsByQuizSetId(quizSet.id);
                    boolean isAIGenerated = isAIGeneratedQuizSet(quizSet);
                    historyItems.add(new HistoryItem(quizSet, progress, collectionName, totalQuestions, sessionHistory, isAIGenerated));
                }
            }

            // Từ completed sessions
            for (QuizSessionHistoryEntity session : completedSessions) {
                QuizSetEntity quizSet = db.quizSetDao().getById(session.quizSetId);
                if (quizSet != null && !containsQuizSet(historyItems, quizSet.id)) {
                    String collectionName = getCollectionName(quizSet.collectionId);
                    int totalQuestions = getQuizCountBySetId(quizSet.id);
                    List<QuizSessionHistoryEntity> sessionHistory = db.quizSessionHistoryDao().getSessionsByQuizSetId(quizSet.id);
                    PracticeProgressEntity progress = db.practiceProgressDao().getPracticeProgress(quizSet.id);
                    boolean isAIGenerated = isAIGeneratedQuizSet(quizSet);
                    historyItems.add(new HistoryItem(quizSet, progress, collectionName, totalQuestions, sessionHistory, isAIGenerated));
                }
            }

            liveData.postValue(historyItems);
        });

        return liveData;
    }

    public void saveSessionHistory(long quizSetId, String sessionType, int totalQuestions,
                                  int correctAnswers, int incorrectAnswers, int skippedAnswers,
                                  double scorePercentage, long timeSpent, String detailedResults,
                                  boolean isCompleted, long startTime, long endTime) {
        executor.execute(() -> {
            QuizSessionHistoryEntity session = new QuizSessionHistoryEntity(
                quizSetId, sessionType, totalQuestions, correctAnswers, incorrectAnswers,
                skippedAnswers, scorePercentage, timeSpent, detailedResults, isCompleted,
                startTime, endTime, System.currentTimeMillis()
            );
            db.quizSessionHistoryDao().insertSession(session);
        });
    }

    private boolean containsQuizSet(List<HistoryItem> items, long quizSetId) {
        for (HistoryItem item : items) {
            if (item.getQuizSet().id == quizSetId) {
                return true;
            }
        }
        return false;
    }

    private PracticeProgressEntity findProgressForSet(List<PracticeProgressEntity> allProgress, long setId) {
        for (PracticeProgressEntity progress : allProgress) {
            if (progress.quizSetId == setId) {
                return progress;
            }
        }
        return null;
    }

    private List<QuizSessionHistoryEntity> getSessionHistoryForSet(List<QuizSessionHistoryEntity> allSessions, long setId) {
        List<QuizSessionHistoryEntity> sessionHistory = new ArrayList<>();
        for (QuizSessionHistoryEntity session : allSessions) {
            if (session.quizSetId == setId) {
                sessionHistory.add(session);
            }
        }
        return sessionHistory;
    }

    private String getCollectionName(long collectionId) {
        QuizCollectionEntity collection = db.quizCollectionDao().getById(collectionId);
        return collection != null ? collection.name : "Unknown Collection";
    }

    private int getQuizCountBySetId(long setId) {
        List<QuizEntity> quizzes = db.quizDao().getByQuizSetId(setId);
        return quizzes != null ? quizzes.size() : 0;
    }

    private boolean isAIGeneratedQuizSet(QuizSetEntity quizSet) {
        // Kiểm tra xem quiz set có được tạo bằng AI hay không
        // Có thể dựa vào tên collection, description, hoặc metadata
        if (quizSet.description != null && quizSet.description.contains("AI Generated")) {
            return true;
        }

        // Kiểm tra tên collection
        String collectionName = getCollectionName(quizSet.collectionId);
        if (collectionName != null && (collectionName.contains("AI") || collectionName.contains("Generated"))) {
            return true;
        }

        return false;
    }
}
