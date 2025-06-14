package dev.vtvinh24.ezquiz.ui.editor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

import dev.vtvinh24.ezquiz.domain.model.Answer;
import dev.vtvinh24.ezquiz.domain.model.Question;
import dev.vtvinh24.ezquiz.domain.model.Quiz;
import dev.vtvinh24.ezquiz.domain.repository.QuizRepository;

/**
 * ViewModel for Quiz Editor screen.
 * Manages UI-related data and business logic for creating and editing quizzes.
 */
public class EditorViewModel extends ViewModel {

    private final QuizRepository quizRepository;
    private final MutableLiveData<Quiz> currentQuiz = new MutableLiveData<>(new Quiz());
    private final MutableLiveData<Boolean> isSaving = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> saveSuccess = new MutableLiveData<>();

    public EditorViewModel(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
        // Initialize an empty quiz
        Quiz quiz = new Quiz();
        quiz.setQuestions(new ArrayList<>());
        currentQuiz.setValue(quiz);
    }

    /**
     * Load a quiz by ID for editing.
     *
     * @param quizId ID of the quiz to edit.
     */
    public void loadQuiz(String quizId) {
        Quiz quiz = quizRepository.getQuizById(quizId);
        if (quiz != null) {
            currentQuiz.setValue(quiz);
        }
    }

    /**
     * Update the quiz title.
     *
     * @param title New title for the quiz.
     */
    public void setQuizTitle(String title) {
        Quiz quiz = currentQuiz.getValue();
        if (quiz != null) {
            quiz.setTitle(title);
            currentQuiz.setValue(quiz);
        }
    }

    /**
     * Update the quiz description.
     *
     * @param description New description for the quiz.
     */
    public void setQuizDescription(String description) {
        Quiz quiz = currentQuiz.getValue();
        if (quiz != null) {
            quiz.setDescription(description);
            currentQuiz.setValue(quiz);
        }
    }

    /**
     * Add a new question to the quiz.
     *
     * @param questionText Text of the question.
     * @param type Type of question.
     * @param points Points for the question.
     */
    public void addQuestion(String questionText, Question.QuestionType type, int points) {
        Quiz quiz = currentQuiz.getValue();
        if (quiz != null) {
            List<Question> questions = quiz.getQuestions();
            if (questions == null) {
                questions = new ArrayList<>();
                quiz.setQuestions(questions);
            }

            Question question = new Question(questionText, new ArrayList<>(), type, points);
            questions.add(question);
            currentQuiz.setValue(quiz);
        }
    }

    /**
     * Add an answer to a specific question.
     *
     * @param questionIndex Index of the question to add the answer to.
     * @param answerText Text of the answer.
     * @param isCorrect Whether this answer is correct.
     */
    public void addAnswer(int questionIndex, String answerText, boolean isCorrect) {
        Quiz quiz = currentQuiz.getValue();
        if (quiz != null && quiz.getQuestions() != null && questionIndex >= 0 && questionIndex < quiz.getQuestions().size()) {
            Question question = quiz.getQuestions().get(questionIndex);
            List<Answer> answers = question.getAnswers();
            if (answers == null) {
                answers = new ArrayList<>();
                question.setAnswers(answers);
            }

            Answer answer = new Answer(answerText, isCorrect);
            answers.add(answer);
            currentQuiz.setValue(quiz);
        }
    }

    /**
     * Save the current quiz.
     */
    public void saveQuiz() {
        Quiz quiz = currentQuiz.getValue();
        if (quiz != null) {
            isSaving.setValue(true);
            try {
                Quiz savedQuiz = quizRepository.saveQuiz(quiz);
                currentQuiz.setValue(savedQuiz);
                saveSuccess.setValue(true);
            } catch (Exception e) {
                saveSuccess.setValue(false);
            } finally {
                isSaving.setValue(false);
            }
        }
    }

    /**
     * Get the current quiz being edited.
     *
     * @return LiveData containing the current quiz.
     */
    public LiveData<Quiz> getCurrentQuiz() {
        return currentQuiz;
    }

    /**
     * Get the saving state.
     *
     * @return LiveData containing the saving state.
     */
    public LiveData<Boolean> getIsSaving() {
        return isSaving;
    }

    /**
     * Get the save operation result.
     *
     * @return LiveData containing whether the save was successful.
     */
    public LiveData<Boolean> getSaveSuccess() {
        return saveSuccess;
    }
}
