package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import dev.vtvinh24.ezquiz.R;

public class SplashActivity extends AppCompatActivity {

  private static final int SPLASH_DELAY = 2000; // 2 seconds
  private AuthViewModel authViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_splash);

    authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

    new Handler(Looper.getMainLooper()).postDelayed(() -> {
      if (authViewModel.isUserLoggedIn()) {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
      } else {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
      }
      finish();
    }, SPLASH_DELAY);
  }
}
