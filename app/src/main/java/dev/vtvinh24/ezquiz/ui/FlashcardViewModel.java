package dev.vtvinh24.ezquiz.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.model.CardStatus;
import dev.vtvinh24.ezquiz.data.model.FlashcardResult;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.model.UserQuizProgress;
import dev.vtvinh24.ezquiz.data.repo.QuizRepository;
import dev.vtvinh24.ezquiz.data.repo.UserProgressRepository;
import dev.vtvinh24.ezquiz.util.SingleEvent;

public class FlashcardViewModel extends AndroidViewModel {

    public static class QuizDisplayItem {
        public final long id;
        public final Quiz quiz;

        QuizDisplayItem(long id, Quiz quiz) {
            this.id = id;
            this.quiz = quiz;
        }
    }

    private final QuizRepository quizRepository;
    private final UserProgressRepository userProgressRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<String> _sessionProgressText = new MutableLiveData<>();
    public final LiveData<String> sessionProgressText = _sessionProgressText;

    private final MutableLiveData<SingleEvent<List<FlashcardResult>>> _sessionFinished = new MutableLiveData<>();
    public final LiveData<SingleEvent<List<FlashcardResult>>> sessionFinished = _sessionFinished;

    private final MutableLiveData<List<QuizDisplayItem>> _flashcards = new MutableLiveData<>();
    public final LiveData<List<QuizDisplayItem>> flashcards = _flashcards;

    private final MutableLiveData<Integer> _currentCardPosition = new MutableLiveData<>();
    public final LiveData<Integer> currentCardPosition = _currentCardPosition;

    private final Map<Long, CardStatus> sessionProgress = new HashMap<>();

