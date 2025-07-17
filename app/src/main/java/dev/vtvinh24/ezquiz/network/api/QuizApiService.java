package dev.vtvinh24.ezquiz.network.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

import dev.vtvinh24.ezquiz.network.api.model.AuthResponse;
import dev.vtvinh24.ezquiz.network.api.model.LoginRequest;
import dev.vtvinh24.ezquiz.network.api.model.RegisterRequest;
import dev.vtvinh24.ezquiz.network.api.model.UserProfileResponse;
import dev.vtvinh24.ezquiz.network.api.model.TokenValidationResponse;
import dev.vtvinh24.ezquiz.network.api.model.QuizListResponse;
import dev.vtvinh24.ezquiz.network.api.model.QuizDetailResponse;
import dev.vtvinh24.ezquiz.network.api.model.SaveQuizRequest;
import dev.vtvinh24.ezquiz.network.api.model.QuizSaveResponse;
import dev.vtvinh24.ezquiz.network.api.model.UpdateQuizRequest;
import dev.vtvinh24.ezquiz.network.api.model.QuizUpdateResponse;
import dev.vtvinh24.ezquiz.network.api.model.QuizDeleteResponse;
import dev.vtvinh24.ezquiz.network.api.model.GenerateQuizRequest;
import dev.vtvinh24.ezquiz.network.api.model.GeneratedQuizResponse;

public interface QuizApiService {

    // Auth endpoints
    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest request);

    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest request);

    @GET("me")
    Call<UserProfileResponse> getUserProfile(@Header("Authorization") String token);

    @GET("verify-token")
    Call<TokenValidationResponse> verifyToken(@Header("Authorization") String token);

    // Quiz endpoints
    @GET("quizzes")
    Call<QuizListResponse> getUserQuizzes(
        @Header("Authorization") String token,
        @Query("page") int page,
        @Query("limit") int limit
    );

    @GET("quizzes/{id}")
    Call<QuizDetailResponse> getQuizById(
        @Header("Authorization") String token,
        @Path("id") String quizId
    );

    @POST("quizzes")
    Call<QuizSaveResponse> saveQuiz(
        @Header("Authorization") String token,
        @Body SaveQuizRequest request
    );

    @PUT("quizzes/{id}")
    Call<QuizUpdateResponse> updateQuiz(
        @Header("Authorization") String token,
        @Path("id") String quizId,
        @Body UpdateQuizRequest request
    );

    @DELETE("quizzes/{id}")
    Call<QuizDeleteResponse> deleteQuiz(
        @Header("Authorization") String token,
        @Path("id") String quizId
    );

    // AI Quiz Generation
    @POST("generate-quiz")
    Call<GeneratedQuizResponse> generateQuiz(
        @Header("Authorization") String token,
        @Body GenerateQuizRequest request
    );
}
