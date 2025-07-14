package dev.vtvinh24.ezquiz.ui.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
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
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.UserEntity;
import dev.vtvinh24.ezquiz.data.model.AIService;
import dev.vtvinh24.ezquiz.data.model.GenerateQuizResponse;
import dev.vtvinh24.ezquiz.data.model.GeneratedQuizItem;
import dev.vtvinh24.ezquiz.network.RetrofitClient;
import dev.vtvinh24.ezquiz.ui.ReviewGeneratedQuizActivity;
import dev.vtvinh24.ezquiz.ui.adapter.TopicAdapter;
import dev.vtvinh24.ezquiz.util.UserLimitValidator;
import dev.vtvinh24.ezquiz.util.UserLimits;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GenerateQuizFragment extends Fragment implements TopicAdapter.OnTopicClickListener {
    private static final String TAG = "GenerateQuizFragment";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 200;

    private TextInputEditText editAiPrompt;
    private MaterialButton btnGenerateQuiz;
    private MaterialButton btnVoiceInput;
    private MaterialButton btnUploadImage;
    private MaterialButton btnRemoveImage;
    private CircularProgressIndicator progressBar;
    private TextView textAiStatus;
    private TextView textPromptsCount;
    private TextView textImagesCount;
    private MaterialCardView layoutStatus;
    private RecyclerView recyclerTopics;
    private MaterialCardView cardImagePreview;
    private ImageView imagePreview;
    private TextView textImageName;

    private TopicAdapter topicAdapter;
    private File selectedImageFile;
    private boolean isVoiceInputActive = false;

    private UserLimitValidator limitValidator;
    private AppDatabase database;

    private final List<String> quickTopics = Arrays.asList(
            "Thể thao", "Khoa học", "Lịch sử", "Văn học", "Toán học",
            "Địa lý", "Tiếng Anh", "Công nghệ", "Âm nhạc", "Điện ảnh",
            "Thiên nhiên", "Y học", "Kinh tế", "Chính trị", "Nghệ thuật"
    );

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
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
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
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
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    boolean shouldClearData = result.getData().getBooleanExtra(
                            ReviewGeneratedQuizActivity.EXTRA_CLEAR_GENERATE_DATA, false);
                    if (shouldClearData) {
                        clearGenerateData();
                        Toast.makeText(getContext(), "Đã xóa dữ liệu tạo quiz", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_generate_quiz, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        limitValidator = new UserLimitValidator(requireContext());
        database = AppDatabaseProvider.getDatabase(requireContext());

        initializeViews(view);
        setupTopicSuggestions();
        setupClickListeners();
        updateUIBasedOnUserLimits();
        updateDailyUsageStats();
    }

    private void initializeViews(View view) {
        editAiPrompt = view.findViewById(R.id.edit_ai_prompt);
        btnGenerateQuiz = view.findViewById(R.id.btn_generate_quiz);
        btnVoiceInput = view.findViewById(R.id.btn_voice_input);
        btnUploadImage = view.findViewById(R.id.btn_upload_image);
        btnRemoveImage = view.findViewById(R.id.btn_remove_image);
        progressBar = view.findViewById(R.id.progress_bar);
        textAiStatus = view.findViewById(R.id.text_ai_status);
        textPromptsCount = view.findViewById(R.id.text_prompts_count);
        textImagesCount = view.findViewById(R.id.text_images_count);
        layoutStatus = view.findViewById(R.id.layout_status);
        recyclerTopics = view.findViewById(R.id.recycler_topics);
        cardImagePreview = view.findViewById(R.id.card_image_preview);
        imagePreview = view.findViewById(R.id.image_preview);
        textImageName = view.findViewById(R.id.text_image_name);
    }

    private void setupTopicSuggestions() {
        topicAdapter = new TopicAdapter(quickTopics, this);
        recyclerTopics.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
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

        if (!SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            Toast.makeText(getContext(), "Thiết bị không hỗ trợ nhận dạng giọng nói", Toast.LENGTH_SHORT).show();
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

        Toast.makeText(getContext(), "Đã thêm: " + spokenText, Toast.LENGTH_SHORT).show();
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
                Glide.with(this)
                        .load(imageUri)
                        .centerCrop()
                        .into(imagePreview);

                textImageName.setText(selectedImageFile.getName());
                cardImagePreview.setVisibility(View.VISIBLE);

                Toast.makeText(getContext(), "Đã chọn hình ảnh: " + selectedImageFile.getName(), Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error handling selected image", e);
            Toast.makeText(getContext(), "Lỗi khi xử lý hình ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeSelectedImage() {
        selectedImageFile = null;
        cardImagePreview.setVisibility(View.GONE);
        imagePreview.setImageDrawable(null);
        textImageName.setText("");
    }

    private String getMimeTypeFromUri(Uri uri) {
        String mimeType = requireContext().getContentResolver().getType(uri);
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
                    return "image/jpeg";
            }
        }
        return mimeType;
    }

    private File createFileFromUri(Uri uri) throws IOException {
        InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
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
        File file = new File(requireContext().getCacheDir(), fileName);

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
            Toast.makeText(getContext(), "Vui lòng nhập nội dung hoặc chọn hình ảnh", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            UserEntity currentUser = database.userDao().getCurrentUserSync();

            requireActivity().runOnUiThread(() -> {
                if (currentUser == null) {
                    Toast.makeText(getContext(), "Vui lòng đăng nhập để sử dụng tính năng này", Toast.LENGTH_SHORT).show();
                    return;
                }

                int requestedQuestions = currentUser.isPremium() ?
                    UserLimits.PREMIUM_MAX_QUIZ_QUESTIONS :
                    UserLimits.FREE_MAX_QUIZ_QUESTIONS;

                UserLimitValidator.ValidationResult validation = limitValidator.validateQuizGeneration(
                    currentUser, requestedQuestions, selectedImageFile != null);

                if (!validation.isValid) {
                    showPremiumUpgradeDialog(validation.message);
                    return;
                }

                proceedWithQuizGeneration(prompt);
            });
        }).start();
    }

    private void proceedWithQuizGeneration(String prompt) {
        showLoading(true);

        AIService aiService = RetrofitClient.getAIService(AIService.class);

        try {
            if (selectedImageFile != null) {
                String mimeType = "image/jpeg";
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
                RequestBody promptBody = RequestBody.create(MediaType.parse("text/plain"), prompt);
                aiService.generateQuizTextOnly(promptBody).enqueue(createQuizCallback());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error creating request", e);
            showLoading(false);
            Toast.makeText(getContext(), "Lỗi khi tạo yêu cầu", Toast.LENGTH_SHORT).show();
        }
    }

    private void showPremiumUpgradeDialog(String message) {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Nâng cấp Premium")
                .setMessage(message + "\n\nTài khoản Premium sẽ có những lợi ích sau:\n" +
                        "• Không giới hạn số câu hỏi\n" +
                        "• Không giới hạn ảnh\n" +
                        "• Không có thời gian chờ\n" +
                        "• Hỗ trợ ưu tiên")
                .setPositiveButton("Tôi hiểu", null)
                .setNegativeButton("Hủy", null)
                .show();
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
                            // Record successful usage
                            limitValidator.recordSuccessfulRequest(selectedImageFile != null);
                            updateDailyUsageStats();

                            Intent intent = new Intent(getContext(), ReviewGeneratedQuizActivity.class);
                            String quizzesJson = new Gson().toJson(quizzes);
                            Log.d(TAG, "Success. Sending JSON to ReviewActivity: " + quizzesJson);
                            intent.putExtra(ReviewGeneratedQuizActivity.EXTRA_GENERATED_QUIZZES, quizzesJson);
                            reviewQuizLauncher.launch(intent);
                        } else {
                            Log.e(TAG, "Response successful but quizzes list is null or empty");
                            Toast.makeText(getContext(),
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

                        Toast.makeText(getContext(),
                                "Lỗi từ server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response", e);
                    Toast.makeText(getContext(),
                            "Lỗi khi xử lý phản hồi từ server", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<GenerateQuizResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network call failed", t);
                Toast.makeText(getContext(),
                        "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            layoutStatus.setVisibility(View.VISIBLE);
            btnGenerateQuiz.setEnabled(false);
        } else {
            layoutStatus.setVisibility(View.GONE);
            btnGenerateQuiz.setEnabled(true);
        }
    }

    private void updateDailyUsageStats() {
        new Thread(() -> {
            UserEntity currentUser = database.userDao().getCurrentUserSync();

            requireActivity().runOnUiThread(() -> {
                if (currentUser == null) {
                    textPromptsCount.setText("0/0");
                    textImagesCount.setText("0/0");
                    return;
                }

                UserLimitValidator.LimitStatus status = limitValidator.getCurrentLimitStatus(currentUser.isPremium());

                if (currentUser.isPremium()) {
                    textPromptsCount.setText("∞");
                    textImagesCount.setText("∞");
                } else {
                    int usedPrompts = UserLimits.FREE_MAX_QUIZ_SETS_PER_DAY - status.remainingDailyQuizzes;
                    int usedImages = UserLimits.FREE_MAX_IMAGES_PER_SESSION - status.remainingSessionImages;

                    textPromptsCount.setText(String.format("%d/%d", usedPrompts, UserLimits.FREE_MAX_QUIZ_SETS_PER_DAY));
                    textImagesCount.setText(String.format("%d/%d", usedImages, UserLimits.FREE_MAX_IMAGES_PER_SESSION));
                }
            });
        }).start();
    }

    private void updateUIBasedOnUserLimits() {
        new Thread(() -> {
            UserEntity currentUser = database.userDao().getCurrentUserSync();

            requireActivity().runOnUiThread(() -> {
                if (currentUser == null) {
                    textAiStatus.setText("Vui lòng đăng nhập");
                    btnGenerateQuiz.setEnabled(false);
                    return;
                }

                UserLimitValidator.LimitStatus status = limitValidator.getCurrentLimitStatus(currentUser.isPremium());

                if (currentUser.isPremium()) {
                    textAiStatus.setText("Premium - Không giới hạn");
                    btnGenerateQuiz.setEnabled(true);
                } else {
                    String statusText = String.format("Còn lại: %d quiz hôm nay, %d ảnh",
                        status.remainingDailyQuizzes, status.remainingSessionImages);
                    textAiStatus.setText(statusText);

                    btnGenerateQuiz.setEnabled(status.remainingDailyQuizzes > 0);
                }
            });
        }).start();
    }

    private boolean checkAudioPermission() {
        return ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestAudioPermission() {
        ActivityCompat.requestPermissions(requireActivity(),
                new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceInput();
            } else {
                Toast.makeText(getContext(), "Cần quyền ghi âm để sử dụng tính năng giọng nói", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void clearGenerateData() {
        editAiPrompt.setText("");
        removeSelectedImage();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateDailyUsageStats();
        updateUIBasedOnUserLimits();
    }
}
