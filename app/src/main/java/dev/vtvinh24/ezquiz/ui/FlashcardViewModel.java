package dev.vtvinh24.ezquiz.ui;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;
import android.util.Log; // THÊM: Import Log
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
import dev.vtvinh24.ezquiz.data.entity.QuizEntity; // Vẫn cần để nhận từ Repository
import dev.vtvinh24.ezquiz.data.model.CardStatus;
import dev.vtvinh24.ezquiz.data.model.FlashcardResult;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.model.UserQuizProgress;
import dev.vtvinh24.ezquiz.data.repo.QuizRepository;
import dev.vtvinh24.ezquiz.data.repo.UserProgressRepository;
import dev.vtvinh24.ezquiz.util.SingleEvent;

// ViewModel bây giờ hoạt động với Quiz model không có ID, sử dụng QuizDisplayItem
public class FlashcardViewModel extends AndroidViewModel {

    // Lớp Wrapper nội bộ để kết hợp ID (từ Entity) và Quiz Model (không ID)
    // Lớp UI (Adapter) sẽ làm việc với đối tượng này
    public static class QuizDisplayItem {
        public final long id; // ID này đến từ QuizEntity
        public final Quiz quiz; // Quiz model không có ID

        QuizDisplayItem(long id, Quiz quiz) {
            this.id = id;
            this.quiz = quiz;
        }
    }

    private final QuizRepository quizRepository; // Để lấy QuizEntity từ DB
    private final UserProgressRepository userProgressRepository; // Để lưu tiến độ người dùng
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<String> _sessionProgressText = new MutableLiveData<>();
    public final LiveData<String> sessionProgressText = _sessionProgressText;

    private final MutableLiveData<SingleEvent<List<FlashcardResult>>> _sessionFinished = new MutableLiveData<>();
    public final LiveData<SingleEvent<List<FlashcardResult>>> sessionFinished = _sessionFinished;

    // LiveData bây giờ chứa danh sách các QuizDisplayItem
    private final MutableLiveData<List<QuizDisplayItem>> _flashcards = new MutableLiveData<>();
    public final LiveData<List<QuizDisplayItem>> flashcards = _flashcards;

    private final MutableLiveData<Integer> _currentCardPosition = new MutableLiveData<>();
    public final LiveData<Integer> currentCardPosition = _currentCardPosition;

    // Map để theo dõi tiến trình, sử dụng ID (Long) từ QuizDisplayItem
    private Map<Long, CardStatus> sessionProgress = new HashMap<>();

