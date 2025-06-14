package dev.vtvinh24.ezquiz.ui.import_quiz;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import dev.vtvinh24.ezquiz.data.model.QuizCollection;
import dev.vtvinh24.ezquiz.domain.ImportManager;

/**
 * ViewModel for handling quiz imports from external sources like Quizlet.
 */
public class ImportViewModel extends ViewModel {

    private ImportManager importManager;
    private MutableLiveData<Boolean> isImporting = new MutableLiveData<>(false);
    private MutableLiveData<QuizCollection> importedCollection = new MutableLiveData<>();
    private MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public LiveData<Boolean> isImporting() {
        return isImporting;
    }

    public LiveData<QuizCollection> getImportedCollection() {
        return importedCollection;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Imports a quiz collection from a Quizlet URL.
     *
     * @param quizletUrl URL of the Quizlet set to import
     */
    public void importFromQuizlet(String quizletUrl) {
        // Will be implemented to handle actual import logic
    }
}
