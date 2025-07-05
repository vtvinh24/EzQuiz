package dev.vtvinh24.ezquiz.data.model;

import dev.vtvinh24.ezquiz.data.model.GenerateQuizRequest;
import dev.vtvinh24.ezquiz.data.model.GenerateQuizResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface AIService {
    /**
     * Đường dẫn ở đây sẽ được nối vào BASE_URL_AI_SERVICE.
     * https://server-horusoul.onrender.com/ + generate-quiz
     * -> https://server-horusoul.onrender.com/generate-quiz
     *
     * Nếu bạn đã đặt tiền tố /api trên server (ví dụ: /api/generate-quiz),
     * thì ở đây bạn cần điền "api/generate-quiz".
     * Dựa vào link bạn đưa, có vẻ không có tiền tố /api.
     */
    @Headers("Content-Type: application/json") // Đảm bảo server nhận đúng định dạng
    @POST("generate-quiz")
    Call<GenerateQuizResponse> generateQuiz(@Body GenerateQuizRequest request);
}