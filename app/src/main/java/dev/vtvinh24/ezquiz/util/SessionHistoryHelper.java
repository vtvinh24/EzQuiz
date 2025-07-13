package dev.vtvinh24.ezquiz.util;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.vtvinh24.ezquiz.data.model.DetailedSessionResult;
import dev.vtvinh24.ezquiz.data.repo.HistoryRepository;

import java.lang.reflect.Type;
import java.util.List;

public class SessionHistoryHelper {
    private static final Gson gson = new Gson();

    public static void saveTestSession(Context context, long quizSetId, int totalQuestions,
                                     int correctAnswers, long timeSpent,
                                     List<DetailedSessionResult.QuestionResult> questionResults) {

        HistoryRepository historyRepository = new HistoryRepository(context);

        int incorrectAnswers = totalQuestions - correctAnswers;
        int skippedAnswers = 0; // Test thường không có skip
        double scorePercentage = (correctAnswers * 100.0) / totalQuestions;

        String detailedResults = serializeQuestionResults(questionResults);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - timeSpent;

        historyRepository.saveSessionHistory(
            quizSetId,
            "TEST",
            totalQuestions,
            correctAnswers,
            incorrectAnswers,
            skippedAnswers,
            scorePercentage,
            timeSpent,
            detailedResults,
            true, // Test luôn hoàn thành
            startTime,
            endTime
        );
    }

    public static void savePracticeSession(Context context, long quizSetId, int totalQuestions,
                                         int correctAnswers, int incorrectAnswers, int skippedAnswers,
                                         long timeSpent, boolean isCompleted,
                                         List<DetailedSessionResult.QuestionResult> questionResults) {

        HistoryRepository historyRepository = new HistoryRepository(context);

        double scorePercentage = totalQuestions > 0 ? (correctAnswers * 100.0) / totalQuestions : 0;

        String detailedResults = serializeQuestionResults(questionResults);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - timeSpent;

        historyRepository.saveSessionHistory(
            quizSetId,
            "PRACTICE",
            totalQuestions,
            correctAnswers,
            incorrectAnswers,
            skippedAnswers,
            scorePercentage,
            timeSpent,
            detailedResults,
            isCompleted,
            startTime,
            endTime
        );
    }

    public static void saveFlashcardSession(Context context, long quizSetId, int totalCards,
                                          int knownCards, int unknownCards, long timeSpent,
                                          boolean isCompleted,
                                          List<DetailedSessionResult.QuestionResult> cardResults) {

        HistoryRepository historyRepository = new HistoryRepository(context);

        double scorePercentage = totalCards > 0 ? (knownCards * 100.0) / totalCards : 0;

        String detailedResults = serializeQuestionResults(cardResults);

        long endTime = System.currentTimeMillis();
        long startTime = endTime - timeSpent;

        historyRepository.saveSessionHistory(
            quizSetId,
            "FLASHCARD",
            totalCards,
            knownCards,
            unknownCards,
            0, // Flashcard không có skip
            scorePercentage,
            timeSpent,
            detailedResults,
            isCompleted,
            startTime,
            endTime
        );
    }

    private static String serializeQuestionResults(List<DetailedSessionResult.QuestionResult> results) {
        if (results == null || results.isEmpty()) {
            return "[]";
        }
        return gson.toJson(results);
    }

    public static List<DetailedSessionResult.QuestionResult> deserializeQuestionResults(String json) {
        if (json == null || json.isEmpty() || json.equals("[]")) {
            return null;
        }

        Type listType = new TypeToken<List<DetailedSessionResult.QuestionResult>>(){}.getType();
        return gson.fromJson(json, listType);
    }
}
