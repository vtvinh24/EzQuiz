package dev.vtvinh24.ezquiz.ui.play;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import dev.vtvinh24.ezquiz.data.model.PlaySettings;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.domain.PlaySessionManager;

/**
 * ViewModel for quiz play functionality.
 * Handles the logic for quiz play sessions including retry, shuffle and spaced repetition.
 */
public class PlaySessionViewModel extends ViewModel {

    private PlaySessionManager playSessionManager;
    private MutableLiveData<Quiz> currentQuiz = new MutableLiveData<>();
    private MutableLiveData<Integer> score = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> isSessionComplete = new MutableLiveData<>(false);
    private PlaySettings playSettings = new PlaySettings();

    // Fields and methods will be implemented to handle quiz play logic

    public LiveData<Quiz> getCurrentQuiz() {
        return currentQuiz;
    }

    public LiveData<Integer> getScore() {
        return score;
    }

    public LiveData<Boolean> isSessionComplete() {
        return isSessionComplete;
    }

    public PlaySettings getPlaySettings() {
        return playSettings;
    }

    public void setPlaySettings(PlaySettings playSettings) {
        this.playSettings = playSettings;
    }
}