    public FlashcardViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabaseProvider.getDatabase(application);
        this.quizRepository = new QuizRepository(db); // Khởi tạo QuizRepository
        // Giả định UserProgressRepository đã được sửa để lấy DAOs từ AppDatabase và hoạt động đúng
        this.userProgressRepository = new UserProgressRepository(application); // Truyền Application context
    }

    public void startSession(long quizSetId) {
        // === THÊM DÒNG LOG NÀY VÀO ===
        android.util.Log.d("DEBUG_FLASHCARD", "ViewModel startSession called with quizSetId: " + quizSetId);
        executor.execute(() -> {
            try {
                List<QuizEntity> flashcardEntities = quizRepository.getFlashcardsOfSet(quizSetId);

                // ===============================================
                // === THÊM DÒNG LOG QUAN TRỌNG NÀY VÀO ===
                if (flashcardEntities == null) {
                    android.util.Log.d("DEBUG_FLASHCARD", "Repository returned a NULL list.");
                } else {
                    android.util.Log.d("DEBUG_FLASHCARD", "Repository returned a list with size: " + flashcardEntities.size());
                }
                // ===============================================


                if (flashcardEntities == null || flashcardEntities.isEmpty()) {
                    Log.d("FlashcardViewModel", "No flashcard entities found for quizSetId: " + quizSetId);
                    mainThreadHandler.post(() -> {
                        _flashcards.setValue(Collections.emptyList()); // Đặt danh sách rỗng để UI cập nhật
                        _sessionProgressText.setValue("0 / 0");
                        _currentCardPosition.setValue(0);
                        // Có thể hiển thị thông báo "Không có thẻ nào" qua một LiveData khác
                    });
                    return;
                }

                // Chuyển từ List<QuizEntity> đã lọc sang List<QuizDisplayItem>
                List<QuizDisplayItem> displayItems = flashcardEntities.stream().map(entity -> {
                    // Tạo Quiz Model (KHÔNG CÓ ID) từ Entity
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
                    // Tạo đối tượng hiển thị bao gồm cả ID từ Entity và Quiz Model
                    return new QuizDisplayItem(entity.id, model);
                }).collect(Collectors.toList());

                Collections.shuffle(displayItems);

                mainThreadHandler.post(() -> {
                    _flashcards.setValue(displayItems); // Cập nhật LiveData với danh sách QuizDisplayItem
                    _currentCardPosition.setValue(0);
                    sessionProgress.clear();
                    updateProgressText();
                });
            } catch (Exception e) {
                Log.e("FlashcardViewModel", "Error loading flashcards: " + e.getMessage(), e);
                mainThreadHandler.post(() -> {
                    _flashcards.setValue(Collections.emptyList());
                    _sessionProgressText.setValue("Error loading");
                    _currentCardPosition.setValue(0);
                    // Có thể thông báo lỗi cho người dùng
                });
            }
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
        List<QuizDisplayItem> cards = _flashcards.getValue(); // Bây giờ là List<QuizDisplayItem>
        if (position != null && cards != null && position < cards.size()) {
            // Lấy ID từ QuizDisplayItem
            long currentQuizId = cards.get(position).id;
            sessionProgress.put(currentQuizId, status);
        }
    }

    private void moveToNextCard() {
        Integer position = _currentCardPosition.getValue();
        List<QuizDisplayItem> cards = _flashcards.getValue(); // Bây giờ là List<QuizDisplayItem>
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
        List<QuizDisplayItem> cards = _flashcards.getValue(); // Bây giờ là List<QuizDisplayItem>
        if (position != null && cards != null) {
            _sessionProgressText.setValue((position + 1) + " / " + cards.size());
        } else {
            _sessionProgressText.setValue("0 / 0"); // Xử lý trường hợp list rỗng
        }
    }

    private void finishSession() {
        List<QuizDisplayItem> cards = _flashcards.getValue(); // Bây giờ là List<QuizDisplayItem>
        if (cards == null) {
            Log.w("FlashcardViewModel", "finishSession called but flashcard list is null.");
            return;
        }

        List<FlashcardResult> results = new ArrayList<>();
        for (QuizDisplayItem cardItem : cards) { // Lặp qua List<QuizDisplayItem>
            long cardId = cardItem.id; // Lấy ID từ QuizDisplayItem
            String question = cardItem.quiz.getQuestion();

            boolean wasKnown = sessionProgress.getOrDefault(cardId, CardStatus.UNKNOWN) == CardStatus.KNOWN;
            results.add(new FlashcardResult(cardId, question, wasKnown));
        }

        // BẮT ĐẦU: LƯU KẾT QUẢ VÀO UserProgressRepository
        executor.execute(() -> {
            try {
                long now = System.currentTimeMillis();
                for (FlashcardResult result : results) {
                    UserQuizProgress progress = userProgressRepository.getQuizProgress(result.getQuizId());
                    if (progress == null) {
                        progress = new UserQuizProgress();
                        // Nếu UserQuizProgress model có constructor không tham số, các field sẽ là giá trị mặc định (0/false/null)
                        // Bạn cần set các ID liên quan ở đây nếu UserQuizProgress cần biết nó là của Quiz nào
                        // Ví dụ: progress.quizId = result.getQuizId(); (Nếu UserQuizProgress có trường quizId)
                    }
                    progress.isPinned = false;
                    progress.isFavorite = result.wasKnown(); // Ví dụ: coi là yêu thích nếu biết
                    progress.lastAttemptedAt = now;
                    progress.attemptCount++;
                    if (result.wasKnown()) {
                        progress.correctAttemptCount++;
                    }
                    // Tránh chia cho 0 nếu attemptCount là 0
                    progress.successRate = (progress.attemptCount == 0) ? 0 : (float) progress.correctAttemptCount / progress.attemptCount;
                    progress.archived = false;
                    progress.notes = "";
                    progress.difficulty = 0;
                    progress.type = Quiz.Type.FLASHCARD; // Đảm bảo type được set cho tiến độ flashcard
                    userProgressRepository.setQuizProgress(result.getQuizId(), progress);
                }
                // TODO: Bạn có thể thêm logic để cập nhật UserQuizSetProgress và UserQuizCollectionProgress tại đây
                // dựa trên tổng hợp các kết quả của quiz trong set/collection

                mainThreadHandler.post(() -> {
                    _sessionFinished.setValue(new SingleEvent<>(results));
                });
            } catch (Exception e) {
                Log.e("FlashcardViewModel", "Error saving flashcard progress: " + e.getMessage(), e);
                // Có thể báo lỗi cho UI hoặc chỉ log
            }
        });
        // KẾT THÚC: LƯU KẾT QUẢ
    }
}