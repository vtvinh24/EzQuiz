package dev.vtvinh24.ezquiz.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;
import dev.vtvinh24.ezquiz.data.model.Quiz;

public class GeneratedQuizItem implements Serializable {
    @SerializedName("question")
    public String question;

    @SerializedName("answers")
    public List<String> answers;

    @SerializedName("correctAnswerIndices")
    public List<Integer> correctAnswerIndices;

    @SerializedName("type")
    public Quiz.Type type; // Gson sẽ tự động map string "SINGLE_CHOICE" thành enum
}