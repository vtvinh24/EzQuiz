package dev.vtvinh24.ezquiz.data.model; // <<< SỬA LẠI PACKAGE CHO ĐÚNG

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final Map<String, Retrofit> retrofitMap = new ConcurrentHashMap<>();

    // Đặt timeout dài hơn cho các request AI vì có thể mất thời gian xử lý
    private static final OkHttpClient aiHttpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    public static Retrofit getInstance(String baseUrl, OkHttpClient client) {
        Retrofit retrofit = retrofitMap.get(baseUrl);
        if (retrofit == null) {
            Retrofit.Builder builder = new Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .addConverterFactory(GsonConverterFactory.create());
            if (client != null) {
                builder.client(client);
            }
            retrofit = builder.build();
            retrofitMap.put(baseUrl, retrofit);
        }
        return retrofit;
    }

    public static <T> T createService(String baseUrl, Class<T> serviceClass, OkHttpClient client) {
        return getInstance(baseUrl, client).create(serviceClass);
    }

    public static final String BASE_URL_PASTE_SERVICE = "https://paste.rs/";

    // URL server Render của bạn, kết thúc bằng dấu "/"
    public static final String BASE_URL_AI_SERVICE = "https://server-horusoul.onrender.com/";

    public static <T> T getPasteService(Class<T> serviceClass) {
        return createService(BASE_URL_PASTE_SERVICE, serviceClass, null);
    }

    // Phương thức này giờ đã đúng và sẽ được tìm thấy
    public static <T> T getAIService(Class<T> serviceClass) {
        return createService(BASE_URL_AI_SERVICE, serviceClass, aiHttpClient);
    }
}