package dev.vtvinh24.ezquiz.network;

import dev.vtvinh24.ezquiz.data.model.AuthResponse;
import dev.vtvinh24.ezquiz.data.model.LoginRequest;
import dev.vtvinh24.ezquiz.data.model.RegisterRequest;
import dev.vtvinh24.ezquiz.data.model.RedeemCodeRequest;
import dev.vtvinh24.ezquiz.data.model.RedeemCodeResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface AuthApiService {
    @POST("auth/login")
    Call<AuthResponse> login(@Body LoginRequest loginRequest);

    @POST("auth/register")
    Call<AuthResponse> register(@Body RegisterRequest registerRequest);

    @POST("auth/logout")
    Call<Void> logout();

    @GET("me")
    Call<AuthResponse> getCurrentUser();

    @POST("redeem-code")
    Call<RedeemCodeResponse> redeemCode(@Body RedeemCodeRequest redeemCodeRequest);

    @GET("verify-token")
    Call<Void> verifyToken();
}
