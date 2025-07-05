package dev.vtvinh24.ezquiz.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
  public static final String BASE_URL_PASTE_SERVICE = "https://paste.rs/";
  public static final String BASE_URL_LLM_SERVICE = "https://gemini.googleapis.com/";
  public static final String BASE_URL_AI_SERVICE = "https://server-horusoul.onrender.com/";
  private static final Map<String, Retrofit> retrofitMap = new ConcurrentHashMap<>();

  private static OkHttpClient createDefaultClient() {
    return new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
  }

  private static OkHttpClient createAIClient() {
    return new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(180, TimeUnit.SECONDS)
            .writeTimeout(180, TimeUnit.SECONDS)
            .build();
  }

  public static Retrofit getInstance(String baseUrl) {
    Retrofit retrofit = retrofitMap.get(baseUrl);
    if (retrofit == null) {
      OkHttpClient client = baseUrl.equals(BASE_URL_AI_SERVICE) ?
              createAIClient() : createDefaultClient();

      retrofit = new Retrofit.Builder()
              .baseUrl(baseUrl)
              .client(client)
              .addConverterFactory(GsonConverterFactory.create())
              .build();
      retrofitMap.put(baseUrl, retrofit);
    }
    return retrofit;
  }

  public static <T> T createService(String baseUrl, Class<T> serviceClass) {
    return getInstance(baseUrl).create(serviceClass);
  }

  public static <T> T getPasteService(Class<T> serviceClass) {
    return createService(BASE_URL_PASTE_SERVICE, serviceClass);
  }

  public static <T> T getLLMService(Class<T> serviceClass) {
    return createService(BASE_URL_LLM_SERVICE, serviceClass);
  }

  public static <T> T getAIService(Class<T> serviceClass) {
    return createService(BASE_URL_AI_SERVICE, serviceClass);
  }
}
