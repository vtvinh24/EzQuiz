package dev.vtvinh24.ezquiz.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
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
import dev.vtvinh24.ezquiz.util.SingleEvent;

// ViewModel đã được tinh chỉnh để hoạt động mà không cần Quiz model có ID
public class FlashcardViewModel extends AndroidViewModel {

    // Lớp Wrapper nội bộ để kết hợp ID và Model
    // Lớp UI (Adapter) sẽ làm việc với đối tượng này
    public static class QuizDisplayItem {
        public final long id;
        public final Quiz quiz;

        QuizDisplayItem(long id, Quiz quiz) {
            this.id = id;
            this.quiz = quiz;
        }
    }

    // --- Các thành phần không đổi ---
    private final AppDatabase db;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<String> _sessionProgressText = new MutableLiveData<>();
    public final LiveData<String> sessionProgressText = _sessionProgressText;

    private final MutableLiveData<SingleEvent<List<FlashcardResult>>> _sessionFinished = new MutableLiveData<>();
    public final LiveData<SingleEvent<List<FlashcardResult>>> sessionFinished = _sessionFinished;

    // --- Các thành phần đã THAY ĐỔI ---

    // 1. LiveData bây giờ chứa danh sách các QuizDisplayItem
    private final MutableLiveData<List<QuizDisplayItem>> _flashcards = new MutableLiveData<>();
    public final LiveData<List<QuizDisplayItem>> flashcards = _flashcards;

    // 2. Vị trí thẻ không đổi, vẫn là Integer
    private final MutableLiveData<Integer> _currentCardPosition = new MutableLiveData<>();
    public final LiveData<Integer> currentCardPosition = _currentCardPosition;

    // 3. Map để theo dõi tiến trình không đổi, vì nó đã dùng ID (Long)
    private Map<Long, CardStatus> sessionProgress = new HashMap<>();

    public FlashcardViewModel(@NonNull Application application) {
        super(application);
        this.db = AppDatabaseProvider.getDatabase(application);
    }

    public void startSession(long quizSetId) {
        executor.execute(() -> {
            List<QuizEntity> entities = db.quizDao().getFlashcardsByQuizSetId(quizSetId);

            // Chuyển từ List<QuizEntity> sang List<QuizDisplayItem>
            List<QuizDisplayItem> displayItems = entities.stream().map(entity -> {
                // Tạo Model (không có ID)
                Quiz model = new Quiz(
                        entity.question,
                        entity.answers,
                        entity.correctAnswerIndices,
                        entity.type,
                        entity.createdAt,
                        entity.updatedAt,
                        entity.archived,
                        entity.difficulty
                );
                // Tạo đối tượng hiển thị bao gồm cả ID và Model
                return new QuizDisplayItem(entity.id, model);
            }).collect(Collectors.toList());

            Collections.shuffle(displayItems);

            mainThreadHandler.post(() -> {
                _flashcards.setValue(displayItems); // Cập nhật LiveData với danh sách mới
                _currentCardPosition.setValue(0);
                sessionProgress.clear();
                updateProgressText();
            });
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

    private void updateCardStatus(CardStatus status) {
        Integer position = _currentCardPosition.getValue();
        List<QuizDisplayItem> cards = _flashcards.getValue();
        if (position != null && cards != null && position < cards.size()) {
            // Lấy ID từ QuizDisplayItem, không phải từ Quiz model
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
        if (position != null && cards != null) {
            _sessionProgressText.setValue((position + 1) + " / " + cards.size());
        }
    }

    private void finishSession() {
        List<QuizDisplayItem> cards = _flashcards.getValue();
        if (cards == null) return;

        List<FlashcardResult> results = new ArrayList<>();
        for (QuizDisplayItem cardItem : cards) {
            // Lấy ID và question từ cardItem
            long cardId = cardItem.id;
            String question = cardItem.quiz.getQuestion();

            // Lấy trạng thái từ map
            boolean wasKnown = sessionProgress.getOrDefault(cardId, CardStatus.UNKNOWN) == CardStatus.KNOWN;
            results.add(new FlashcardResult(cardId, question, wasKnown));
        }

        // TODO: Lưu kết quả vào UserProgressRepository

        _sessionFinished.setValue(new SingleEvent<>(results));
    }
}