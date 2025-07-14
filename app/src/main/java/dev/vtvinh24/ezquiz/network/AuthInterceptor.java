package dev.vtvinh24.ezquiz.network;

import android.content.Context;
import androidx.annotation.NonNull;
import dev.vtvinh24.ezquiz.util.SessionManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;

public class AuthInterceptor implements Interceptor {
    private final SessionManager sessionManager;

    public AuthInterceptor(Context context) {
        this.sessionManager = new SessionManager(context);
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request originalRequest = chain.request();

        String token = sessionManager.getUserToken();
        if (token != null && !token.isEmpty()) {
            Request newRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .build();
            return chain.proceed(newRequest);
        }

        Request newRequest = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .build();
        return chain.proceed(newRequest);
    }
}
