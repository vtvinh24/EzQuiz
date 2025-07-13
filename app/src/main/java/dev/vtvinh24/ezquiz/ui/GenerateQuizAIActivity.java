package dev.vtvinh24.ezquiz.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.AIService;
import dev.vtvinh24.ezquiz.data.model.GenerateQuizResponse;
import dev.vtvinh24.ezquiz.data.model.GeneratedQuizItem;
import dev.vtvinh24.ezquiz.network.RetrofitClient;
import dev.vtvinh24.ezquiz.ui.adapter.TopicAdapter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateQuizAIActivity extends AppCompatActivity implements TopicAdapter.OnTopicClickListener {
    private static final String TAG = "GenerateQuizAIActivity";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private TextInputEditText editAiPrompt;
    private MaterialButton btnGenerateQuiz;
    private MaterialButton btnVoiceInput;
    private MaterialButton btnUploadImage;
    private MaterialButton btnRemoveImage;
    private CircularProgressIndicator progressBar;
    private TextView textAiStatus;
    private MaterialCardView layoutStatus;
    private MaterialToolbar toolbar;
    private RecyclerView recyclerTopics;
    private MaterialCardView cardImagePreview;
    private ImageView imagePreview;
    private TextView textImageName;

    private TopicAdapter topicAdapter;
    private File selectedImageFile;
    private boolean isVoiceInputActive = false;

    // Predefined topics for quick selection
    private final List<String> quickTopics = Arrays.asList(
            "Thể thao", "Khoa học", "Lịch sử", "Văn học", "Toán học",
            "Địa lý", "Tiếng Anh", "Công nghệ", "Âm nhạc", "Điện ảnh",
            "Thiên nhiên", "Y học", "Kinh tế", "Chính trị", "Nghệ thuật"
    );

    // Activity result launchers
    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri imageUri = result.getData().getData();
                    if (imageUri != null) {
                        handleSelectedImage(imageUri);
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> voiceInputLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> results = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (results != null && !results.isEmpty()) {
                        String spokenText = results.get(0);
                        handleVoiceInput(spokenText);
                    }
                }
                isVoiceInputActive = false;
                updateVoiceButtonState();
            }
    );

    private final ActivityResultLauncher<Intent> reviewQuizLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean shouldClearData = result.getData().getBooleanExtra(
                            ReviewGeneratedQuizActivity.EXTRA_CLEAR_GENERATE_DATA, false);
                    if (shouldClearData) {
                        clearGenerateData();
                        Toast.makeText(this, "Đã xóa dữ liệu tạo quiz", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_quiz_ai);

        initializeViews();
        setupToolbar();
        setupTopicSuggestions();
        setupClickListeners();
    }

    private void initializeViews() {
        editAiPrompt = findViewById(R.id.edit_ai_prompt);
        btnGenerateQuiz = findViewById(R.id.btn_generate_quiz);
        btnVoiceInput = findViewById(R.id.btn_voice_input);
        btnUploadImage = findViewById(R.id.btn_upload_image);
        btnRemoveImage = findViewById(R.id.btn_remove_image);
        progressBar = findViewById(R.id.progress_bar);
        textAiStatus = findViewById(R.id.text_ai_status);
        layoutStatus = findViewById(R.id.layout_status);
        toolbar = findViewById(R.id.topAppBar);
        recyclerTopics = findViewById(R.id.recycler_topics);
        cardImagePreview = findViewById(R.id.card_image_preview);
        imagePreview = findViewById(R.id.image_preview);
        textImageName = findViewById(R.id.text_image_name);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupTopicSuggestions() {
        topicAdapter = new TopicAdapter(quickTopics, this);
        recyclerTopics.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerTopics.setAdapter(topicAdapter);
    }

    private void setupClickListeners() {
        btnGenerateQuiz.setOnClickListener(v -> generateQuiz());
        btnVoiceInput.setOnClickListener(v -> startVoiceInput());
        btnUploadImage.setOnClickListener(v -> openImagePicker());
        btnRemoveImage.setOnClickListener(v -> removeSelectedImage());
    }

    @Override
    public void onTopicClick(String topic) {
        String currentText = editAiPrompt.getText() != null ? editAiPrompt.getText().toString().trim() : "";
        String newText = currentText.isEmpty() ?
            "Tạo câu hỏi trắc nghiệm về chủ đề " + topic :
            currentText + " về chủ đề " + topic;
        editAiPrompt.setText(newText);
        editAiPrompt.setSelection(newText.length());
    }

    private void startVoiceInput() {
        if (!checkAudioPermission()) {
            requestAudioPermission();
            return;
        }

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Thiết bị không hỗ trợ nhận dạng giọng nói", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Nói về chủ đề bạn muốn tạo quiz...");

        isVoiceInputActive = true;
        updateVoiceButtonState();
        voiceInputLauncher.launch(intent);
    }

    private void handleVoiceInput(String spokenText) {
        String currentText = editAiPrompt.getText() != null ? editAiPrompt.getText().toString().trim() : "";
        String newText = currentText.isEmpty() ? spokenText : currentText + " " + spokenText;
        editAiPrompt.setText(newText);
        editAiPrompt.setSelection(newText.length());

        Toast.makeText(this, "Đã thêm: " + spokenText, Toast.LENGTH_SHORT).show();
    }

    private void updateVoiceButtonState() {
        if (isVoiceInputActive) {
            btnVoiceInput.setText("Listening...");
            btnVoiceInput.setEnabled(false);
        } else {
            btnVoiceInput.setText("Voice");
            btnVoiceInput.setEnabled(true);
        }
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        imagePickerLauncher.launch(Intent.createChooser(intent, "Chọn hình ảnh"));
    }

    private void handleSelectedImage(Uri imageUri) {
        try {
            selectedImageFile = createFileFromUri(imageUri);
            if (selectedImageFile != null) {
                // Display image preview
                Glide.with(this)
                        .load(imageUri)
                        .centerCrop()
                        .into(imagePreview);

                textImageName.setText(selectedImageFile.getName());
                cardImagePreview.setVisibility(View.VISIBLE);

                Toast.makeText(this, "Đã chọn hình ảnh: " + selectedImageFile.getName(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error handling selected image", e);
            Toast.makeText(this, "Lỗi khi xử lý hình ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeSelectedImage() {
        selectedImageFile = null;
        cardImagePreview.setVisibility(View.GONE);
        imagePreview.setImageDrawable(null);
        textImageName.setText("");
    }

    private String getMimeTypeFromUri(Uri uri) {
        String mimeType = getContentResolver().getType(uri);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            String extension = "";
            String path = uri.getPath();
            if (path != null) {
                int i = path.lastIndexOf('.');
                if (i > 0) {
                    extension = path.substring(i + 1).toLowerCase();
                }
            }

            switch (extension) {
                case "jpg":
                case "jpeg":
                    return "image/jpeg";
                case "png":
                    return "image/png";
                case "webp":
                    return "image/webp";
                case "gif":
                    return "image/gif";
                default:
                    return "image/jpeg"; // Default fallback
            }
        }
        return mimeType;
    }

    private File createFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);
        if (inputStream == null) return null;

        String mimeType = getMimeTypeFromUri(uri);
        String extension = ".jpg";
        if (mimeType.equals("image/png")) {
            extension = ".png";
        } else if (mimeType.equals("image/webp")) {
            extension = ".webp";
        } else if (mimeType.equals("image/gif")) {
            extension = ".gif";
        }

        String fileName = "quiz_image_" + System.currentTimeMillis() + extension;
        File file = new File(getCacheDir(), fileName);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } finally {
            inputStream.close();
        }

        return file;
    }

    private void generateQuiz() {
        String prompt = editAiPrompt.getText() != null ? editAiPrompt.getText().toString().trim() : "";

        if (prompt.isEmpty() && selectedImageFile == null) {
            Toast.makeText(this, "Vui lòng nhập nội dung hoặc chọn hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading(true);

        AIService aiService = RetrofitClient.getAIService(AIService.class);

        try {
            if (selectedImageFile != null) {
                // Determine proper MIME type based on file extension
                String mimeType = "image/jpeg"; // Default
                String fileName = selectedImageFile.getName().toLowerCase();
                if (fileName.endsWith(".png")) {
                    mimeType = "image/png";
                } else if (fileName.endsWith(".webp")) {
                    mimeType = "image/webp";
                } else if (fileName.endsWith(".gif")) {
                    mimeType = "image/gif";
                }

                RequestBody promptBody = RequestBody.create(MediaType.parse("text/plain"), prompt);
                RequestBody imageBody = RequestBody.create(MediaType.parse(mimeType), selectedImageFile);
                MultipartBody.Part imagePart = MultipartBody.Part.createFormData("file", selectedImageFile.getName(), imageBody);

                aiService.generateQuiz(promptBody, imagePart).enqueue(createQuizCallback());
            } else {
                // Generate quiz with text only
                RequestBody promptBody = RequestBody.create(MediaType.parse("text/plain"), prompt);
                aiService.generateQuizTextOnly(promptBody).enqueue(createQuizCallback());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            showLoading(false);
            Toast.makeText(this, "Lỗi khi tạo yêu cầu", Toast.LENGTH_SHORT).show();
        }
    }

    private Callback<GenerateQuizResponse> createQuizCallback() {
        return new Callback<GenerateQuizResponse>() {
            @Override
            public void onResponse(Call<GenerateQuizResponse> call, Response<GenerateQuizResponse> response) {
                showLoading(false);
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        GenerateQuizResponse body = response.body();
                        List<GeneratedQuizItem> quizzes = body.getQuizzes();

                        if (quizzes != null && !quizzes.isEmpty()) {
                            Intent intent = new Intent(GenerateQuizAIActivity.this, ReviewGeneratedQuizActivity.class);
                            String quizzesJson = new Gson().toJson(quizzes);
                            Log.d(TAG, "Success. Sending JSON to ReviewActivity: " + quizzesJson);
                            intent.putExtra(ReviewGeneratedQuizActivity.EXTRA_GENERATED_QUIZZES, quizzesJson);
                            reviewQuizLauncher.launch(intent);
                        } else {
                            Log.e(TAG, "Response successful but quizzes list is null or empty");
                            Toast.makeText(GenerateQuizAIActivity.this,
                                    "Không thể tạo câu hỏi. Vui lòng thử lại.", Toast.LENGTH_SHORT).show();
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
                                "Lỗi từ server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response", e);
                    Toast.makeText(GenerateQuizAIActivity.this,
                            "Lỗi khi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenerateQuizResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network call failed", t);
                Toast.makeText(GenerateQuizAIActivity.this,
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            layoutStatus.setVisibility(View.VISIBLE);
            btnGenerateQuiz.setEnabled(false);
            textAiStatus.setText("Đang tạo câu hỏi...");
        } else {
            layoutStatus.setVisibility(View.GONE);
            btnGenerateQuiz.setEnabled(true);
        }
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput();
            } else {
                Toast.makeText(this, "Cần quyền ghi âm để sử dụng tính năng giọng nói", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearGenerateData() {
        editAiPrompt.setText("");
        removeSelectedImage();
    }
}