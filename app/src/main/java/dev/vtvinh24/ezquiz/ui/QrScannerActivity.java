package dev.vtvinh24.ezquiz.ui;

import android.Manifest;
import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
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

import com.google.common.util.concurrent.ListenableFuture;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.repo.QuizSetRepository;
import dev.vtvinh24.ezquiz.network.QuizImporter;
import dev.vtvinh24.ezquiz.util.QRParser;

public class QrScannerActivity extends AppCompatActivity {
  private static final String TAG = "QrScannerActivity";
  private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

  public static final String EXTRA_COLLECTION_ID = "collection_id";
  public static final String EXTRA_SET_NAME = "set_name";

  private ExecutorService cameraExecutor;
  private QRParser qrParser;
  private boolean isProcessing = false;
  private PreviewView previewView;

  private long collectionId;
  private String setName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_qr_scanner);

    getIntentData();
    initializeComponents();
    setupClickListeners();
    requestCameraPermission();
  }

  private void getIntentData() {
    collectionId = getIntent().getLongExtra(EXTRA_COLLECTION_ID, -1);
    setName = getIntent().getStringExtra(EXTRA_SET_NAME);

    if (collectionId == -1 || setName == null) {
      Toast.makeText(this, "Invalid import parameters", Toast.LENGTH_SHORT).show();
      finish();
    }
  }

  private void initializeComponents() {
    previewView = findViewById(R.id.camera_preview);
    qrParser = new QRParser();
    cameraExecutor = Executors.newSingleThreadExecutor();
  }

  private void setupClickListeners() {
    findViewById(R.id.text_input_button).setOnClickListener(v -> showManualInputDialog());
    findViewById(R.id.submit_button).setOnClickListener(v -> finish());
  }

  private void showManualInputDialog() {
    EditText editText = new EditText(this);
    editText.setHint("Enter QR code data manually");

    new AlertDialog.Builder(this)
      .setTitle("Manual QR Input")
      .setView(editText)
      .setPositiveButton("Process", (dialog, which) -> {
        String qrData = editText.getText().toString().trim();
        if (!qrData.isEmpty()) {
          handleQRResult(qrData);
        }
      })
      .setNegativeButton("Cancel", null)
      .show();
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

    ResolutionSelector resolutionSelector = new ResolutionSelector.Builder()
      .setResolutionStrategy(ResolutionStrategy.HIGHEST_AVAILABLE_STRATEGY)
      .build();

    ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
      .setResolutionSelector(resolutionSelector)
      .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
      .build();

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
      qrParser.getDataFromQR(mediaImage)
        .addOnSuccessListener(result -> {
          if (result != null && !result.isEmpty()) {
            handleQRResult(result);
          }
          isProcessing = false;
          imageProxy.close();
        })
        .addOnFailureListener(e -> {
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
      JsonObject obj = JsonParser.parseString(qrData).getAsJsonObject();

      if (obj.has("type") && "request".equals(obj.get("type").getAsString()) && obj.has("url")) {
        String url = obj.get("url").getAsString();
        String pasteId = extractPasteId(url);

        if (pasteId != null) {
          importFlashcardsFromPaste(pasteId);
        } else {
          showError("Invalid paste.rs URL format");
        }
      } else {
        showError("QR code does not contain a valid request format");
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to parse QR data", e);
      showError("Failed to parse QR code data");
    }
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

        runOnUiThread(() -> showImportSuccessDialog(quizzes.size()));
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
    } catch (Exception e) {
      Log.e(TAG, "Failed to extract paste ID", e);
    }
    return null;
  }

  private void showImportSuccessDialog(int count) {
    new AlertDialog.Builder(this)
      .setTitle("Import Successful")
      .setMessage("Successfully imported " + count + " flashcards")
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