    public FlashcardViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabaseProvider.getDatabase(application);
        this.quizRepository = new QuizRepository(db);
        this.userProgressRepository = new UserProgressRepository(application);
    }

    /**
     * Bắt đầu một phiên học mới với tất cả các thẻ trong một bộ.
     * @param quizSetId ID của bộ câu hỏi.
     */
    public void startSession(long quizSetId) {
        Log.d("DEBUG_FLASHCARD", "ViewModel startSession called with quizSetId: " + quizSetId);
        executor.execute(() -> {
            List<QuizEntity> flashcardEntities = quizRepository.getFlashcardsOfSet(quizSetId);
            processAndDisplayCards(flashcardEntities);
        });
    }

    /**
     * Bắt đầu một phiên học đặc biệt chỉ với danh sách ID thẻ được chỉ định.
     * @param cardIds Danh sách ID của các thẻ cần học.
     */
    public void startSessionWithSpecificCards(List<Long> cardIds) {
        Log.d("DEBUG_FLASHCARD", "ViewModel startSessionWithSpecificCards called with " + cardIds.size() + " cards.");
        if (cardIds == null || cardIds.isEmpty()) {
            _sessionFinished.postValue(new SingleEvent<>(new ArrayList<>()));
            return;
        }
        executor.execute(() -> {
            List<QuizEntity> flashcardEntities = quizRepository.getQuizzesByIds(cardIds);
            processAndDisplayCards(flashcardEntities);
        });
    }

    /**
     * Xử lý danh sách các QuizEntity và cập nhật LiveData để hiển thị trên UI.
     * @param flashcardEntities Danh sách các thẻ từ database.
     */
    private void processAndDisplayCards(List<QuizEntity> flashcardEntities) {
        if (flashcardEntities == null || flashcardEntities.isEmpty()) {
            mainThreadHandler.post(() -> {
                _flashcards.setValue(Collections.emptyList());
                _sessionProgressText.setValue("0 / 0");
            });
            return;
        }

        List<QuizDisplayItem> displayItems = flashcardEntities.stream().map(entity -> {
            Quiz model = new Quiz(
                    entity.question, entity.answers, entity.correctAnswerIndices,
                    entity.type, entity.createdAt, entity.updatedAt,
                    entity.archived, entity.difficulty);
            return new QuizDisplayItem(entity.id, model);
        }).collect(Collectors.toList());

        Collections.shuffle(displayItems);

        mainThreadHandler.post(() -> {
            _flashcards.setValue(displayItems);
            _currentCardPosition.setValue(0);
            sessionProgress.clear();
            updateProgressText();
        });
    }


    public void markAsKnown() {
        updateCardStatus(CardStatus.KNOWN);
        moveToNextCard();
    }

    public void markAsUnknown() {
        updateCardStatus(CardStatus.UNKNOWN);
        moveToNextCard();
    }

    public void onUserSwiped(int position) {
        if (_currentCardPosition.getValue() != null && _currentCardPosition.getValue() != position) {
            _currentCardPosition.setValue(position);
            updateProgressText();
        }
    }

    public void jumpToPosition(int position) {
        List<QuizDisplayItem> cards = _flashcards.getValue();
        if (cards != null && position >= 0 && position < cards.size()) {
            _currentCardPosition.setValue(position);
            updateProgressText();
        }
    }

    private void updateCardStatus(CardStatus status) {
        Integer position = _currentCardPosition.getValue();
        List<QuizDisplayItem> cards = _flashcards.getValue();
        if (position != null && cards != null && position < cards.size()) {
            long currentQuizId = cards.get(position).id;
            sessionProgress.put(currentQuizId, status);
        }
    }

    private void moveToNextCard() {
        Integer position = _currentCardPosition.getValue();
        List<QuizDisplayItem> cards = _flashcards.getValue();
        if (position != null && cards != null) {
            if (position < cards.size() - 1) {
                _currentCardPosition.setValue(position + 1);
                updateProgressText();
            } else {
                finishSession();
            }
        }
    }

    private void updateProgressText() {
        Integer position = _currentCardPosition.getValue();
        List<QuizDisplayItem> cards = _flashcards.getValue();
        if (position != null && cards != null && !cards.isEmpty()) {
            _sessionProgressText.setValue((position + 1) + " / " + cards.size());
        } else {
            _sessionProgressText.setValue("0 / 0");
        }
    }

    private void finishSession() {
        List<QuizDisplayItem> cards = _flashcards.getValue();
        if (cards == null) {
            Log.w("FlashcardViewModel", "finishSession called but flashcard list is null.");
            _sessionFinished.postValue(new SingleEvent<>(new ArrayList<>()));
            return;
        }

        List<FlashcardResult> results = new ArrayList<>();
        for (QuizDisplayItem cardItem : cards) {
            long cardId = cardItem.id;
            String question = cardItem.quiz.getQuestion();
            boolean wasKnown = sessionProgress.getOrDefault(cardId, CardStatus.UNKNOWN) == CardStatus.KNOWN;
            results.add(new FlashcardResult(cardId, question, wasKnown));
        }

        saveProgressToDatabase(results);
        _sessionFinished.postValue(new SingleEvent<>(results));
    }

    private void saveProgressToDatabase(List<FlashcardResult> results) {
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                for (FlashcardResult result : results) {
                    UserQuizProgress progress = userProgressRepository.getQuizProgress(result.getQuizId());
                    if (progress == null) {
                        progress = new UserQuizProgress();
                    }
                    progress.lastAttemptedAt = now;
                    progress.attemptCount++;
                    if (result.wasKnown()) {
                        progress.correctAttemptCount++;
                    }
                    progress.successRate = (progress.attemptCount == 0) ? 0 : (float) progress.correctAttemptCount / progress.attemptCount;
                    progress.type = Quiz.Type.FLASHCARD;
                    userProgressRepository.setQuizProgress(result.getQuizId(), progress);
                }
            } catch (Exception e) {
                Log.e("FlashcardViewModel", "Error saving flashcard progress: " + e.getMessage(), e);
            }
        });
    }
}