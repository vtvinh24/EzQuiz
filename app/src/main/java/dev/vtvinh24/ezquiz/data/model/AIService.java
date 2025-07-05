package dev.vtvinh24.ezquiz.data.model;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface AIService {
    /**
     * Generate quiz with support for both text prompts and image files
     * Uses multipart form data to match the Node.js backend with multer
     */
    @Multipart
    @POST("generate-quiz")
    Call<GenerateQuizResponse> generateQuiz(
            @Part("prompt") RequestBody prompt,
            @Part MultipartBody.Part file
    );

    /**
     * Generate quiz with only text prompt (when no image is provided)
     */
    @Multipart
    @POST("generate-quiz")
    Call<GenerateQuizResponse> generateQuizTextOnly(
            @Part("prompt") RequestBody prompt
    );
}