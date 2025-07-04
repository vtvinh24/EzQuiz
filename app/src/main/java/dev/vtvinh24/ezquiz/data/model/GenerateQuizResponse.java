package dev.vtvinh24.ezquiz.data.model;

import com.google.gson.annotations.SerializedName; // Import thư viện này
import java.util.List;

public class GenerateQuizResponse {
    // Dùng @SerializedName để đảm bảo Gson map đúng key "quizzes" từ JSON
    // vào biến này, kể cả khi bạn đổi tên biến trong Java.
    @SerializedName("quizzes")
    private List<GeneratedQuizItem> quizzes;

    public List<GeneratedQuizItem> getQuizzes() {
        return quizzes;
    }
}