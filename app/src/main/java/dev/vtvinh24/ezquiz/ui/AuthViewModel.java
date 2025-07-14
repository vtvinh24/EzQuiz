package dev.vtvinh24.ezquiz.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.UserEntity;
import dev.vtvinh24.ezquiz.data.repo.AuthRepository;

public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);

    public AuthViewModel(@NonNull Application application) {
        super(application);
        this.authRepository = new AuthRepository(
            AppDatabaseProvider.getDatabase(application).userDao(),
            application.getApplicationContext()
        );
    }

    public LiveData<AuthRepository.AuthResult> login(String email, String password) {
        if (email.trim().isEmpty() || password.trim().isEmpty()) {
            MutableLiveData<AuthRepository.AuthResult> result = new MutableLiveData<>();
            result.setValue(new AuthRepository.AuthResult(false, "Email and password are required", null));
            return result;
        }

        isLoading.setValue(true);
        LiveData<AuthRepository.AuthResult> result = authRepository.login(email, password);
        result.observeForever(authResult -> isLoading.setValue(false));
        return result;
    }

    public LiveData<AuthRepository.AuthResult> register(String email, String name, String password, String confirmPassword) {
        if (email.trim().isEmpty() || name.trim().isEmpty() || password.trim().isEmpty()) {
            MutableLiveData<AuthRepository.AuthResult> result = new MutableLiveData<>();
            result.setValue(new AuthRepository.AuthResult(false, "All fields are required", null));
            return result;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            MutableLiveData<AuthRepository.AuthResult> result = new MutableLiveData<>();
            result.setValue(new AuthRepository.AuthResult(false, "Please enter a valid email", null));
            return result;
        }

        if (password.length() < 6) {
            MutableLiveData<AuthRepository.AuthResult> result = new MutableLiveData<>();
            result.setValue(new AuthRepository.AuthResult(false, "Password must be at least 6 characters", null));
            return result;
        }

        isLoading.setValue(true);
        LiveData<AuthRepository.AuthResult> result = authRepository.register(email, name, password, confirmPassword);
        result.observeForever(authResult -> isLoading.setValue(false));
        return result;
    }

    public LiveData<AuthRepository.RedeemResult> redeemGiftCode(String code) {
        if (code.trim().isEmpty()) {
            MutableLiveData<AuthRepository.RedeemResult> result = new MutableLiveData<>();
            result.setValue(new AuthRepository.RedeemResult(false, "Gift code is required", null));
            return result;
        }

        isLoading.setValue(true);
        LiveData<AuthRepository.RedeemResult> result = authRepository.redeemGiftCode(code);
        result.observeForever(redeemResult -> isLoading.setValue(false));
        return result;
    }

    public void logout() {
        authRepository.logout();
    }

    public LiveData<UserEntity> getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public boolean isUserLoggedIn() {
        return authRepository.isUserLoggedIn();
    }
}
