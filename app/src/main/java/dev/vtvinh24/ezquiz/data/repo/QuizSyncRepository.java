package dev.vtvinh24.ezquiz.data.repo;

import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.dao.QuizDao;
import dev.vtvinh24.ezquiz.data.dao.QuizSetDao;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.network.api.QuizApiService;
import dev.vtvinh24.ezquiz.network.api.model.QuizQuestion;
import dev.vtvinh24.ezquiz.network.api.model.QuizListResponse;
import dev.vtvinh24.ezquiz.network.api.model.ServerQuiz;
import dev.vtvinh24.ezquiz.network.api.model.SaveQuizRequest;
import dev.vtvinh24.ezquiz.network.api.model.QuizSaveResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuizSyncRepository {
    private final QuizApiService apiService;
    private final UserRepository userRepository;
    private final QuizDao quizDao;
    private final QuizSetDao quizSetDao;
    private final ExecutorService executor;

    private final MutableLiveData<Boolean> isSyncing = new MutableLiveData<>(false);
    private final MutableLiveData<String> syncStatus = new MutableLiveData<>();
    private final MutableLiveData<List<QuizSetEntity>> userQuizSets = new MutableLiveData<>();

    public QuizSyncRepository(Context context, QuizApiService apiService, UserRepository userRepository) {
        this.apiService = apiService;
        this.userRepository = userRepository;
        this.executor = Executors.newSingleThreadExecutor();

        // Sử dụng AppDatabaseProvider thay vì AppDatabase.getDatabase()
        var db = AppDatabaseProvider.getDatabase(context);
        this.quizDao = db.quizDao();
        this.quizSetDao = db.quizSetDao();

        loadUserQuizSets();
    }

    public LiveData<Boolean> getIsSyncing() {
        return isSyncing;
    }

    public LiveData<String> getSyncStatus() {
        return syncStatus;
    }

    public LiveData<List<QuizSetEntity>> getUserQuizSets() {
        return userQuizSets;
    }

    public void syncQuizzesFromServer() {
        if (!userRepository.isUserLoggedIn()) {
            syncStatus.setValue("Cần đăng nhập để đồng bộ quiz");
            return;
        }

        String authHeader = userRepository.getAuthHeader();
        if (authHeader == null) {
            syncStatus.setValue("Token không hợp lệ");
            return;
        }

        isSyncing.setValue(true);
        syncStatus.setValue("Đang đồng bộ quiz từ server...");

        syncAllUserQuizzes(authHeader, 1);
    }

    private void syncAllUserQuizzes(String authHeader, int page) {
        Call<QuizListResponse> call = apiService.getUserQuizzes(authHeader, page, 50);

        call.enqueue(new Callback<QuizListResponse>() {
            @Override
            public void onResponse(Call<QuizListResponse> call, Response<QuizListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    QuizListResponse quizResponse = response.body();
                    List<ServerQuiz> serverQuizzes = quizResponse.getQuizzes();

                    executor.execute(() -> {
                        try {
                            String currentUserId = userRepository.getCurrentUserId();
                            int savedCount = 0;

                            for (ServerQuiz serverQuiz : serverQuizzes) {
                                if (saveServerQuizToLocal(serverQuiz, currentUserId)) {
                                    savedCount++;
                                }
                            }

                            syncStatus.postValue(String.format("Đã lưu %d quiz từ trang %d",
                                    savedCount, page));

                            // Kiểm tra xem có trang tiếp theo không
                            QuizListResponse.Pagination pagination = quizResponse.getPagination();
                            if (page < pagination.getPages()) {
                                syncAllUserQuizzes(authHeader, page + 1);
                            } else {
                                // Hoàn thành đồng bộ
                                userRepository.setLastSyncTime(System.currentTimeMillis());
                                isSyncing.postValue(false);
                                syncStatus.postValue(String.format("Đồng bộ hoàn tất. Tổng cộng %d quiz",
                                        pagination.getTotal()));
                                loadUserQuizSets();
                            }

                        } catch (Exception e) {
                            syncStatus.postValue("Lỗi khi lưu quiz: " + e.getMessage());
                            isSyncing.postValue(false);
                        }
                    });
                } else {
                    syncStatus.setValue("Lỗi từ server: " + response.code());
                    isSyncing.setValue(false);
                }
            }

            @Override
            public void onFailure(Call<QuizListResponse> call, Throwable t) {
                syncStatus.setValue("Lỗi kết nối: " + t.getMessage());
                isSyncing.setValue(false);
            }
        });
    }

    private boolean saveServerQuizToLocal(ServerQuiz serverQuiz, String currentUserId) {
        try {
            // Kiểm tra xem quiz đã tồn tại chưa (dựa trên server ID và user ID)
            QuizSetEntity existingQuizSet = quizSetDao.getQuizSetByServerIdAndUserId(serverQuiz.getId(), currentUserId);

            QuizSetEntity quizSet;
            if (existingQuizSet != null) {
                // Cập nhật quiz set hiện có
                quizSet = existingQuizSet;
                quizSet.name = serverQuiz.getTitle();
                quizSet.description = serverQuiz.getDescription();
                quizSet.updatedAt = System.currentTimeMillis();

                // Xóa các quiz cũ - simplified approach
                // quizDao.deleteQuizzesBySetId(quizSet.id);
            } else {
                // Tạo quiz set mới
                quizSet = new QuizSetEntity();
                quizSet.name = serverQuiz.getTitle();
                quizSet.description = serverQuiz.getDescription();
                quizSet.userId = currentUserId;
                quizSet.serverId = serverQuiz.getId();
                quizSet.isFromServer = true;
                quizSet.updatedAt = System.currentTimeMillis();
                quizSet.createdAt = System.currentTimeMillis();

                long quizSetId = quizSetDao.insert(quizSet);
                quizSet.id = quizSetId;
            }

            // Đơn giản hóa: chỉ cập nhật metadata, không sync detailed questions
            quizSet.questionCount = serverQuiz.getQuizzes() != null ? serverQuiz.getQuizzes().size() : 0;
            quizSetDao.update(quizSet);

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void uploadQuizToServer(QuizSetEntity quizSet, List<Quiz> quizzes) {
        if (!userRepository.isUserLoggedIn()) {
            syncStatus.setValue("Cần đăng nhập để upload quiz");
            return;
        }

        String authHeader = userRepository.getAuthHeader();
        if (authHeader == null) {
            syncStatus.setValue("Token không hợp lệ");
            return;
        }

        executor.execute(() -> {
            try {
                // Simplified: create basic quiz without detailed conversion
                List<QuizQuestion> serverQuestions = new ArrayList<>();
                // TODO: Convert local quiz to server format when needed

                SaveQuizRequest request = new SaveQuizRequest(
                    quizSet.name,
                    quizSet.description != null ? quizSet.description : "",
                    serverQuestions,
                    false, // isPublic
                    new ArrayList<>() // tags
                );

                Call<QuizSaveResponse> call = apiService.saveQuiz(authHeader, request);

                call.enqueue(new Callback<QuizSaveResponse>() {
                    @Override
                    public void onResponse(Call<QuizSaveResponse> call, Response<QuizSaveResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            QuizSaveResponse saveResponse = response.body();

                            // Cập nhật quiz set với server ID
                            executor.execute(() -> {
                                quizSet.serverId = saveResponse.getQuiz().getId();
                                quizSet.isFromServer = true;
                                quizSet.needsUpload = false;
                                quizSetDao.update(quizSet);

                                syncStatus.postValue("Đã upload quiz: " + quizSet.name);
                            });
                        } else {
                            syncStatus.postValue("Lỗi upload quiz: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<QuizSaveResponse> call, Throwable t) {
                        syncStatus.postValue("Lỗi kết nối khi upload: " + t.getMessage());
                    }
                });

            } catch (Exception e) {
                syncStatus.postValue("Lỗi chuẩn bị upload: " + e.getMessage());
            }
        });
    }

    private void loadUserQuizSets() {
        executor.execute(() -> {
            try {
                String currentUserId = userRepository.getCurrentUserId();
                if (currentUserId != null) {
                    List<QuizSetEntity> quizSets = quizSetDao.getQuizSetsByUserId(currentUserId);
                    userQuizSets.postValue(quizSets);
                } else {
                    userQuizSets.postValue(new ArrayList<>());
                }
            } catch (Exception e) {
                e.printStackTrace();
                userQuizSets.postValue(new ArrayList<>());
            }
        });
    }

    public void refreshUserQuizSets() {
        loadUserQuizSets();
    }
}
