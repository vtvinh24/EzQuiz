package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.repo.QuizCollectionRepository;

public class PreImportActivity extends AppCompatActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_import_quiz_external_step1);

    Spinner spinnerCollection = findViewById(R.id.spinner_collection);
    EditText editSetName = findViewById(R.id.edit_set_name);
    Button btnNext = findViewById(R.id.btn_next);

    AppDatabase db = AppDatabaseProvider.getDatabase(this);
    QuizCollectionRepository collectionRepo = new QuizCollectionRepository(db);
    List<QuizCollectionEntity> collections = collectionRepo.getAllCollections();
    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    for (QuizCollectionEntity c : collections) {
      adapter.add(c.name);
    }
    spinnerCollection.setAdapter(adapter);

    btnNext.setOnClickListener(v -> {
      int selectedIdx = spinnerCollection.getSelectedItemPosition();
      long collectionId = collections.get(selectedIdx).id;
      String setName = editSetName.getText().toString().trim();
      Intent intent = new Intent(this, PostImportActivity.class);
      intent.putExtra(PostImportActivity.EXTRA_COLLECTION_ID, collectionId);
      intent.putExtra(PostImportActivity.EXTRA_SET_NAME, setName);
      startActivity(intent);
      finish();
    });
  }
}

