package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
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
import dev.vtvinh24.ezquiz.ui.adapter.EditableQuizAdapter;

public class ReviewGeneratedQuizActivity extends AppCompatActivity implements EditableQuizAdapter.OnQuizChangeListener {

  public static final String EXTRA_GENERATED_QUIZZES = "EXTRA_GENERATED_QUIZZES";
  public static final String EXTRA_CLEAR_GENERATE_DATA = "EXTRA_CLEAR_GENERATE_DATA";
  private static final String TAG = "ReviewQuizActivity";

  private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
  private RecyclerView recyclerView;
  private EditableQuizAdapter adapter;
  private Spinner spinnerCollection;
  private EditText editSetName;
  private EditText editDescription;
  private ExtendedFloatingActionButton btnSave;
  private ExtendedFloatingActionButton fabAddQuestion;
  private MaterialTextView textQuestionCount;
  private List<GeneratedQuizItem> generatedQuizzes;
  private List<QuizCollectionEntity> collections;
  private AppDatabase db;
  private ItemTouchHelper itemTouchHelper;
  private boolean isQuizSaved = false;
  private boolean hasUnsavedChanges = false;

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
      setupItemTouchHelper();
      loadCollections();
    } catch (Exception e) {
      Log.e(TAG, "Error in onCreate", e);
      Toast.makeText(this, "An error occurred while loading the quiz", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private void setupToolbar() {
    // Setup custom navigation với style mới
    ImageView btnBack = findViewById(R.id.btn_back);
    MaterialButton btnPreview = findViewById(R.id.btn_preview);

    if (btnBack != null) {
      btnBack.setOnClickListener(v -> handleBackPressed());
    }

    if (btnPreview != null) {
      btnPreview.setOnClickListener(v -> showPreviewDialog());
    }
  }

  private void handleBackPressed() {
    if (!isQuizSaved && (hasUnsavedChanges || adapter.getItemCount() > 0)) {
      showExitConfirmationDialog();
    } else {
      finishWithResult(false);
    }
  }

  private void showExitConfirmationDialog() {
    new MaterialAlertDialogBuilder(this)
        .setTitle("Bạn có chắc muốn thoát?")
        .setMessage("Bạn chưa lưu bộ quiz này. Tất cả câu hỏi sẽ bị mất và dữ liệu tạo quiz sẽ được xóa.")
        .setPositiveButton("Thoát", (dialog, which) -> {
          finishWithResult(true);
        })
        .setNegativeButton("Ở lại", null)
        .setNeutralButton("Lưu trước", (dialog, which) -> {
          saveQuizSet();
        })
        .show();
  }

  private void finishWithResult(boolean shouldClearData) {
    if (shouldClearData) {
      setResult(RESULT_OK, getIntent().putExtra(EXTRA_CLEAR_GENERATE_DATA, true));
    } else {
      setResult(RESULT_CANCELED);
    }
    finish();
  }

  private void showPreviewDialog() {
    new MaterialAlertDialogBuilder(this)
        .setTitle("Quiz Preview")
        .setMessage("Preview functionality will show a quick overview of your quiz questions and answers.")
        .setPositiveButton("Coming Soon", null)
        .setNegativeButton("Close", null)
        .show();
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
      editDescription = findViewById(R.id.edit_description);
      btnSave = findViewById(R.id.btn_save_quiz_set);
      fabAddQuestion = findViewById(R.id.fab_add_question);
      textQuestionCount = findViewById(R.id.text_question_count);

      btnSave.setOnClickListener(v -> saveQuizSet());
      fabAddQuestion.setOnClickListener(v -> showAddQuestionDialog());

      // Set default quiz set name
      String defaultName = "AI Quiz Set " + System.currentTimeMillis();
      editSetName.setText(defaultName);

      // Set default description placeholder
      setupDefaultDescription();

      updateQuestionCount();

    } catch (Exception e) {
      Log.e(TAG, "Error initializing views: " + e.getMessage(), e);
      Toast.makeText(this, "Error setting up the screen", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private void setupDefaultDescription() {
    // Tạo description mặc định với thời gian hiện tại
    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy 'lúc' HH:mm", java.util.Locale.getDefault());
    String currentDateTime = sdf.format(new java.util.Date());
    String defaultDescription = "Được tạo vào " + currentDateTime;

    // Thiết lập placeholder hint thay vì text mặc định
    editDescription.setHint("Mô tả bộ quiz (mặc định: " + defaultDescription + ")");
  }

  private void setupRecyclerView() {
    Log.d(TAG, "Setting up RecyclerView with enhanced adapter");
    adapter = new EditableQuizAdapter(this, generatedQuizzes);
    adapter.setOnQuizChangeListener(this);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    recyclerView.setAdapter(adapter);

    Log.d(TAG, "RecyclerView setup complete with " + generatedQuizzes.size() + " items");
  }

  private void setupItemTouchHelper() {
    ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

      @Override
      public boolean onMove(@NonNull RecyclerView recyclerView,
                           @NonNull RecyclerView.ViewHolder viewHolder,
                           @NonNull RecyclerView.ViewHolder target) {
        int fromPosition = viewHolder.getAdapterPosition();
        int toPosition = target.getAdapterPosition();
        adapter.moveQuiz(fromPosition, toPosition);
        hasUnsavedChanges = true;
        return true;
      }

      @Override
      public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        showDeleteQuestionConfirmation(position);
      }

      @Override
      public boolean isLongPressDragEnabled() {
        return true;
      }

      @Override
      public boolean isItemViewSwipeEnabled() {
        return true;
      }
    };

    itemTouchHelper = new ItemTouchHelper(callback);
    itemTouchHelper.attachToRecyclerView(recyclerView);
    adapter.setItemTouchHelper(itemTouchHelper);
  }

  private void showAddQuestionDialog() {
    new MaterialAlertDialogBuilder(this)
        .setTitle("Add New Question")
        .setMessage("Choose where to add the new question:")
        .setPositiveButton("Add at End", (dialog, which) -> {
          adapter.addNewQuiz(adapter.getItemCount());
          updateQuestionCount();
          hasUnsavedChanges = true;
        })
        .setNeutralButton("Add at Beginning", (dialog, which) -> {
          adapter.addNewQuiz(0);
          updateQuestionCount();
          hasUnsavedChanges = true;
        })
        .setNegativeButton("Cancel", null)
        .show();
  }

  private void showDeleteQuestionConfirmation(int position) {
    new MaterialAlertDialogBuilder(this)
        .setTitle("Delete Question")
        .setMessage("Are you sure you want to delete this question?")
        .setPositiveButton("Delete", (dialog, which) -> {
          adapter.removeQuiz(position);
          updateQuestionCount();
          hasUnsavedChanges = true;
        })
        .setNegativeButton("Cancel", (dialog, which) -> {
          adapter.notifyItemChanged(position);
        })
        .show();
  }

  private void updateQuestionCount() {
    int count = adapter != null ? adapter.getItemCount() : generatedQuizzes.size();
    if (textQuestionCount != null) {
      textQuestionCount.setText(count + " questions • Tap to edit, swipe to delete");
    }
  }

  @Override
  public void onQuizChanged() {
    updateQuestionCount();
    hasUnsavedChanges = true;
  }

  @Override
  public void onQuizDeleted(int position) {
    updateQuestionCount();
    hasUnsavedChanges = true;
    Toast.makeText(this, "Question deleted", Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onQuizAdded(int position) {
    updateQuestionCount();
    hasUnsavedChanges = true;
    Toast.makeText(this, "Question added", Toast.LENGTH_SHORT).show();
    recyclerView.scrollToPosition(position);
  }

  @Override
  public void onBackPressed() {
    handleBackPressed();
    super.onBackPressed();
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

    List<GeneratedQuizItem> updatedQuizzes = adapter.getUpdatedQuizItems();
    if (updatedQuizzes == null || updatedQuizzes.isEmpty()) {
      Toast.makeText(this, "No valid quiz questions to save", Toast.LENGTH_SHORT).show();
      return;
    }

    QuizCollectionEntity selectedCollection = collections.get(selectedPosition);

    btnSave.setEnabled(false);
    btnSave.setText("Saving...");

    databaseExecutor.execute(() -> {
      try {
        saveQuizSetToDatabase(setName, selectedCollection, updatedQuizzes);

        runOnUiThread(() -> {
          Toast.makeText(this, "Quiz set saved successfully!", Toast.LENGTH_SHORT).show();
          isQuizSaved = true;
          hasUnsavedChanges = false;
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

    // Xử lý description - nếu trống thì dùng mặc định
    String description = editDescription.getText().toString().trim();
    if (description.isEmpty()) {
      java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy 'lúc' HH:mm", java.util.Locale.getDefault());
      String currentDateTime = sdf.format(new java.util.Date());
      description = "Được tạo vào " + currentDateTime;
    }

    // Create quiz set
    QuizSetEntity quizSet = new QuizSetEntity();
    quizSet.name = setName;
    quizSet.description = description;
    quizSet.collectionId = collection.id;
    quizSet.createdAt = System.currentTimeMillis();
    quizSet.updatedAt = System.currentTimeMillis();

    long quizSetId = db.quizSetDao().insert(quizSet);
    Log.d(TAG, "Quiz set saved with ID: " + quizSetId + " and description: " + description);

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
