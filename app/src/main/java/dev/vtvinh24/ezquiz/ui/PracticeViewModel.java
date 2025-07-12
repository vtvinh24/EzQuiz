package dev.vtvinh24.ezquiz.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.PracticeProgressEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.repo.QuizRepository;
import dev.vtvinh24.ezquiz.util.QuizAttemptResult;
import dev.vtvinh24.ezquiz.util.SingleEvent;

public class PracticeViewModel extends AndroidViewModel {

    public static class PracticeItem {
        public final long id;
        public final Quiz quiz;
        public List<Integer> userAnswerIndices = new ArrayList<>();

        PracticeItem(long id, Quiz quiz) { this.id = id; this.quiz = quiz; }
    }

    private final QuizRepository quizRepository;
    private final AppDatabase database;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Gson gson = new Gson();

    private final MutableLiveData<List<PracticeItem>> _quizItems = new MutableLiveData<>();
    public final LiveData<List<PracticeItem>> quizItems = _quizItems;

    private final MutableLiveData<Integer> _currentQuestionPosition = new MutableLiveData<>(0);
    public final LiveData<Integer> currentQuestionPosition = _currentQuestionPosition;

    private final MutableLiveData<Boolean> _isAnswerChecked = new MutableLiveData<>(false);
    public final LiveData<Boolean> isAnswerChecked = _isAnswerChecked;

    private final MutableLiveData<Boolean> _canCheckAnswer = new MutableLiveData<>(false);
    public final LiveData<Boolean> canCheckAnswer = _canCheckAnswer;

    private final MutableLiveData<SingleEvent<List<QuizAttemptResult>>> _sessionFinished = new MutableLiveData<>();
    public final LiveData<SingleEvent<List<QuizAttemptResult>>> sessionFinished = _sessionFinished;

    private final MutableLiveData<Boolean> _hasExistingProgress = new MutableLiveData<>();
    public final LiveData<Boolean> hasExistingProgress = _hasExistingProgress;

    private final List<QuizAttemptResult> sessionResults = new ArrayList<>();
    private int currentPosition = 0;
    private long currentQuizSetId = -1;

    public PracticeViewModel(@NonNull Application application) {
        super(application);
        this.database = AppDatabaseProvider.getDatabase(application);
        this.quizRepository = new QuizRepository(database);
    }

    public void checkForExistingProgress(long quizSetId) {
        this.currentQuizSetId = quizSetId;
        executor.execute(() -> {
            boolean hasProgress = database.practiceProgressDao().hasPracticeProgress(quizSetId);
            _hasExistingProgress.postValue(hasProgress);
        });
    }

    public void startNewSession(long quizSetId) {
        this.currentQuizSetId = quizSetId;
        executor.execute(() -> {
            database.practiceProgressDao().deletePracticeProgressBySetId(quizSetId);
            loadQuizItems(quizSetId, false);
        });
    }

    public void resumeSession(long quizSetId) {
        this.currentQuizSetId = quizSetId;
        executor.execute(() -> {
            loadQuizItems(quizSetId, true);
        });
    }

    public void startSession(long quizSetId) {
        checkForExistingProgress(quizSetId);
    }

