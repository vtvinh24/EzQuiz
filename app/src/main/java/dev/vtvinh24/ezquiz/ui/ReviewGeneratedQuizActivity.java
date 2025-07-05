package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
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
  private static final String TAG = "ReviewQuizActivity";

  private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
  private RecyclerView recyclerView;
  private GeneratedQuizAdapter adapter;
  private Spinner spinnerCollection;
  private EditText editSetName;
  private ExtendedFloatingActionButton btnSave;
  private List<GeneratedQuizItem> generatedQuizzes;
  private List<QuizCollectionEntity> collections;
  private AppDatabase db;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_review_generated_quiz);

    setupToolbar();
    initializeDatabase();

    try {
      loadQuizData();
      initializeViews();
      setupRecyclerView();
      loadCollections();
    } catch (Exception e) {
      Log.e(TAG, "Error in onCreate", e);
      Toast.makeText(this, "An error occurred while loading the quiz", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private void setupToolbar() {
    MaterialToolbar toolbar = findViewById(R.id.topAppBar);
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
    toolbar.setNavigationOnClickListener(v -> finish());
  }

  private void initializeDatabase() {
    try {
      db = AppDatabaseProvider.getDatabase(this);
    } catch (Exception e) {
      Log.e(TAG, "Failed to initialize database", e);
      Toast.makeText(this, "Error: Could not initialize database", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private void loadQuizData() {
    String quizzesJson = getIntent().getStringExtra(EXTRA_GENERATED_QUIZZES);
    Log.d(TAG, "Received JSON data: " + quizzesJson);

    if (quizzesJson == null || quizzesJson.isEmpty()) {
      Log.e(TAG, "No quiz data received");
      Toast.makeText(this, "Error: No quiz data received", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    try {
      Type listType = new TypeToken<List<GeneratedQuizItem>>() {}.getType();
      generatedQuizzes = new Gson().fromJson(quizzesJson, listType);

      if (generatedQuizzes == null || generatedQuizzes.isEmpty()) {
        Log.e(TAG, "Failed to parse quiz data or empty quiz list");
        Toast.makeText(this, "Error: Invalid quiz data", Toast.LENGTH_SHORT).show();
        finish();
        return;
      }

      Log.d(TAG, "Successfully parsed " + generatedQuizzes.size() + " quizzes");
    } catch (JsonSyntaxException e) {
      Log.e(TAG, "JSON parsing error: " + e.getMessage(), e);
      Toast.makeText(this, "Error: Invalid data format", Toast.LENGTH_SHORT).show();
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

      // Set default quiz set name
      String defaultName = "AI Quiz Set " + System.currentTimeMillis();
      editSetName.setText(defaultName);

    } catch (Exception e) {
      Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
      Toast.makeText(this, "Error setting up the screen", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private void setupRecyclerView() {
    Log.d(TAG, "Setting up RecyclerView with enhanced adapter");
    adapter = new GeneratedQuizAdapter(this, generatedQuizzes);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(adapter);

    // Add some spacing between items
    recyclerView.addItemDecoration(new androidx.recyclerview.widget.DividerItemDecoration(
        this, androidx.recyclerview.widget.DividerItemDecoration.VERTICAL));

    Log.d(TAG, "RecyclerView setup complete with " + generatedQuizzes.size() + " items");
  }

  private void loadCollections() {
    Log.d(TAG, "Loading collections from database");
    databaseExecutor.execute(() -> {
      try {
        collections = db.quizCollectionDao().getAll();

        runOnUiThread(() -> {
          if (collections == null) {
            Log.e(TAG, "Failed to load collections from database");
            Toast.makeText(this, "Error loading collections.", Toast.LENGTH_SHORT).show();
            return;
          }

          Log.d(TAG, "Loaded " + collections.size() + " collections");
          setupCollectionSpinner();
        });
      } catch (Exception e) {
        Log.e(TAG, "Error loading collections", e);
        runOnUiThread(() -> Toast.makeText(this, "Error loading collections", Toast.LENGTH_SHORT).show());
      }
    });
  }

  private void setupCollectionSpinner() {
    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_spinner_item);
    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

    for (QuizCollectionEntity collection : collections) {
      spinnerAdapter.add(collection.name);
    }

    spinnerCollection.setAdapter(spinnerAdapter);

    // Select first collection by default if available
    if (!collections.isEmpty()) {
      spinnerCollection.setSelection(0);
    }
  }

  private void saveQuizSet() {
    String setName = editSetName.getText().toString().trim();
    if (setName.isEmpty()) {
      Toast.makeText(this, "Please enter a quiz set name", Toast.LENGTH_SHORT).show();
      return;
    }

    if (collections == null || collections.isEmpty()) {
      Toast.makeText(this, "No collections available", Toast.LENGTH_SHORT).show();
      return;
    }

    int selectedPosition = spinnerCollection.getSelectedItemPosition();
    if (selectedPosition < 0 || selectedPosition >= collections.size()) {
      Toast.makeText(this, "Please select a collection", Toast.LENGTH_SHORT).show();
      return;
    }

    // Get updated quiz data from adapter
    List<GeneratedQuizItem> updatedQuizzes = adapter.getUpdatedQuizItems();
    if (updatedQuizzes == null || updatedQuizzes.isEmpty()) {
      Toast.makeText(this, "No quiz questions to save", Toast.LENGTH_SHORT).show();
      return;
    }

    QuizCollectionEntity selectedCollection = collections.get(selectedPosition);

    // Show loading state
    btnSave.setEnabled(false);
    btnSave.setText("Saving...");

    databaseExecutor.execute(() -> {
      try {
        saveQuizSetToDatabase(setName, selectedCollection, updatedQuizzes);

        runOnUiThread(() -> {
          Toast.makeText(this, "Quiz set saved successfully!", Toast.LENGTH_SHORT).show();
          finish();
        });
      } catch (Exception e) {
        Log.e(TAG, "Error saving quiz set", e);
        runOnUiThread(() -> {
          Toast.makeText(this, "Error saving quiz set: " + e.getMessage(), Toast.LENGTH_SHORT).show();
          btnSave.setEnabled(true);
          btnSave.setText("Save Quiz Set");
        });
      }
    });
  }

  private void saveQuizSetToDatabase(String setName, QuizCollectionEntity collection,
                                   List<GeneratedQuizItem> quizzes) {
    Log.d(TAG, "Saving quiz set: " + setName + " to collection: " + collection.name);

    // Create quiz set
    QuizSetEntity quizSet = new QuizSetEntity();
    quizSet.name = setName;
    quizSet.collectionId = collection.id;
    quizSet.createdAt = System.currentTimeMillis();
    quizSet.updatedAt = System.currentTimeMillis();

    long quizSetId = db.quizSetDao().insert(quizSet);
    Log.d(TAG, "Quiz set saved with ID: " + quizSetId);

    // Save individual quizzes
    for (int i = 0; i < quizzes.size(); i++) {
      GeneratedQuizItem item = quizzes.get(i);

      QuizEntity quiz = new QuizEntity();
      quiz.question = item.question;
      quiz.answers = item.answers;
      quiz.correctAnswerIndices = item.correctAnswerIndices;
      quiz.type = item.type;
      quiz.quizSetId = quizSetId;
        quiz.order = i;
      quiz.createdAt = System.currentTimeMillis();
      quiz.updatedAt = System.currentTimeMillis();

      long quizId = db.quizDao().insert(quiz);
      Log.d(TAG, "Saved quiz " + (i + 1) + " with ID: " + quizId);
    }

    Log.d(TAG, "Successfully saved " + quizzes.size() + " quizzes to quiz set");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (databaseExecutor != null && !databaseExecutor.isShutdown()) {
      databaseExecutor.shutdown();
    }
  }
}
