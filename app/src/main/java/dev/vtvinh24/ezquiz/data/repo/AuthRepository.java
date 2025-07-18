package dev.vtvinh24.ezquiz.data.repo;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.data.dao.UserDao;
import dev.vtvinh24.ezquiz.data.entity.UserEntity;
import dev.vtvinh24.ezquiz.data.model.AuthResponse;
import dev.vtvinh24.ezquiz.data.model.LoginRequest;
import dev.vtvinh24.ezquiz.data.model.RegisterRequest;
import dev.vtvinh24.ezquiz.data.model.RedeemCodeRequest;
import dev.vtvinh24.ezquiz.data.model.RedeemCodeResponse;
import dev.vtvinh24.ezquiz.network.AuthApiService;
import dev.vtvinh24.ezquiz.network.RetrofitClient;
import dev.vtvinh24.ezquiz.util.SessionManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {
    private static final String TAG = "AuthRepository";
    private final UserDao userDao;
    private final AuthApiService authApiService;
    private final SessionManager sessionManager;
    private final ExecutorService executor;

    public AuthRepository(UserDao userDao, Context context) {
        this.userDao = userDao;
        this.sessionManager = new SessionManager(context);
        this.authApiService = RetrofitClient.getAuthService(AuthApiService.class, context);
        this.executor = Executors.newFixedThreadPool(2);
    }

    public LiveData<UserEntity> getCurrentUser() {
        return userDao.getCurrentUser();
    }

    public LiveData<AuthResult> login(String email, String password) {
        MutableLiveData<AuthResult> result = new MutableLiveData<>();

        LoginRequest request = new LoginRequest(email, password);
        authApiService.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    saveUserToDatabase(authResponse, result);
                } else {
                    result.setValue(new AuthResult(false, "Login failed", null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "Login API call failed", t);
                String msg = t.getMessage() != null && t.getMessage().contains("Unable to resolve host")
                        ? "Server không phản hồi"
                        : "Network error: " + t.getMessage();
                result.setValue(new AuthResult(false, msg, null));
            }
        });

        return result;
    }

    public LiveData<AuthResult> register(String email, String name, String password, String confirmPassword) {
        MutableLiveData<AuthResult> result = new MutableLiveData<>();

        if (!password.equals(confirmPassword)) {
            result.setValue(new AuthResult(false, "Passwords do not match", null));
            return result;
        }

        RegisterRequest request = new RegisterRequest(email, name, password);
        authApiService.register(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    saveUserToDatabase(authResponse, result);
                } else {
                    result.setValue(new AuthResult(false, "Registration failed", null));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                Log.e(TAG, "Register API call failed", t);
                String msg = t.getMessage() != null && t.getMessage().contains("Unable to resolve host")
                        ? "Server không phản hồi"
                        : "Network error: " + t.getMessage();
                result.setValue(new AuthResult(false, msg, null));
            }
        });

        return result;
    }

    public LiveData<RedeemResult> redeemGiftCode(String code) {
        MutableLiveData<RedeemResult> result = new MutableLiveData<>();

        RedeemCodeRequest request = new RedeemCodeRequest(code);
        authApiService.redeemCode(request).enqueue(new Callback<RedeemCodeResponse>() {
            @Override
            public void onResponse(Call<RedeemCodeResponse> call, Response<RedeemCodeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    RedeemCodeResponse redeemResponse = response.body();

                    // Update user's premium status in database
                    executor.execute(() -> {
                        try {
                            UserEntity currentUser = userDao.getCurrentUserSync();
                            if (currentUser != null) {
                                currentUser.setPremium(redeemResponse.getPremium().isPremium());
                                currentUser.setPremiumExpiryDate(redeemResponse.getPremium().getPremiumExpiryDate());
                                userDao.update(currentUser);

                                // Update session
                                sessionManager.createSession(
                                        currentUser.getId(),
                                        currentUser.getToken(),
                                        currentUser.getEmail(),
                                        currentUser.getName()
                                );
                            }

                            result.postValue(new RedeemResult(true, redeemResponse.getMessage(), redeemResponse.getPremium()));
                        } catch (Exception e) {
                            Log.e(TAG, "Error updating premium status", e);
                            result.postValue(new RedeemResult(false, "Failed to update premium status", null));
                        }
                    });
                } else {
                    result.setValue(new RedeemResult(false, "Invalid gift code", null));
                }
            }

            @Override
            public void onFailure(Call<RedeemCodeResponse> call, Throwable t) {
                Log.e(TAG, "Redeem code API call failed", t);
                result.setValue(new RedeemResult(false, "Network error: " + t.getMessage(), null));
            }
        });

        return result;
    }

    private void saveUserToDatabase(AuthResponse authResponse, MutableLiveData<AuthResult> result) {
        executor.execute(() -> {
            try {
                userDao.logoutAllUsers();

                long currentTime = System.currentTimeMillis();
                UserEntity user = new UserEntity(
                        authResponse.getUser().getId(),
                        authResponse.getUser().getEmail(),
                        authResponse.getUser().getName(),
                        authResponse.getToken(),
                        authResponse.getUser().isPremium(),
                        authResponse.getUser().getPremiumExpiryDate(),
                        currentTime,
                        currentTime,
                        true
                );

                userDao.insert(user);

                // Save session data
                sessionManager.createSession(
                        user.getId(),
                        user.getToken(),
                        user.getEmail(),
                        user.getName()
                );

                result.postValue(new AuthResult(true, "Success", user));
            } catch (Exception e) {
                Log.e(TAG, "Error saving user to database", e);
                result.postValue(new AuthResult(false, "Database error", null));
            }
        });
    }

    public void logout() {
        executor.execute(() -> {
            userDao.logoutAllUsers();
            sessionManager.clearSession();
        });

        authApiService.logout().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                Log.d(TAG, "Logout successful");
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Logout API call failed", t);
            }
        });
    }

    public boolean isUserLoggedIn() {
        try {
            return sessionManager.isLoggedIn() && userDao.getLoggedInUsersCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Error checking login status", e);
            return sessionManager.isLoggedIn();
        }
    }

    public static class AuthResult {
        private final boolean success;
        private final String message;
        private final UserEntity user;

        public AuthResult(boolean success, String message, UserEntity user) {
            this.success = success;
            this.message = message;
            this.user = user;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public UserEntity getUser() { return user; }
    }

    public static class RedeemResult {
        private final boolean success;
        private final String message;
        private final RedeemCodeResponse.Premium premium;

        public RedeemResult(boolean success, String message, RedeemCodeResponse.Premium premium) {
            this.success = success;
            this.message = message;
            this.premium = premium;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public RedeemCodeResponse.Premium getPremium() { return premium; }
    }
}
