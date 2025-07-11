package dev.vtvinh24.ezquiz.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.repo.QuizRepository;

public class TestConfigViewModel extends AndroidViewModel {

    private final QuizRepository quizRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Integer> _mcCount = new MutableLiveData<>(0);
    public final LiveData<Integer> mcCount = _mcCount;

    private final MutableLiveData<Integer> _tfCount = new MutableLiveData<>(0);
    public final LiveData<Integer> tfCount = _tfCount;

    public TestConfigViewModel(@NonNull Application application) {
        super(application);
        AppDatabase db = AppDatabaseProvider.getDatabase(application);
        this.quizRepository = new QuizRepository(db);
    }

    public void loadAllTestableQuizzes(long quizSetId) {
        executor.execute(() -> {
            ArrayList<Quiz.Type> typesToFetch = new ArrayList<>();
            typesToFetch.add(Quiz.Type.SINGLE_CHOICE);
            typesToFetch.add(Quiz.Type.MULTIPLE_CHOICE);

            List<QuizEntity> allQuizzes = quizRepository.getQuizzesOfSetByTypes(quizSetId, typesToFetch);

            if (allQuizzes == null) {
                _mcCount.postValue(0);
                _tfCount.postValue(0);
                return;
            }


            int availableMcCount = 0;

            int availableTfCount = 0;

            for (QuizEntity quiz : allQuizzes) {
                if (quiz.answers != null && !quiz.answers.isEmpty()) {

                    availableTfCount++;


                        availableMcCount++;

                }
            }

            _mcCount.postValue(availableMcCount);
            _tfCount.postValue(availableTfCount);





        });
    }
}
