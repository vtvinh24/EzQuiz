package dev.vtvinh24.ezquiz.ui.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.repo.UserRepository;
import dev.vtvinh24.ezquiz.data.repo.QuizSyncRepository;
import dev.vtvinh24.ezquiz.network.api.QuizApiService;
import dev.vtvinh24.ezquiz.network.api.model.AuthResponse;
import dev.vtvinh24.ezquiz.network.ApiClient;

public class QuizSyncViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final QuizSyncRepository quizSyncRepository;
    private final QuizApiService apiService;

    public QuizSyncViewModel(@NonNull Application application) {
        super(application);

        // Khởi tạo các repository
        userRepository = new UserRepository(application);
        apiService = ApiClient.getQuizApiService();
        quizSyncRepository = new QuizSyncRepository(application, apiService, userRepository);
    }

    // User management
    public LiveData<AuthResponse.UserData> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public LiveData<Boolean> getIsLoggedIn() {
        return userRepository.getIsLoggedIn();
    }

    public boolean isUserLoggedIn() {
        return userRepository.isUserLoggedIn();
    }

    public void logout() {
        userRepository.logout();
        // Có thể thêm logic xóa quiz data khi logout nếu cần
    }

    // Quiz sync management
    public LiveData<Boolean> getIsSyncing() {
        return quizSyncRepository.getIsSyncing();
    }

    public LiveData<String> getSyncStatus() {
        return quizSyncRepository.getSyncStatus();
    }

    public LiveData<List<QuizSetEntity>> getUserQuizSets() {
        return quizSyncRepository.getUserQuizSets();
    }

    public void syncQuizzesFromServer() {
        quizSyncRepository.syncQuizzesFromServer();
    }

    public void refreshUserQuizSets() {
        quizSyncRepository.refreshUserQuizSets();
    }

    // Kiểm tra xem có quiz nào cần sync không
    public boolean hasPendingSync() {
        // Logic để kiểm tra xem có quiz nào cần upload lên server không
        return false; // Placeholder
    }

    // Auto sync khi app khởi động (nếu user đã login)
    public void performAutoSync() {
        if (isUserLoggedIn()) {
            long lastSync = userRepository.getLastSyncTime();
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastSync;

            // Auto sync nếu đã quá 1 giờ kể từ lần sync cuối
            if (timeDiff > 3600000) { // 1 hour in milliseconds
                syncQuizzesFromServer();
            } else {
                // Chỉ refresh local data
                refreshUserQuizSets();
            }
        }
    }
}