    private void loadQuizItems(long quizSetId, boolean isResume) {
        List<QuizEntity> quizEntities = quizRepository.getQuizzesOfSet(quizSetId);

        if (isResume) {
            PracticeProgressEntity progress = database.practiceProgressDao().getPracticeProgress(quizSetId);
            if (progress != null) {
                List<Long> quizIds = new ArrayList<>();
                Map<Long, List<Integer>> userAnswers = new HashMap<>();
                List<QuizAttemptResult> results = new ArrayList<>();

                try {
                    quizIds = gson.fromJson(progress.quizIds, new TypeToken<List<Long>>(){}.getType());
                    userAnswers = gson.fromJson(progress.userAnswers, new TypeToken<Map<Long, List<Integer>>>(){}.getType());
                    results = gson.fromJson(progress.sessionResults, new TypeToken<List<QuizAttemptResult>>(){}.getType());
                } catch (Exception e) {
                    loadQuizItems(quizSetId, false);
                    return;
                }

                if (quizIds == null) quizIds = new ArrayList<>();
                if (userAnswers == null) userAnswers = new HashMap<>();
                if (results == null) results = new ArrayList<>();

                List<QuizEntity> orderedQuizzes = new ArrayList<>();
                for (Long id : quizIds) {
                    for (QuizEntity entity : quizEntities) {
                        if (entity.id == id) {
                            orderedQuizzes.add(entity);
                            break;
                        }
                    }
                }

                if (orderedQuizzes.isEmpty()) {
                    orderedQuizzes = quizEntities;
                }

                // Make userAnswers effectively final for lambda usage
                final Map<Long, List<Integer>> finalUserAnswers = userAnswers;

                List<PracticeItem> practiceItems = orderedQuizzes.stream().map(entity -> {
                    Quiz model = new Quiz(entity.question, entity.answers, entity.correctAnswerIndices,
                            entity.type, entity.createdAt, entity.updatedAt,
                            entity.archived, entity.difficulty);
                    PracticeItem item = new PracticeItem(entity.id, model);
                    if (finalUserAnswers.containsKey(entity.id)) {
                        item.userAnswerIndices = finalUserAnswers.get(entity.id);
                    }
                    return item;
                }).collect(Collectors.toList());

                currentPosition = progress.currentPosition;
                sessionResults.clear();
                sessionResults.addAll(results);

                _quizItems.postValue(practiceItems);
                _currentQuestionPosition.postValue(currentPosition);
                return;
            }
        }

        Collections.shuffle(quizEntities);
        List<PracticeItem> practiceItems = quizEntities.stream().map(entity -> {
            Quiz model = new Quiz(entity.question, entity.answers, entity.correctAnswerIndices,
                    entity.type, entity.createdAt, entity.updatedAt,
                    entity.archived, entity.difficulty);
            return new PracticeItem(entity.id, model);
        }).collect(Collectors.toList());

        currentPosition = 0;
        sessionResults.clear();
        _quizItems.postValue(practiceItems);
        _currentQuestionPosition.postValue(currentPosition);
    }

    public void saveProgress() {
        if (currentQuizSetId == -1) return;

        executor.execute(() -> {
            List<PracticeItem> items = _quizItems.getValue();
            if (items == null || items.isEmpty()) return;

            List<Long> quizIds = items.stream().map(item -> item.id).collect(Collectors.toList());
            Map<Long, List<Integer>> userAnswers = new HashMap<>();
            for (PracticeItem item : items) {
                if (!item.userAnswerIndices.isEmpty()) {
                    userAnswers.put(item.id, item.userAnswerIndices);
                }
            }

            long currentTime = System.currentTimeMillis();
            PracticeProgressEntity progress = new PracticeProgressEntity(
                currentQuizSetId,
                currentPosition,
                gson.toJson(quizIds),
                gson.toJson(userAnswers),
                gson.toJson(sessionResults),
                currentTime,
                currentTime
            );

            database.practiceProgressDao().insertPracticeProgress(progress);
        });
    }

    public void clearProgress() {
        if (currentQuizSetId == -1) return;

        executor.execute(() -> {
            database.practiceProgressDao().deletePracticeProgressBySetId(currentQuizSetId);
        });
    }

    public void onAnswerSelected(long quizId, List<Integer> selectedIndices) {
        List<PracticeItem> items = _quizItems.getValue();
        if (items == null) return;
        for (PracticeItem item : items) {
            if (item.id == quizId) {
                item.userAnswerIndices = new ArrayList<>(selectedIndices);
                break;
            }
        }
        _canCheckAnswer.postValue(!selectedIndices.isEmpty());
        saveProgress();
    }

    public void checkCurrentAnswer() {
        List<PracticeItem> items = _quizItems.getValue();
        if (items == null || currentPosition >= items.size()) return;

        PracticeItem currentItem = items.get(currentPosition);
        List<Integer> userAnswers = new ArrayList<>(currentItem.userAnswerIndices);
        List<Integer> correctAnswers = new ArrayList<>(currentItem.quiz.getCorrectAnswerIndices());

        Collections.sort(userAnswers);
        Collections.sort(correctAnswers);

        boolean isCorrect = userAnswers.equals(correctAnswers);
        sessionResults.add(new QuizAttemptResult(currentItem.id, isCorrect));

        _isAnswerChecked.postValue(true);
        saveProgress();
    }

    public void moveToNextQuestion() {
        List<PracticeItem> items = _quizItems.getValue();
        if (items != null && currentPosition < items.size() - 1) {
            currentPosition++;
            _currentQuestionPosition.setValue(currentPosition);
            _isAnswerChecked.setValue(false);
            _canCheckAnswer.setValue(false);
            saveProgress();
        } else {
            clearProgress();
            _sessionFinished.setValue(new SingleEvent<>(new ArrayList<>(sessionResults)));
        }
    }

    public int getCurrentPosition() { return currentPosition; }
}