package dev.vtvinh24.ezquiz.network;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
  private static final Map<String, Retrofit> retrofitMap = new ConcurrentHashMap<>();

  public static Retrofit getInstance(String baseUrl) {
    Retrofit retrofit = retrofitMap.get(baseUrl);
    if (retrofit == null) {
      retrofit = new Retrofit.Builder()
              .baseUrl(baseUrl)
              .addConverterFactory(GsonConverterFactory.create())
              .build();
      retrofitMap.put(baseUrl, retrofit);
    }
    return retrofit;
  }

  public static <T> T createService(String baseUrl, Class<T> serviceClass) {
    return getInstance(baseUrl).create(serviceClass);
  }

  public static final String BASE_URL_PASTE_SERVICE = "https://paste.rs/";
  public static final String BASE_URL_LLM_SERVICE = "https://gemini.googleapis.com/";

  public static <T> T getPasteService(Class<T> serviceClass) {
    return createService(BASE_URL_PASTE_SERVICE, serviceClass);
  }

  public static <T> T getLLMService(Class<T> serviceClass) {
    return createService(BASE_URL_LLM_SERVICE, serviceClass);
  }
}
