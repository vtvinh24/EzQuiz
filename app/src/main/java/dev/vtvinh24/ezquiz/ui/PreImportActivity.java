package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.repo.QuizCollectionRepository;

public class PreImportActivity extends AppCompatActivity {
  private static final String EXTRA_COLLECTION_ID = "collection_id";
  private static final String EXTRA_SET_NAME = "set_name";

  private AutoCompleteTextView collectionDropdown;
  private TextInputEditText editSetName;
  private MaterialButton btnScanQR;
  private List<QuizCollectionEntity> collections;
  private ActivityResultLauncher<Intent> qrScannerLauncher;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_pre_import_quiz);

    setupActivityResultLauncher();
    initializeViews();
    setupToolbar();
    setupCollectionDropdown();
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
    collectionDropdown = findViewById(R.id.spinner_collection);
    editSetName = findViewById(R.id.edit_set_name);
    btnScanQR = findViewById(R.id.btn_scan_qr);
  }

  private void setupToolbar() {
    MaterialToolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    toolbar.setNavigationOnClickListener(v -> onBackPressed());
  }

  private void setupCollectionDropdown() {
    AppDatabase db = AppDatabaseProvider.getDatabase(this);
    QuizCollectionRepository collectionRepo = new QuizCollectionRepository(db);
    collections = collectionRepo.getAllCollections();

    ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
        android.R.layout.simple_dropdown_item_1line);

    for (QuizCollectionEntity collection : collections) {
      adapter.add(collection.name);
    }

    collectionDropdown.setAdapter(adapter);

    // Select first item by default if available
    if (!collections.isEmpty()) {
      collectionDropdown.setText(collections.get(0).name, false);
    }
  }

  private void setupClickListeners() {
    btnScanQR.setOnClickListener(v -> startQRScanner());
  }

  private void startQRScanner() {
    if (validateInputs()) {
      Intent intent = new Intent(this, PostImportActivity.class);
      intent.putExtra(EXTRA_COLLECTION_ID, getSelectedCollectionId());
      intent.putExtra(EXTRA_SET_NAME, getSetName());
      qrScannerLauncher.launch(intent);
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
    String selectedCollectionName = collectionDropdown.getText().toString();
    for (QuizCollectionEntity collection : collections) {
      if (collection.name.equals(selectedCollectionName)) {
        return collection.id;
      }
    }
    // Return first collection id as fallback
    return collections.get(0).id;
  }

  private String getSetName() {
    String name = editSetName.getText().toString().trim();
    return name.isEmpty() ? "Imported Set" : name;
  }
}
