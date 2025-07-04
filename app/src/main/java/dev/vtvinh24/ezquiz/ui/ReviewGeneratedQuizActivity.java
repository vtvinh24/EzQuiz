package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.google.android.material.appbar.MaterialToolbar;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.model.GeneratedQuizItem;

public class ReviewGeneratedQuizActivity extends AppCompatActivity {

    public static final String EXTRA_GENERATED_QUIZZES = "EXTRA_GENERATED_QUIZZES";
    private static final String TAG = "ReviewQuizActivity"; // Tag để lọc log trong Logcat

    private RecyclerView recyclerView;
    private GeneratedQuizAdapter adapter;
    private Spinner spinnerCollection;
    private EditText editSetName;
    private Button btnSave;

    private List<GeneratedQuizItem> generatedQuizzes;
    private List<QuizCollectionEntity> collections;
    private AppDatabase db;

    // Dùng Executor để chạy tác vụ DB trên luồng nền, tránh lỗi ANR
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_generated_quiz);

        // Set up toolbar
        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        // Khởi tạo database ngay từ đầu
        try {
            db = AppDatabaseProvider.getDatabase(this);
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize database", e);
            Toast.makeText(this, "Error: Could not initialize database", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        try {
            String quizzesJson = getIntent().getStringExtra(EXTRA_GENERATED_QUIZZES);
            Log.d(TAG, "Received JSON data: " + quizzesJson);

            if (quizzesJson == null || quizzesJson.isEmpty()) {
                Log.e(TAG, "No quiz data received");
                Toast.makeText(this, "Error: No quiz data received", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Type listType = new TypeToken<List<GeneratedQuizItem>>(){}.getType();
            generatedQuizzes = new Gson().fromJson(quizzesJson, listType);

            if (generatedQuizzes == null || generatedQuizzes.isEmpty()) {
                Log.e(TAG, "Failed to parse quiz data or empty quiz list");
                Toast.makeText(this, "Error: Invalid quiz data", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            Log.d(TAG, "Successfully parsed " + generatedQuizzes.size() + " quizzes");

            // Initialize views and setup UI
            initializeViews();
            setupRecyclerView();
            loadCollections();

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
            Toast.makeText(this, "Error: Invalid data format", Toast.LENGTH_SHORT).show();
            finish();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in onCreate: " + e.getMessage(), e);
            Toast.makeText(this, "An unexpected error occurred", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initializeViews() {
        Log.d(TAG, "Initializing views");
        try {
            recyclerView = findViewById(R.id.recycler_view_generated_quiz);
            spinnerCollection = findViewById(R.id.spinner_collection);
            editSetName = findViewById(R.id.edit_set_name);
            btnSave = findViewById(R.id.btn_save_quiz_set);

            btnSave.setOnClickListener(v -> saveQuizSet());
        } catch (Exception e) {
            Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
            Toast.makeText(this, "Error setting up the screen", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void setupRecyclerView() {
        Log.d(TAG, "setupRecyclerView: Setting up RecyclerView.");
        adapter = new GeneratedQuizAdapter(this, generatedQuizzes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        Log.d(TAG, "RecyclerView setup complete.");
    }

    private void loadCollections() {
        Log.d(TAG, "loadCollections: Loading collections from database.");
        // Chạy truy vấn DB trên luồng nền
        databaseExecutor.execute(() -> {
            collections = db.quizCollectionDao().getAll();

            // Cập nhật UI trên luồng chính
            runOnUiThread(() -> {
                if (collections == null) {
                    Log.e(TAG, "loadCollections: Failed to load collections from database.");
                    Toast.makeText(this, "Error loading collections.", Toast.LENGTH_SHORT).show();
                    return;
                }

                Log.d(TAG, "Loaded " + collections.size() + " collections.");
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                for (QuizCollectionEntity c : collections) {
                    spinnerAdapter.add(c.name);
                }
                spinnerCollection.setAdapter(spinnerAdapter);
                Log.d(TAG, "Spinner adapter set.");
            });
        });
    }

    private void saveQuizSet() {
        Log.d(TAG, "saveQuizSet: Save button clicked.");
        String setName = editSetName.getText().toString().trim();
        if (setName.isEmpty()) {
            editSetName.setError("Set name cannot be empty");
            return;
        }

        if (spinnerCollection.getSelectedItemPosition() < 0 || collections == null || collections.isEmpty()) {
            Toast.makeText(this, "Please select a collection", Toast.LENGTH_SHORT).show();
            return;
        }

        long selectedCollectionId = collections.get(spinnerCollection.getSelectedItemPosition()).id;
        Log.d(TAG, "Selected collection ID: " + selectedCollectionId);

        // Lấy dữ liệu đã được cập nhật từ adapter
        List<GeneratedQuizItem> quizzesToSave = adapter.getUpdatedQuizItems();
        Log.d(TAG, "Number of quizzes to save: " + quizzesToSave.size());

        Toast.makeText(this, "Saving...", Toast.LENGTH_SHORT).show();

        // Chạy tác vụ lưu vào DB trên luồng nền
        databaseExecutor.execute(() -> {
            try {
                // 1. Tạo và chèn QuizSet
                QuizSetEntity newSet = new QuizSetEntity();
                newSet.name = setName;
                newSet.description = "Generated by AI";
                newSet.collectionId = selectedCollectionId;
                newSet.createdAt = System.currentTimeMillis();
                newSet.updatedAt = System.currentTimeMillis();

                long newSetId = db.quizSetDao().insert(newSet);
                Log.d(TAG, "New QuizSet created with ID: " + newSetId);

                // 2. Chèn từng câu hỏi
                for (GeneratedQuizItem item : quizzesToSave) {
                    QuizEntity quizEntity = new QuizEntity();
                    quizEntity.question = item.question;
                    quizEntity.answers = item.answers;
                    quizEntity.correctAnswerIndices = item.correctAnswerIndices;
                    quizEntity.type = item.type;
                    quizEntity.quizSetId = newSetId;
                    quizEntity.createdAt = System.currentTimeMillis();
                    quizEntity.updatedAt = System.currentTimeMillis();

                    long quizId = db.quizDao().insert(quizEntity);
                    Log.d(TAG, "Inserted quiz with ID: " + quizId);
                }
                Log.d(TAG, "Saved " + quizzesToSave.size() + " quizzes to the new set.");

                // Hiển thị thông báo và đóng Activity trên luồng UI
                runOnUiThread(() -> {
                    Toast.makeText(this, "Quiz set saved successfully!", Toast.LENGTH_LONG).show();
                    setResult(RESULT_OK);
                    finish();
                });
            } catch (Exception e) {
                Log.e(TAG, "Error saving quiz set", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error saving quiz set: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Đóng executor khi activity bị hủy để tránh rò rỉ bộ nhớ
        if (databaseExecutor != null && !databaseExecutor.isShutdown()) {
            databaseExecutor.shutdown();
        }
        Log.d(TAG, "onDestroy: Activity destroyed.");
    }
}
