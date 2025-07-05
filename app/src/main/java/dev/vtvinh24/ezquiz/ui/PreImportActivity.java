package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.repo.QuizCollectionRepository;

public class PreImportActivity extends AppCompatActivity {
  private static final String EXTRA_COLLECTION_ID = "collection_id";
  private static final String EXTRA_SET_NAME = "set_name";

  private Spinner spinnerCollection;
  private EditText editSetName;
  private List<QuizCollectionEntity> collections;
  private ActivityResultLauncher<Intent> qrScannerLauncher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_import_quiz_external_step1);

    setupActivityResultLauncher();
    initializeViews();
    setupCollectionSpinner();
    setupClickListeners();
  }

  private void setupActivityResultLauncher() {
    qrScannerLauncher = registerForActivityResult(
      new ActivityResultContracts.StartActivityForResult(),
      result -> {
        if (result.getResultCode() == RESULT_OK) {
          finish();
        }
      }
    );
  }

  private void initializeViews() {
    spinnerCollection = findViewById(R.id.spinner_collection);
    editSetName = findViewById(R.id.edit_set_name);
  }

  private void setupCollectionSpinner() {
    AppDatabase db = AppDatabaseProvider.getDatabase(this);
    QuizCollectionRepository collectionRepo = new QuizCollectionRepository(db);
    collections = collectionRepo.getAllCollections();

    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    for (QuizCollectionEntity c : collections) {
      adapter.add(c.name);
    }
    spinnerCollection.setAdapter(adapter);
  }

  private void setupClickListeners() {
    findViewById(R.id.btn_scan_qr).setOnClickListener(v -> startQRScanner());
    findViewById(R.id.btn_next).setOnClickListener(v -> proceedToManualImport());
  }

  private void startQRScanner() {
    if (validateInputs()) {
      Intent intent = new Intent(this, QrScannerActivity.class);
      intent.putExtra(EXTRA_COLLECTION_ID, getSelectedCollectionId());
      intent.putExtra(EXTRA_SET_NAME, getSetName());
      qrScannerLauncher.launch(intent);
    }
  }

  private void proceedToManualImport() {
    if (validateInputs()) {
      Intent intent = new Intent(this, PostImportActivity.class);
      intent.putExtra(EXTRA_COLLECTION_ID, getSelectedCollectionId());
      intent.putExtra(EXTRA_SET_NAME, getSetName());
      startActivity(intent);
      finish();
    }
  }

  private boolean validateInputs() {
    if (collections.isEmpty()) {
      Toast.makeText(this, "No collections available. Please create a collection first.", Toast.LENGTH_SHORT).show();
      return false;
    }
    return true;
  }

  private long getSelectedCollectionId() {
    int selectedIdx = spinnerCollection.getSelectedItemPosition();
    return collections.get(selectedIdx).id;
  }

  private String getSetName() {
    String name = editSetName.getText().toString().trim();
    return name.isEmpty() ? "Imported Set" : name;
  }
}
