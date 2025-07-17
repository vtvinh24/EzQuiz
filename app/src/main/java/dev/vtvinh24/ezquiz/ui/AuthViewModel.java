package dev.vtvinh24.ezquiz.ui;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import dev.vtvinh24.ezquiz.data.repo.UserRepository;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.UserEntity;
import dev.vtvinh24.ezquiz.network.ApiClient;
import dev.vtvinh24.ezquiz.network.api.QuizApiService;
import dev.vtvinh24.ezquiz.network.api.model.AuthResponse;
import dev.vtvinh24.ezquiz.network.api.model.LoginRequest;
import dev.vtvinh24.ezquiz.network.api.model.RegisterRequest;
import dev.vtvinh24.ezquiz.network.api.model.UserProfileResponse;
import dev.vtvinh24.ezquiz.network.api.model.TokenValidationResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final QuizApiService apiService;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.userRepository = new UserRepository(application);
        this.apiService = ApiClient.getQuizApiService();
    }

    public LiveData<AuthResponse.UserData> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return userRepository.getIsLoggedIn();
    }

    public boolean isUserLoggedIn() {
        return userRepository.isUserLoggedIn();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void login(String email, String password) {
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            errorMessage.setValue("Email và mật khẩu không được để trống");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Vui lòng nhập email hợp lệ");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        LoginRequest request = new LoginRequest(email, password);
        Call<AuthResponse> call = apiService.login(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    // Lưu token và thông tin user vào SharedPreferences
                    userRepository.saveUserData(authResponse.getToken(), authResponse.getUser());

                    // Sync data với Room database để generate quiz có thể access
                    syncUserDataToDatabase(authResponse.getUser());

                    errorMessage.setValue(null);
                } else {
                    String error = "Đăng nhập thất bại";
                    if (response.code() == 401) {
                        error = "Email hoặc mật khẩu không đúng";
                    } else if (response.code() >= 500) {
                        error = "Lỗi server, vui lòng thử lại sau";
                    }
                    errorMessage.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);

                String errorMsg;
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMsg = "Kết nối quá chậm, vui lòng kiểm tra mạng và thử lại";
                } else if (t instanceof java.net.UnknownHostException) {
                    errorMsg = "Không thể kết nối đến server, vui lòng kiểm tra kết nối mạng";
                } else if (t instanceof java.net.ConnectException) {
                    errorMsg = "Không thể kết nối đến server, vui lòng thử lại sau";
                } else {
                    errorMsg = "Lỗi kết nối: " + t.getMessage();
                }

                errorMessage.setValue(errorMsg);
            }
        });
    }

    private void syncUserDataToDatabase(AuthResponse.UserData userData) {
        new Thread(() -> {
            AppDatabase database = AppDatabaseProvider.getDatabase(getApplication());

            // Logout all existing users first
            database.userDao().logoutAllUsers();

            // Create UserEntity với constructor đầy đủ tham số
            UserEntity userEntity = new UserEntity(
                userData.getId(),
                userData.getEmail(),
                userData.getName(),
                null, // token không cần lưu trong Room database
                userData.isPremium(),
                userData.getPremiumExpiryDate(),
                System.currentTimeMillis(), // createdAt
                System.currentTimeMillis(), // lastLoginAt
                true // isLoggedIn
            );

            // Insert into database
            database.userDao().insert(userEntity);

            android.util.Log.d("AuthViewModel", "User data synced to database: " + userData.getEmail() + ", isPremium: " + userData.isPremium());
        }).start();
    }

    public void register(String email, String name, String password, String confirmPassword) {
        if (email.trim().isEmpty() || name.trim().isEmpty() || password.trim().isEmpty()) {
            errorMessage.setValue("Tất cả các trường không được để trống");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            errorMessage.setValue("Vui lòng nhập email hợp lệ");
            return;
        }

        if (password.length() < 6) {
            errorMessage.setValue("Mật khẩu phải có ít nhất 6 ký tự");
            return;
        }

        if (!password.equals(confirmPassword)) {
            errorMessage.setValue("Mật khẩu xác nhận không khớp");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        RegisterRequest request = new RegisterRequest(email, password, name);
        Call<AuthResponse> call = apiService.register(request);

        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                isLoading.setValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    // Lưu token và thông tin user vào SharedPreferences
                    userRepository.saveUserData(authResponse.getToken(), authResponse.getUser());

                    // Sync data với Room database để generate quiz có thể access
                    syncUserDataToDatabase(authResponse.getUser());

                    errorMessage.setValue(null);
                } else {
                    String error = "Đăng ký thất bại";
                    if (response.code() == 409) {
                        error = "Email đã được sử dụng";
                    } else if (response.code() >= 500) {
                        error = "Lỗi server, vui lòng thử lại sau";
                    }
                    errorMessage.setValue(error);
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                isLoading.setValue(false);
                errorMessage.setValue("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    public void logout() {
        // Logout từ SharedPreferences
        userRepository.logout();

        // Logout từ Room database
        new Thread(() -> {
            AppDatabase database = AppDatabaseProvider.getDatabase(getApplication());
            database.userDao().logoutAllUsers();
            android.util.Log.d("AuthViewModel", "User logged out from database");
        }).start();
    }

    public void verifyToken() {
        String authHeader = userRepository.getAuthHeader();
        if (authHeader == null) {
            logout();
            return;
        }

        Call<TokenValidationResponse> call = apiService.verifyToken(authHeader);
        call.enqueue(new Callback<TokenValidationResponse>() {
            @Override
            public void onResponse(Call<TokenValidationResponse> call, Response<TokenValidationResponse> response) {
                if (!response.isSuccessful() || response.body() == null || !response.body().isValid()) {
                    logout();
                }
            }

            @Override
            public void onFailure(Call<TokenValidationResponse> call, Throwable t) {
                // Không logout khi có lỗi mạng, giữ user đăng nhập để dùng offline
            }
        });
    }

    public void refreshUserProfile() {
        String authHeader = userRepository.getAuthHeader();
        if (authHeader == null) return;

        Call<UserProfileResponse> call = apiService.getUserProfile(authHeader);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userRepository.updateUserData(response.body().getUser());
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                // Không cần thông báo lỗi khi refresh profile thất bại
            }
        });
    }

    // Redeem gift code functionality
    public void redeemGiftCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            errorMessage.setValue("Vui lòng nhập mã gift code");
            return;
        }

        String authHeader = userRepository.getAuthHeader();
        if (authHeader == null) {
            errorMessage.setValue("Cần đăng nhập để sử dụng gift code");
            return;
        }

        isLoading.setValue(true);
        errorMessage.setValue(null);

        // TODO: Implement RedeemCodeRequest and RedeemCodeResponse if needed
        // For now, just show a placeholder response
        isLoading.setValue(false);
        errorMessage.setValue("Tính năng redeem code đang được phát triển");
    }
}
