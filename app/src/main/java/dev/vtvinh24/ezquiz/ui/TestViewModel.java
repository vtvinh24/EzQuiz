package dev.vtvinh24.ezquiz.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.repo.QuizRepository;
import dev.vtvinh24.ezquiz.util.SingleEvent;

public class TestViewModel extends AndroidViewModel {


    public static class TestQuestionItem implements Serializable {
        public final long id;
        public final Quiz quiz;
        public List<Integer> userAnswerIndices = new ArrayList<>();

        TestQuestionItem(long id, Quiz quiz) {
            this.id = id;
            this.quiz = quiz;
        }
    }


    public static class TestResult implements Serializable {
        public final int correctCount;
        public final int totalCount;

        TestResult(int correctCount, int totalCount) {
            this.correctCount = correctCount;
            this.totalCount = totalCount;
        }
    }

    private final QuizRepository quizRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private long testStartTime = 0;


    private final MutableLiveData<List<TestQuestionItem>> _questions = new MutableLiveData<>();
    public final LiveData<List<TestQuestionItem>> questions = _questions;


    private final MutableLiveData<Boolean> _isGrading = new MutableLiveData<>(false);
    public final LiveData<Boolean> isGrading = _isGrading;


    private final MutableLiveData<SingleEvent<Void>> _unansweredWarning = new MutableLiveData<>();
    public final LiveData<SingleEvent<Void>> unansweredWarning = _unansweredWarning;


    private final MutableLiveData<SingleEvent<TestResult>> _sessionFinished = new MutableLiveData<>();
    public final LiveData<SingleEvent<TestResult>> sessionFinished = _sessionFinished;


    public TestViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabaseProvider.getDatabase(application);
        this.quizRepository = new QuizRepository(db);
    }

    public void startTest(long quizSetId, int totalCount, boolean isMcChecked, boolean isTfChecked) {
        testStartTime = System.currentTimeMillis(); // Bắt đầu đếm thời gian
        executor.execute(() -> {
            ArrayList<Quiz.Type> typesToFetch = new ArrayList<>();
            typesToFetch.add(Quiz.Type.SINGLE_CHOICE);
            typesToFetch.add(Quiz.Type.MULTIPLE_CHOICE);

            List<QuizEntity> allOriginalQuizzes = quizRepository.getQuizzesOfSetByTypes(quizSetId, typesToFetch);

            if (allOriginalQuizzes == null || allOriginalQuizzes.isEmpty()) {
                _questions.postValue(new ArrayList<>());
                return;
            }


            List<TestQuestionItem> mcItems = new ArrayList<>();
            List<TestQuestionItem> tfItems = new ArrayList<>();


            if (isMcChecked) {
                for (QuizEntity entity : allOriginalQuizzes) {
                    if (entity.answers != null && entity.answers.size() > 2) {
                        Quiz model = new Quiz(entity.question, entity.answers, entity.correctAnswerIndices,
                                entity.type, entity.createdAt, entity.updatedAt, entity.archived, entity.difficulty);
                        mcItems.add(new TestQuestionItem(entity.id, model));
                    }
                }
            }

            if (isTfChecked) {
                long generatedIdCounter = -1L;
                String trueString = getApplication().getString(R.string.answer_true);
                String falseString = getApplication().getString(R.string.answer_false);

                for (QuizEntity originalEntity : allOriginalQuizzes) {
                    if (originalEntity.answers == null || originalEntity.answers.isEmpty()) continue;

                    for (int i = 0; i < originalEntity.answers.size(); i++) {
                        String newQuestionText = originalEntity.question + "\n\n" + originalEntity.answers.get(i);
                        List<String> newAnswers = Arrays.asList(trueString, falseString);
                        boolean isStatementTrue = originalEntity.correctAnswerIndices.contains(i);
                        List<Integer> newCorrectAnswerIndex = Collections.singletonList(isStatementTrue ? 0 : 1);

                        Quiz newTfQuiz = new Quiz(newQuestionText, newAnswers, newCorrectAnswerIndex,
                                Quiz.Type.SINGLE_CHOICE, originalEntity.createdAt, originalEntity.updatedAt, false, originalEntity.difficulty);

                        tfItems.add(new TestQuestionItem(generatedIdCounter--, newTfQuiz));
                    }
                }
            }


            Collections.shuffle(mcItems);
            Collections.shuffle(tfItems);


            List<TestQuestionItem> finalCombinedList = new ArrayList<>();
            finalCombinedList.addAll(mcItems);
            finalCombinedList.addAll(tfItems);


            List<TestQuestionItem> limitedList = finalCombinedList.stream().limit(totalCount).collect(Collectors.toList());

            _questions.postValue(limitedList);
        });
    }


    public void onAnswerSelected(long quizId, List<Integer> selectedIndices) {
        List<TestQuestionItem> currentList = _questions.getValue();
        if (currentList == null) return;
        for (TestQuestionItem item : currentList) {
            if (item.id == quizId) {
                item.userAnswerIndices = selectedIndices;
                break;
            }
        }
    }


    public void submitTest() {
        List<TestQuestionItem> currentList = _questions.getValue();
        if (currentList == null || currentList.isEmpty()) {
            return;
        }

        boolean allAnswered = currentList.stream().allMatch(item -> !item.userAnswerIndices.isEmpty());

        if (allAnswered) {
            gradeTest();
        } else {
            _unansweredWarning.postValue(new SingleEvent<>(null));
        }
    }


    public void forceSubmitTest() {
        gradeTest();
    }

    public long getTestDuration() {
        if (testStartTime == 0) {
            return 0;
        }
        return System.currentTimeMillis() - testStartTime;
    }

    private void gradeTest() {
        _isGrading.postValue(true);
        executor.execute(() -> {
            List<TestQuestionItem> questions = _questions.getValue();
            if (questions == null) {
                _isGrading.postValue(false);
                return;
            }
            int correctCount = 0;
            for (TestQuestionItem item : questions) {

                Collections.sort(item.userAnswerIndices);
                if (item.quiz.getCorrectAnswerIndices() != null) {
                    Collections.sort(item.quiz.getCorrectAnswerIndices());
                }


                boolean isCorrect = item.userAnswerIndices.equals(item.quiz.getCorrectAnswerIndices());
                if (isCorrect) {
                    correctCount++;
                }
            }
            TestResult result = new TestResult(correctCount, questions.size());
            _sessionFinished.postValue(new SingleEvent<>(result));
            _isGrading.postValue(false);
        });
    }
}