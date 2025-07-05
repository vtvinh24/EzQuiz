package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.GenerateQuizRequest;
import dev.vtvinh24.ezquiz.data.model.GenerateQuizResponse;
import dev.vtvinh24.ezquiz.data.model.GeneratedQuizItem;
import dev.vtvinh24.ezquiz.network.AIService;
import dev.vtvinh24.ezquiz.network.RetrofitClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateQuizAIActivity extends AppCompatActivity {
  private static final String TAG = "GenerateQuizAIActivity";

  private TextInputEditText editAiPrompt;
  private Button btnGenerateQuiz;
  private ProgressBar progressBar;
  private TextView textAiStatus;
  private MaterialToolbar toolbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_generate_quiz_ai);

    // Initialize views
    editAiPrompt = findViewById(R.id.edit_ai_prompt);
    btnGenerateQuiz = findViewById(R.id.btn_generate_quiz);
    progressBar = findViewById(R.id.progress_bar);
    textAiStatus = findViewById(R.id.text_ai_status);
    toolbar = findViewById(R.id.topAppBar);

    // Setup toolbar
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    toolbar.setNavigationOnClickListener(v -> finish());

    btnGenerateQuiz.setOnClickListener(v -> generateQuiz());
  }

  private void generateQuiz() {
    String prompt = editAiPrompt.getText().toString().trim();
    if (prompt.isEmpty()) {
      Toast.makeText(this, "Please enter a prompt.", Toast.LENGTH_SHORT).show();
      return;
    }

    showLoading(true);

    AIService aiService = RetrofitClient.getAIService(AIService.class);
    GenerateQuizRequest request = new GenerateQuizRequest(prompt);

    aiService.generateQuiz(request).enqueue(new Callback<GenerateQuizResponse>() {
      @Override
      public void onResponse(Call<GenerateQuizResponse> call, Response<GenerateQuizResponse> response) {
        showLoading(false);
        try {
          if (response.isSuccessful() && response.body() != null) {
            GenerateQuizResponse body = response.body();
            List<GeneratedQuizItem> quizzes = body.getQuizzes();

            if (quizzes != null && !quizzes.isEmpty()) {
              // Chuyển sang màn review với flags phù hợp
              Intent intent = new Intent(GenerateQuizAIActivity.this, ReviewGeneratedQuizActivity.class);
              intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              String quizzesJson = new Gson().toJson(quizzes);
              Log.d(TAG, "Success. Sending JSON to ReviewActivity: " + quizzesJson);
              intent.putExtra(ReviewGeneratedQuizActivity.EXTRA_GENERATED_QUIZZES, quizzesJson);
              startActivity(intent);
            } else {
              Log.e(TAG, "Response successful but quizzes list is null or empty");
              Toast.makeText(GenerateQuizAIActivity.this,
                      "Could not generate quiz. Please try again.",
                      Toast.LENGTH_SHORT).show();
            }
          } else {
            String errorBody = "";
            try {
              if (response.errorBody() != null) {
                errorBody = response.errorBody().string();
              }
            } catch (Exception e) {
              Log.e(TAG, "Error parsing error body", e);
            }
            Log.e(TAG, "API call failed. Code: " + response.code()
                    + " | Message: " + response.message()
                    + " | Error Body: " + errorBody);

            Toast.makeText(GenerateQuizAIActivity.this,
                    "Server error: " + response.code(),
                    Toast.LENGTH_SHORT).show();
          }
        } catch (Exception e) {
          Log.e(TAG, "Error processing response", e);
          Toast.makeText(GenerateQuizAIActivity.this,
                  "Error processing server response",
                  Toast.LENGTH_SHORT).show();
        }
      }

      @Override
      public void onFailure(Call<GenerateQuizResponse> call, Throwable t) {
        showLoading(false);
        Log.e(TAG, "Network call failed", t);
        Toast.makeText(GenerateQuizAIActivity.this,
                "Network error: " + t.getMessage(),
                Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void showLoading(boolean isLoading) {
    if (isLoading) {
      progressBar.setVisibility(View.VISIBLE);
      btnGenerateQuiz.setEnabled(false);
      textAiStatus.setText("Generating... Please wait.");
    } else {
      progressBar.setVisibility(View.GONE);
      btnGenerateQuiz.setEnabled(true);
      textAiStatus.setText("");
    }
  }
}