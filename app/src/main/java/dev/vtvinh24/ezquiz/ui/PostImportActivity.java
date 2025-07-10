package dev.vtvinh24.ezquiz.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

public class PostImportActivity extends AppCompatActivity {
  public static final String EXTRA_COLLECTION_ID = "collection_id";
  public static final String EXTRA_SET_NAME = "set_name";

  private static final String TAG = "PostImportActivity";
  private static final int CAMERA_PERMISSION_REQUEST_CODE = 1001;

  private long selectedCollectionId = -1;
  private String setName = null;
  private boolean isProcessing = false;

  // QR scanner components
  private final Gson gson = new Gson();
  private ExecutorService cameraExecutor;
  private QRParser qrParser;

  // UI components
  private PreviewView previewView;
  private MaterialToolbar toolbar;
  private MaterialTextView textStatus;
  private MaterialCardView statusCard;
  private MaterialButton cancelButton;
  private MaterialCardView qrPlaceholder;
  private View qrFrameOverlay;
  private MaterialTextView instructionText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Intent intent = getIntent();
    if (!intent.hasExtra(EXTRA_COLLECTION_ID) || !intent.hasExtra(EXTRA_SET_NAME)) {
      // If missing extras, start step 1
      Intent step1 = new Intent(this, PreImportActivity.class);
      startActivity(step1);
      finish();
      return;
    }

    selectedCollectionId = intent.getLongExtra(EXTRA_COLLECTION_ID, -1);
    setName = intent.getStringExtra(EXTRA_SET_NAME);

    setContentView(R.layout.activity_qr_scanner);

    initializeViews();
    setupToolbar();
    setupClickListeners();
    setupCameraMode();
  }

  private void initializeViews() {
    toolbar = findViewById(R.id.toolbar);
    previewView = findViewById(R.id.camera_preview);
    qrFrameOverlay = findViewById(R.id.qr_frame_overlay);
    qrPlaceholder = findViewById(R.id.qr_placeholder);
    instructionText = findViewById(R.id.instruction_text);
    textStatus = findViewById(R.id.text_import_status);
    statusCard = findViewById(R.id.status_card);
    cancelButton = findViewById(R.id.import_button);

    // Set up Chrome extension link
    MaterialTextView chromeExtensionLink = findViewById(R.id.chrome_extension_link);
    chromeExtensionLink.setOnClickListener(v -> openChromeExtension());
  }

  // Opens the Chrome extension URL in the default browser
  private void openChromeExtension() {
    String url = getString(R.string.chrome_extension_url);
    Intent intent = new Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url));
    startActivity(intent);
  }

  private void setupToolbar() {
    setSupportActionBar(toolbar);
    toolbar.setTitle(R.string.title_scan_qr_code);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
  }

  private void setupClickListeners() {
    // Set up cancel button
    cancelButton.setText(R.string.qr_cancel);
    cancelButton.setOnClickListener(v -> finish());

    // Hide manual input button
    MaterialButton manualInputButton = findViewById(R.id.manual_input_button);
    manualInputButton.setVisibility(View.GONE);
  }

  private void setupCameraMode() {
    previewView.setVisibility(View.VISIBLE);
    qrFrameOverlay.setVisibility(View.VISIBLE);
    qrPlaceholder.setVisibility(View.GONE);
    instructionText.setText(R.string.position_qr_within_frame);

    // Initialize camera resources
    qrParser = new QRParser();
    cameraExecutor = Executors.newSingleThreadExecutor();

    requestCameraPermission();
  }

  // Camera handling methods
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
        Toast.makeText(this, "Camera permission is required for QR scanning", Toast.LENGTH_SHORT).show();
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
        finish();
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
      if (data.isFlashcardData() || data.isQuizData()) {
        importFromPasteId(pasteId);
      } else {
        showError("Unknown data type: " + data.getDataType());
      }
    } catch (Exception e) {
      Log.e(TAG, "Failed to parse QR data: " + e.getMessage(), e);
      showError("Failed to parse QR code data");
    }
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

  private void importFromPasteId(String pasteId) {
    showStatus(getString(R.string.importing_please_wait));

    Handler mainHandler = new Handler(Looper.getMainLooper());
    Executors.newSingleThreadExecutor().execute(() -> {
      QuizImporter importer = new QuizImporter();
      List<Quiz> quizzes = importer.importFlashcards(pasteId);

      mainHandler.post(() -> {
        if (quizzes.isEmpty()) {
          showStatus(getString(R.string.error_no_items_imported));
          return;
        }

        saveQuizzesToDatabase(quizzes, pasteId);
      });
    });
  }

  private void showStatus(String message) {
    statusCard.setVisibility(View.VISIBLE);
    textStatus.setText(message);
  }

  private void saveQuizzesToDatabase(List<Quiz> quizzes, String pasteId) {
    AppDatabase db = AppDatabaseProvider.getDatabase(this);
    QuizSetEntity set = new QuizSetEntity();
    set.name = setName;
    set.description = getString(R.string.imported_set_description, pasteId);
    set.collectionId = selectedCollectionId;
    set.createdAt = System.currentTimeMillis();
    set.updatedAt = System.currentTimeMillis();

    QuizSetRepository setRepo = new QuizSetRepository(db);
    long setId = setRepo.insertWithDefaultCollectionIfNeeded(set);
    set = setRepo.getSet(setId);

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

    showImportSuccessDialog(quizzes.size());
  }

  private void showImportSuccessDialog(int count) {
    new MaterialAlertDialogBuilder(this)
            .setTitle("Import Successful")
            .setMessage("Successfully imported " + count + " items")
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

  private void cleanupCamera() {
    if (cameraExecutor != null) {
      cameraExecutor.shutdown();
      cameraExecutor = null;
    }
    qrParser = null;
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    cleanupCamera();
  }
}
