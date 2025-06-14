package dev.vtvinh24.ezquiz.domain;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import dev.vtvinh24.ezquiz.domain.model.Answer;
import dev.vtvinh24.ezquiz.domain.model.Question;
import dev.vtvinh24.ezquiz.domain.model.Quiz;
import dev.vtvinh24.ezquiz.domain.repository.QuizRepository;

/**
 * Manager class responsible for importing quizzes from various external sources.
 * Currently supports CSV and JSON formats.
 */
public class ImportManager {
    private static final String TAG = "ImportManager";

    private final Context context;
    private final QuizRepository quizRepository;

    /**
     * Constructs a new ImportManager.
     *
     * @param context The application context.
     * @param quizRepository Repository for storing imported quizzes.
     */
    public ImportManager(Context context, QuizRepository quizRepository) {
        this.context = context;
        this.quizRepository = quizRepository;
    }

    /**
     * Imports a quiz from a file specified by URI.
     *
     * @param fileUri The URI of the file to import.
     * @param format The format of the file (csv, json, etc.).
     * @return The imported quiz if successful, null otherwise.
     */
    public Quiz importQuiz(Uri fileUri, ImportFormat format) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                Log.e(TAG, "Failed to open input stream for URI: " + fileUri);
                return null;
            }

            Quiz quiz;
            switch (format) {
                case CSV:
                    quiz = importFromCsv(inputStream);
                    break;
                case JSON:
                    quiz = importFromJson(inputStream);
                    break;
                default:
                    Log.e(TAG, "Unsupported import format: " + format);
                    inputStream.close();
                    return null;
            }

            if (quiz != null) {
                // Save the imported quiz to repository
                return quizRepository.saveQuiz(quiz);
            }

            inputStream.close();
            return null;
        } catch (IOException e) {
            Log.e(TAG, "Error reading from input stream", e);
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Error during quiz import", e);
            return null;
        }
    }

    /**
     * Imports a quiz from a CSV input stream.
     *
     * @param inputStream The input stream to read from.
     * @return The imported quiz.
     * @throws IOException If there's an error reading from the stream.
     */
    private Quiz importFromCsv(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        // Read header line (title, description, author)
        String headerLine = reader.readLine();
        if (headerLine == null) {
            return null;
        }

        String[] quizInfo = headerLine.split(",");
        if (quizInfo.length < 2) {
            return null;
        }

        String title = quizInfo[0].trim();
        String description = quizInfo.length > 1 ? quizInfo[1].trim() : "";
        String author = quizInfo.length > 2 ? quizInfo[2].trim() : "Unknown";

        List<Question> questions = new ArrayList<>();

        // Read questions and answers
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.trim().isEmpty()) {
                continue; // Skip empty lines
            }

            String[] parts = line.split(",");
            if (parts.length < 3) {
                continue; // Skip invalid lines
            }

            // Format: question_text,question_type,points,answer1,isCorrect1,answer2,isCorrect2,...
            String questionText = parts[0].trim();
            Question.QuestionType questionType = parseQuestionType(parts[1].trim());
            int points = Integer.parseInt(parts[2].trim());

            List<Answer> answers = new ArrayList<>();

            // Parse answers (they come in pairs: text, isCorrect)
            for (int i = 3; i < parts.length - 1; i += 2) {
                String answerText = parts[i].trim();
                boolean isCorrect = Boolean.parseBoolean(parts[i + 1].trim());
                answers.add(new Answer(answerText, isCorrect));
            }

            questions.add(new Question(questionText, answers, questionType, points));
        }

        return new Quiz(title, description, questions, author);
    }

    /**
     * Imports a quiz from a JSON input stream.
     *
     * @param inputStream The input stream to read from.
     * @return The imported quiz.
     * @throws IOException If there's an error reading from the stream.
     */
    private Quiz importFromJson(InputStream inputStream) throws IOException {
        // In a real implementation, this would use a JSON parsing library like Gson or Moshi
        // For this minimal implementation, we'll return null
        Log.d(TAG, "JSON import not fully implemented yet");
        return null;
    }

    /**
     * Parse a string representation of question type to enum.
     *
     * @param typeString The string representation of the question type.
     * @return The corresponding QuestionType enum value.
     */
    private Question.QuestionType parseQuestionType(String typeString) {
        try {
            return Question.QuestionType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Default to single choice if type is unknown
            return Question.QuestionType.SINGLE_CHOICE;
        }
    }

    /**
     * Supported import formats.
     */
    public enum ImportFormat {
        CSV,
        JSON
    }
}
