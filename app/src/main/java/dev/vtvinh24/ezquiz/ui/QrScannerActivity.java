package dev.vtvinh24.ezquiz.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.core.resolutionselector.ResolutionSelector;
import androidx.camera.core.resolutionselector.ResolutionStrategy;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.Gson;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.model.QRData;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.repo.QuizSetRepository;
import dev.vtvinh24.ezquiz.network.QuizImporter;
import dev.vtvinh24.ezquiz.util.QRParser;

public class QrScannerActivity extends AppCompatActivity {
  public static final String EXTRA_COLLECTION_ID = "collection_id";
  public static final String EXTRA_SET_NAME = "set_name";
  public static final String EXTRA_USE_CAMERA = "use_camera";

  private static final String TAG = "QrScannerActivity";
  private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

  private final Gson gson = new Gson();
  private ExecutorService cameraExecutor;
  private QRParser qrParser;
  private boolean isProcessing = false;

  // UI components
  private PreviewView previewView;
  private MaterialToolbar toolbar;
  private MaterialButton manualInputButton;
  private MaterialButton importButton;
  private MaterialCardView qrPlaceholder;
  private MaterialCardView statusCard;
  private MaterialTextView textStatus;
  private View qrFrameOverlay;
  private MaterialTextView instructionText;

  // Data
  private long collectionId;
  private String setName;
  private boolean useCamera = true;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_qr_scanner);

    getIntentData();
    initializeViews();
    setupToolbar();
    setupClickListeners();

    if (useCamera) {
      setupForScanningMode();
    } else {
      setupForManualMode();
    }
  }

  private void getIntentData() {
    collectionId = getIntent().getLongExtra(EXTRA_COLLECTION_ID, -1);
    setName = getIntent().getStringExtra(EXTRA_SET_NAME);
    useCamera = getIntent().getBooleanExtra(EXTRA_USE_CAMERA, true);

    if (collectionId == -1 || setName == null) {
      Toast.makeText(this, "Invalid import parameters", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private void initializeViews() {
    previewView = findViewById(R.id.camera_preview);
    toolbar = findViewById(R.id.toolbar);
    manualInputButton = findViewById(R.id.manual_input_button);
    importButton = findViewById(R.id.import_button);
    qrPlaceholder = findViewById(R.id.qr_placeholder);
    statusCard = findViewById(R.id.status_card);
    textStatus = findViewById(R.id.text_import_status);
    qrFrameOverlay = findViewById(R.id.qr_frame_overlay);
    instructionText = findViewById(R.id.instruction_text);

    qrParser = new QRParser();
    cameraExecutor = Executors.newSingleThreadExecutor();
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
      getSupportActionBar().setDisplayShowHomeEnabled(true);
    }
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
  }

  private void setupClickListeners() {
    manualInputButton.setOnClickListener(v -> showManualInputDialog());

    if (useCamera) {
      importButton.setText(R.string.qr_cancel);
      importButton.setOnClickListener(v -> finish());
    } else {
      importButton.setText(R.string.import_manually);
      importButton.setOnClickListener(v -> showManualInputDialog());
    }
  }

  private void setupForScanningMode() {
    previewView.setVisibility(View.VISIBLE);
    qrFrameOverlay.setVisibility(View.VISIBLE);
    qrPlaceholder.setVisibility(View.GONE);
    instructionText.setText(R.string.position_qr_within_frame);
    setTitle(R.string.title_scan_qr_code);
    requestCameraPermission();
  }

  private void setupForManualMode() {
    previewView.setVisibility(View.GONE);
    qrFrameOverlay.setVisibility(View.GONE);
    qrPlaceholder.setVisibility(View.VISIBLE);
    instructionText.setText(R.string.scan_qr_or_import_manually);
    setTitle(R.string.import_quiz);
  }

  private void showManualInputDialog() {
    // Create layout for dialog
    TextInputLayout textInputLayout = new TextInputLayout(this);
    textInputLayout.setHint("Enter QR code data manually");
    textInputLayout.setPadding(48, 32, 48, 32);
    textInputLayout.setBoxBackgroundMode(TextInputLayout.BOX_BACKGROUND_OUTLINE);

    TextInputEditText editText = new TextInputEditText(this);
    textInputLayout.addView(editText);

    new MaterialAlertDialogBuilder(this)
            .setTitle("Manual QR Input")
            .setView(textInputLayout)
            .setPositiveButton("Process", (dialog, which) -> {
              String qrData = editText.getText().toString().trim();
              if (!qrData.isEmpty()) {
                handleQRResult(qrData);
              }
            })
            .setNegativeButton("Cancel", null)
            .show();
  }

  // Show the import status and message
  private void showStatus(String message) {
    statusCard.setVisibility(View.VISIBLE);
    textStatus.setText(message);
  }

  // Hide the status message
  private void hideStatus() {
    statusCard.setVisibility(View.GONE);
  }

  private void requestCameraPermission() {
    if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
      startCamera();
    } else {
      ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
    }
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
      if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        startCamera();
      } else {
        Toast.makeText(this, "Camera permission is required", Toast.LENGTH_SHORT).show();
        finish();
      }
    }
  }

  private void startCamera() {
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);
    cameraProviderFuture.addListener(() -> {
      try {
        ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
        bindCameraUseCases(cameraProvider);
      } catch (ExecutionException | InterruptedException e) {
        Log.e(TAG, "Camera initialization failed", e);
        Toast.makeText(this, "Camera initialization failed", Toast.LENGTH_SHORT).show();
      }
    }, ContextCompat.getMainExecutor(this));
  }

  @OptIn(markerClass = ExperimentalGetImage.class)
  private void bindCameraUseCases(ProcessCameraProvider cameraProvider) {
    Preview preview = new Preview.Builder().build();
    preview.setSurfaceProvider(previewView.getSurfaceProvider());

    ResolutionSelector resolutionSelector = new ResolutionSelector.Builder().setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY).build();

    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder().setResolutionSelector(resolutionSelector).setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();

    imageAnalysis.setAnalyzer(cameraExecutor, this::analyzeImage);

    CameraSelector cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA;

    try {
      cameraProvider.unbindAll();
      cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis);
    } catch (Exception e) {
      Log.e(TAG, "Camera binding failed", e);
    }
  }

  @ExperimentalGetImage
  private void analyzeImage(ImageProxy imageProxy) {
    if (isProcessing) {
      imageProxy.close();
      return;
    }

    isProcessing = true;
    Image mediaImage = imageProxy.getImage();

    if (mediaImage != null) {
      qrParser.getDataFromQR(mediaImage).addOnSuccessListener(result -> {
        if (result != null && !result.isEmpty()) {
          handleQRResult(result);
        }
        isProcessing = false;
        imageProxy.close();
      }).addOnFailureListener(e -> {
        Log.e(TAG, "QR parsing failed", e);
        isProcessing = false;
        imageProxy.close();
      });
    } else {
      isProcessing = false;
      imageProxy.close();
    }
  }

  private void handleQRResult(String qrData) {
    try {
      // Parse the QR data into our QRData model
      QRData data = gson.fromJson(qrData, QRData.class);

      if (data == null || data.getType() == null || data.getUrl() == null) {
        showError("QR code does not contain valid data format");
        return;
      }

      // Validate that this is a request type QR code
      if (!data.isRequestType()) {
        showError("Unsupported QR code type: " + data.getType());
        return;
      }

      String url = data.getUrl();
      String pasteId = extractPasteId(url);

      if (pasteId == null) {
        showError("Invalid URL format in QR code");
        return;
      }

      // Process according to the data type
      if (data.isFlashcardData()) {
        importFlashcardsFromPaste(pasteId);
      } else if (data.isQuizData()) {
        importQuizzesFromPaste(pasteId);
      } else {
        showError("Unknown data type: " + data.getDataType());
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to parse QR data: " + e.getMessage(), e);
      showError("Failed to parse QR code data");
    }
  }

  private void importQuizzesFromPaste(String pasteId) {
    new Thread(() -> {
      try {
        QuizImporter importer = new QuizImporter();
        List<Quiz> quizzes = importer.importQuizzes(pasteId);

        runOnUiThread(() -> {
          if (!quizzes.isEmpty()) {
            saveQuizzesToDatabase(quizzes);
          } else {
            showError(getString(R.string.error_no_items_imported));
          }
        });
      } catch (Exception e) {
        Log.e(TAG, "Import failed", e);
        runOnUiThread(() -> showError("Failed to import quizzes"));
      }
    }).start();
  }

  private void importFlashcardsFromPaste(String pasteId) {
    new Thread(() -> {
      try {
        QuizImporter importer = new QuizImporter();
        List<Quiz> quizzes = importer.importFlashcards(pasteId);

        runOnUiThread(() -> {
          if (!quizzes.isEmpty()) {
            saveFlashcardsToDatabase(quizzes);
          } else {
            showError(getString(R.string.error_no_items_imported));
          }
        });
      } catch (Exception e) {
        Log.e(TAG, "Import failed", e);
        runOnUiThread(() -> showError("Failed to import flashcards"));
      }
    }).start();
  }

  private void saveQuizzesToDatabase(List<Quiz> quizzes) {
    new Thread(() -> {
      try {
        AppDatabase db = AppDatabaseProvider.getDatabase(this);
        QuizSetRepository setRepo = new QuizSetRepository(db);

        QuizSetEntity set = new QuizSetEntity();
        set.name = setName;
        set.description = "Imported quizzes from QR code";
        set.collectionId = collectionId;
        set.createdAt = System.currentTimeMillis();
        set.updatedAt = System.currentTimeMillis();

        long setId = setRepo.insertWithDefaultCollectionIfNeeded(set);

        for (Quiz quiz : quizzes) {
          QuizEntity entity = new QuizEntity();
          entity.question = quiz.getQuestion();
          entity.answers = quiz.getAnswers();
          entity.correctAnswerIndices = quiz.getCorrectAnswerIndices();
          entity.type = quiz.getType();
          entity.quizSetId = setId;
          entity.createdAt = System.currentTimeMillis();
          entity.updatedAt = System.currentTimeMillis();
          db.quizDao().insert(entity);
        }

        runOnUiThread(() -> showImportSuccessDialog(quizzes.size(), "quizzes"));
      } catch (Exception e) {
        Log.e(TAG, "Database save failed", e);
        runOnUiThread(() -> showError("Failed to save quizzes to database"));
      }
    }).start();
  }

  private void saveFlashcardsToDatabase(List<Quiz> quizzes) {
    new Thread(() -> {
      try {
        AppDatabase db = AppDatabaseProvider.getDatabase(this);
        QuizSetRepository setRepo = new QuizSetRepository(db);

        QuizSetEntity set = new QuizSetEntity();
        set.name = setName;
        set.description = "Imported flashcards from QR code";
        set.collectionId = collectionId;
        set.createdAt = System.currentTimeMillis();
        set.updatedAt = System.currentTimeMillis();

        long setId = setRepo.insertWithDefaultCollectionIfNeeded(set);

        for (Quiz quiz : quizzes) {
          QuizEntity entity = new QuizEntity();
          entity.question = quiz.getQuestion();
          entity.answers = quiz.getAnswers();
          entity.correctAnswerIndices = quiz.getCorrectAnswerIndices();
          entity.type = quiz.getType();
          entity.quizSetId = setId;
          entity.createdAt = System.currentTimeMillis();
          entity.updatedAt = System.currentTimeMillis();
          db.quizDao().insert(entity);
        }

        runOnUiThread(() -> showImportSuccessDialog(quizzes.size(), "flashcards"));
      } catch (Exception e) {
        Log.e(TAG, "Database save failed", e);
        runOnUiThread(() -> showError("Failed to save flashcards to database"));
      }
    }).start();
  }

  private String extractPasteId(String url) {
    if (url == null || url.isEmpty()) return null;

    try {
      if (url.startsWith("https://paste.rs/")) {
        String[] parts = url.split("/");
        return parts.length >= 4 ? parts[3] : null;
      }
      if (url.startsWith("https://0x0.st/")) {
        String[] parts = url.split("/");
        return parts.length >= 4 ? parts[3] : null;
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to extract paste ID", e);
    }
    return null;
  }

  private void showImportSuccessDialog(int count, String type) {
    new MaterialAlertDialogBuilder(this)
            .setTitle("Import Successful")
            .setMessage("Successfully imported " + count + " " + type)
            .setIcon(R.drawable.ic_check_circle)
            .setPositiveButton(android.R.string.ok, (dialog, which) -> {
              setResult(RESULT_OK);
              finish();
            })
            .setCancelable(false)
            .show();
  }

  private void showError(String message) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (cameraExecutor != null) {
      cameraExecutor.shutdown();
    }
  }
}
