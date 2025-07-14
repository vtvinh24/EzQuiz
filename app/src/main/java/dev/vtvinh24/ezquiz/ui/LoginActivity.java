package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

import dev.vtvinh24.ezquiz.R;

public class LoginActivity extends AppCompatActivity {
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin, btnGoToRegister;
    private ProgressBar progressBar;
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Start animated gradient background
        ScrollView scrollView = findViewById(android.R.id.content).findViewById(android.R.id.content).getRootView().findViewById(R.id.scrollViewLogin);
        if (scrollView != null && scrollView.getBackground() instanceof AnimationDrawable) {
            AnimationDrawable animationDrawable = (AnimationDrawable) scrollView.getBackground();
            animationDrawable.setEnterFadeDuration(1000);
            animationDrawable.setExitFadeDuration(2000);
            animationDrawable.start();
        }

        initViews();
        initViewModel();
        setupClickListeners();
        observeViewModel();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        progressBar = findViewById(R.id.progressBar);
    }

    private void initViewModel() {
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> performLogin());
        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading) {
                progressBar.setVisibility(android.view.View.VISIBLE);
                btnLogin.setEnabled(false);
                btnGoToRegister.setEnabled(false);
            } else {
                progressBar.setVisibility(android.view.View.GONE);
                btnLogin.setEnabled(true);
                btnGoToRegister.setEnabled(true);
            }
        });
    }

    private void performLogin() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        authViewModel.login(email, password).observe(this, result -> {
            if (result.isSuccess()) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (authViewModel.isUserLoggedIn()) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
