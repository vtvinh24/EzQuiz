package dev.vtvinh24.ezquiz.network;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;

import dev.vtvinh24.ezquiz.network.api.QuizApiService;

public class ApiClient {
    // Sử dụng localhost với port 3000 cho development
    // Bạn có thể thay đổi thành IP server thực tế khi deploy
    private static final String BASE_URL = "https://server-horusoul.onrender.com/"; // Android emulator localhost
    // private static final String BASE_URL = "http://192.168.1.100:3000/api/"; // Thay bằng IP thực tế của máy bạn
    // private static final String BASE_URL = "https://your-production-server.com/api/"; // Production server

    private static Retrofit retrofit = null;
    private static QuizApiService quizApiService = null;

    public static Retrofit getClient() {
        if (retrofit == null) {
            // Tạo HTTP logging interceptor để debug
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Tạo OkHttpClient với timeout được tối ưu và retry logic
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .connectTimeout(15, TimeUnit.SECONDS)  // Giảm từ 30s xuống 15s
                    .readTimeout(20, TimeUnit.SECONDS)     // Giảm từ 30s xuống 20s
                    .writeTimeout(15, TimeUnit.SECONDS)    // Giảm từ 30s xuống 15s
                    .retryOnConnectionFailure(true)        // Thêm retry logic
                    .build();

            // Tạo Retrofit instance
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static QuizApiService getQuizApiService() {
        if (quizApiService == null) {
            quizApiService = getClient().create(QuizApiService.class);
        }
        return quizApiService;
    }

    // Method để thay đổi base URL nếu cần
    public static void setBaseUrl(String newBaseUrl) {
        retrofit = null;
        quizApiService = null;
        // Recreate với URL mới sẽ được tạo ở lần gọi tiếp theo
    }
}
