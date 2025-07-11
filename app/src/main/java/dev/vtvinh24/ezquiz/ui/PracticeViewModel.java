package dev.vtvinh24.ezquiz.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

    public static class PracticeItem {
        public final long id;
        public final Quiz quiz;
        public List<Integer> userAnswerIndices = new ArrayList<>();

        PracticeItem(long id, Quiz quiz) { this.id = id; this.quiz = quiz; }
    }

    private final QuizRepository quizRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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

    private final List<QuizAttemptResult> sessionResults = new ArrayList<>();
    private int currentPosition = 0;

    public PracticeViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabaseProvider.getDatabase(application);
        this.quizRepository = new QuizRepository(db);
    }

    public void startSession(long quizSetId) {
        executor.execute(() -> {
            List<QuizEntity> quizEntities = quizRepository.getQuizzesOfSet(quizSetId);
            Collections.shuffle(quizEntities);

            List<PracticeItem> practiceItems = quizEntities.stream().map(entity -> {
                Quiz model = new Quiz(entity.question, entity.answers, entity.correctAnswerIndices,
                        entity.type, entity.createdAt, entity.updatedAt,
                        entity.archived, entity.difficulty);
                return new PracticeItem(entity.id, model);
            }).collect(Collectors.toList());

            _quizItems.postValue(practiceItems);
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
    }

    public void moveToNextQuestion() {
        List<PracticeItem> items = _quizItems.getValue();
        if (items != null && currentPosition < items.size() - 1) {
            currentPosition++;
            _currentQuestionPosition.setValue(currentPosition);
            _isAnswerChecked.setValue(false);
            _canCheckAnswer.setValue(false);
        } else {
            _sessionFinished.setValue(new SingleEvent<>(new ArrayList<>(sessionResults)));
        }
    }

    public int getCurrentPosition() { return currentPosition; }
}