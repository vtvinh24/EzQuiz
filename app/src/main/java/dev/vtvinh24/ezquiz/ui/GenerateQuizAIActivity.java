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
import com.google.android.material.textview.MaterialTextView;
import com.google.gson.Gson;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.AIService;
import dev.vtvinh24.ezquiz.data.model.RetrofitClient;
import dev.vtvinh24.ezquiz.data.model.GenerateQuizRequest;
import dev.vtvinh24.ezquiz.data.model.GenerateQuizResponse;
import dev.vtvinh24.ezquiz.data.model.GeneratedQuizItem;
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
      // Trong file GenerateQuizAIActivity.java
      @Override
      public void onResponse(Call<GenerateQuizResponse> call, Response<GenerateQuizResponse> response) {
        showLoading(false);
        if (response.isSuccessful() && response.body() != null) {
          // << KIỂM TRA NULL CHẶT CHẼ HƠN >>
          GenerateQuizResponse body = response.body();
          List<GeneratedQuizItem> quizzes = body.getQuizzes();

          if (quizzes != null && !quizzes.isEmpty()) {
            // Mọi thứ OK, tiếp tục
            Intent intent = new Intent(GenerateQuizAIActivity.this, ReviewGeneratedQuizActivity.class);
            String quizzesJson = new Gson().toJson(quizzes);
            Log.d(TAG, "Success. Sending JSON to ReviewActivity: " + quizzesJson);
            intent.putExtra(ReviewGeneratedQuizActivity.EXTRA_GENERATED_QUIZZES, quizzesJson);
            startActivity(intent);
          } else {
            // Trường hợp response body OK nhưng không có quizzes nào được parse
            Log.e(TAG, "Response successful but quizzes list is null or empty. Backend might not be returning the correct JSON format.");
            textAiStatus.setText("AI generated an invalid response. Please try a different prompt.");
          }
        } else {
          // Log lỗi để dễ debug
          String errorBody = "";
          try {
            if (response.errorBody() != null) {
              errorBody = response.errorBody().string();
            }
          } catch (Exception e) {
            Log.e(TAG, "Error parsing error body", e);
          }
          Log.e(TAG, "API call failed. Code: " + response.code() + " | Message: " + response.message() + " | Error Body: " + errorBody);
          textAiStatus.setText("Error: " + response.code() + ". The server responded with an error.");
        }
      }

      @Override
      public void onFailure(Call<GenerateQuizResponse> call, Throwable t) {
        showLoading(false);
        textAiStatus.setText("Network Failure: " + t.getMessage());
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