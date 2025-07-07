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
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.repo.QuizRepository;
import dev.vtvinh24.ezquiz.util.QuizAttemptResult;
import dev.vtvinh24.ezquiz.util.SingleEvent;

public class PracticeViewModel extends AndroidViewModel {

    // Lớp nội bộ để giữ Quiz và câu trả lời của người dùng
    public static class PracticeItem {
        public final long id;
        public final Quiz quiz;
        public List<Integer> userAnswerIndices = new ArrayList<>();

        PracticeItem(long id, Quiz quiz) {
            this.id = id;
            this.quiz = quiz;
        }
    }

    private final QuizRepository quizRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

    private final MutableLiveData<List<PracticeItem>> _quizItems = new MutableLiveData<>();
    public final LiveData<List<PracticeItem>> quizItems = _quizItems;

    private final MutableLiveData<Integer> _currentQuestionPosition = new MutableLiveData<>(0);
    public final LiveData<Integer> currentQuestionPosition = _currentQuestionPosition;

    private final MutableLiveData<String> _sessionProgressText = new MutableLiveData<>();
    public final LiveData<String> sessionProgressText = _sessionProgressText;

    private final MutableLiveData<Boolean> _isAnswerChecked = new MutableLiveData<>(false);
    public final LiveData<Boolean> isAnswerChecked = _isAnswerChecked;

    private final MutableLiveData<SingleEvent<List<QuizAttemptResult>>> _sessionFinished = new MutableLiveData<>();
    public final LiveData<SingleEvent<List<QuizAttemptResult>>> sessionFinished = _sessionFinished;

    private final Map<Long, QuizAttemptResult> sessionResults = new HashMap<>();

    private int currentPosition = 0;

    public PracticeViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabaseProvider.getDatabase(application);
        this.quizRepository = new QuizRepository(db);
    }

    public void startSession(long quizSetId) {
        executor.execute(() -> {
            // Lấy các câu hỏi trắc nghiệm (không lấy flashcard)
            List<QuizEntity> quizEntities = quizRepository.getQuizzesOfSet(quizSetId);
            Collections.shuffle(quizEntities);

            List<PracticeItem> practiceItems = quizEntities.stream().map(entity -> {
                Quiz model = new Quiz(
                        entity.question, entity.answers, entity.correctAnswerIndices,
                        entity.type, entity.createdAt, entity.updatedAt,
                        entity.archived, entity.difficulty);
                return new PracticeItem(entity.id, model);
            }).collect(Collectors.toList());

            mainThreadHandler.post(() -> {
                _quizItems.setValue(practiceItems);
                _currentQuestionPosition.setValue(0);
                updateProgressText();
            });
        });
    }

    // Được gọi từ Fragment khi người dùng chọn/bỏ chọn một đáp án
    public void onAnswerSelected(long quizId, List<Integer> selectedIndices) {
        List<PracticeItem> items = _quizItems.getValue();
        if (items == null) return;
        for (PracticeItem item : items) {
            if (item.id == quizId) {
                item.userAnswerIndices = selectedIndices;
                break;
            }
        }
    }

    public void checkCurrentAnswer() {
        Integer position = _currentQuestionPosition.getValue();
        List<PracticeItem> items = _quizItems.getValue();
        if (position == null || items == null) return;

        PracticeItem currentItem = items.get(position);
        List<Integer> userAnswers = currentItem.userAnswerIndices;
        List<Integer> correctAnswers = currentItem.quiz.getCorrectAnswerIndices();

        Collections.sort(userAnswers);
        Collections.sort(correctAnswers);

        boolean isCorrect = userAnswers.equals(correctAnswers);
        sessionResults.put(currentItem.id, new QuizAttemptResult(currentItem.id, isCorrect));

        // Báo cho UI biết là đã check xong để cập nhật Fragment và Activity
        _isAnswerChecked.postValue(true);
    }

    public Quiz getCurrentQuiz() {
        List<PracticeItem> items = quizItems.getValue();
        if (items != null && currentPosition < items.size()) {
            return items.get(currentPosition).quiz;
        }
        return null;
    }

    public long getCurrentQuizId() {
        List<PracticeItem> items = quizItems.getValue();
        if (items != null && currentPosition < items.size()) {
            return items.get(currentPosition).id;
        }
        return -1;
    }

    public int getCurrentPosition() {
        return currentPosition;
    }

    public void moveToNextQuestion() {
        List<PracticeItem> items = quizItems.getValue();
        if (items != null && currentPosition < items.size() - 1) {
            currentPosition++;
            _currentQuestionPosition.setValue(currentPosition);
            _isAnswerChecked.setValue(false);
        } else {
            _sessionFinished.setValue(new SingleEvent<>(new ArrayList<>(sessionResults.values())));
        }
    }

    private void updateProgressText() {
        Integer pos = _currentQuestionPosition.getValue();
        List<PracticeItem> items = _quizItems.getValue();
        if (pos != null && items != null && !items.isEmpty()) {
            _sessionProgressText.setValue((pos + 1) + " / " + items.size());
        }
    }
}