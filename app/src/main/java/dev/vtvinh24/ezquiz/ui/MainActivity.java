package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

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
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.network.QuizImporter;

public class MainActivity extends AppCompatActivity implements QuizCollectionAdapter.OnItemClickListener {

  private final List<QuizCollectionEntity> collections = new ArrayList<>();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private RecyclerView recyclerView;
  private QuizCollectionAdapter adapter;
  private AppDatabase db;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    // --- BƯỚC 1: KHỞI TẠO CÁC BIẾN CẦN THIẾT ---
    recyclerView = findViewById(R.id.recyclerView);
    db = AppDatabaseProvider.getDatabase(this);

    // --- BƯỚC 2: SETUP RECYCLERVIEW VÀ ADAPTER ---
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    // Khởi tạo adapter với danh sách rỗng. Dữ liệu sẽ được nạp sau trên luồng nền.
    adapter = new QuizCollectionAdapter(collections, this);
    recyclerView.setAdapter(adapter);

    // --- BƯỚC 3: SETUP CÁC NÚT BẤM (LISTENERS) ---
    FloatingActionButton fabAdd = findViewById(R.id.fab_add_collection);
    fabAdd.setOnClickListener(v -> showAddCollectionDialog());

    findViewById(R.id.fab_generate_ai).setOnClickListener(v ->
            startActivity(new Intent(this, GenerateQuizAIActivity.class)));

    findViewById(R.id.fab_import_external).setOnClickListener(v ->
            startActivity(new Intent(this, PreImportActivity.class)));

    Button btnImportAndStudy = findViewById(R.id.btn_import_and_study_test);
    btnImportAndStudy.setOnClickListener(v -> {
      Toast.makeText(this, "Đang import bộ flashcard mẫu...", Toast.LENGTH_SHORT).show();
      importAndLaunchFlashcards();
    });

    // --- BƯỚC 4: TẢI DỮ LIỆU BAN ĐẦU ---
    // Phương thức này đã được sửa để chạy đúng luồng
    refreshCollections();
  }

  // --- CÁC PHƯƠNG THỨC HỖ TRỢ (LOGIC ĐƯỢC ĐƯA RA NGOÀI CHO GỌN GÀNG) ---

  private void refreshCollections() {
    executor.execute(() -> {
      // Lấy dữ liệu trên luồng nền
      final List<QuizCollectionEntity> newCollections = db.quizCollectionDao().getAll();
      // Cập nhật UI trên Main Thread
      runOnUiThread(() -> {
        collections.clear();
        collections.addAll(newCollections);
        adapter.notifyDataSetChanged();
        updateEmptyView();
      });
    });
  }

  private void importAndLaunchFlashcards() {
    String pasteId = "JpOIB";
    executor.execute(() -> {
      QuizImporter importer = new QuizImporter();
      List<Quiz> importedQuizzes = importer.importFlashcards(pasteId);

      if (importedQuizzes == null || importedQuizzes.isEmpty()) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Lỗi: Không import được dữ liệu.", Toast.LENGTH_LONG).show());
        return;
      }

      QuizCollectionEntity defaultCollection = db.quizCollectionDao().getByName("Default");
      long collectionId;
      if (defaultCollection == null) {
        QuizCollectionEntity newDefault = new QuizCollectionEntity();
        newDefault.name = "Default";
        newDefault.description = "Bộ sưu tập mặc định";
        collectionId = db.quizCollectionDao().insert(newDefault);
      } else {
        collectionId = defaultCollection.id;
      }

      QuizSetEntity newSet = new QuizSetEntity();
      newSet.name = "Bộ Flashcard Mẫu";
      newSet.description = "Dữ liệu được import từ paste.rs/" + pasteId;
      newSet.collectionId = collectionId;
      newSet.createdAt = System.currentTimeMillis();
      newSet.updatedAt = System.currentTimeMillis();
      long newSetId = db.quizSetDao().insert(newSet);

      for (Quiz quiz : importedQuizzes) {
        QuizEntity entity = new QuizEntity();
        entity.question = quiz.getQuestion();
        entity.answers = quiz.getAnswers();
        entity.correctAnswerIndices = quiz.getCorrectAnswerIndices();
        entity.type = Quiz.Type.FLASHCARD;
        entity.quizSetId = newSetId;
        entity.createdAt = System.currentTimeMillis();
        entity.updatedAt = System.currentTimeMillis();
        db.quizDao().insert(entity);
      }

      refreshCollections();

      runOnUiThread(() -> {
        Toast.makeText(MainActivity.this, "Import thành công! Bắt đầu học...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, FlashcardActivity.class);
        intent.putExtra(FlashcardActivity.EXTRA_SET_ID, newSetId);
        startActivity(intent);
      });
    });
  }

  private void updateEmptyView() {
    View emptyView = findViewById(R.id.empty_view);
    if (collections.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
      recyclerView.setVisibility(View.GONE);
    } else {
      emptyView.setVisibility(View.GONE);
      recyclerView.setVisibility(View.VISIBLE);
    }
  }

  private void showAddCollectionDialog() {
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_collection, null);
    EditText nameInput = dialogView.findViewById(R.id.edit_collection_name);
    EditText descInput = dialogView.findViewById(R.id.edit_collection_description);
    new AlertDialog.Builder(this)
            .setTitle("Add Collection")
            .setView(dialogView)
            .setPositiveButton("Add", (d, w) -> {
              String name = nameInput.getText().toString();
              String desc = descInput.getText().toString();
              executor.execute(() -> {
                QuizCollectionEntity entity = new QuizCollectionEntity();
                entity.name = name;
                entity.description = desc;
                db.quizCollectionDao().insert(entity);
                refreshCollections();
              });
            })
            .setNegativeButton("Cancel", null)
            .show();
  }

  private void showEditCollectionDialog(QuizCollectionEntity collection) {
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_collection, null);
    EditText nameInput = dialogView.findViewById(R.id.edit_collection_name);
    EditText descInput = dialogView.findViewById(R.id.edit_collection_description);
    nameInput.setText(collection.name);
    descInput.setText(collection.description);
    new AlertDialog.Builder(this)
            .setTitle("Edit Collection")
            .setView(dialogView)
            .setPositiveButton("Save", (d, w) -> {
              executor.execute(() -> {
                collection.name = nameInput.getText().toString();
                collection.description = descInput.getText().toString();
                collection.updatedAt = System.currentTimeMillis();
                int updated = db.quizCollectionDao().updateIfNotDefault(
                        collection.id, collection.name, collection.description, collection.updatedAt,
                        collection.archived, collection.difficulty, collection.order
                );
                if (updated == 0) {
                  runOnUiThread(() -> Toast.makeText(this, "Cannot modify the Default collection", Toast.LENGTH_SHORT).show());
                }
                refreshCollections();
              });
            })
            .setNegativeButton("Cancel", null)
            .show();
  }

  @Override
  public void onItemClick(QuizCollectionEntity collection) {
    Intent intent = new Intent(this, QuizSetListActivity.class);
    intent.putExtra("collectionId", collection.id);
    startActivity(intent);
  }


  public void onItemLongClick(QuizCollectionEntity collection) {
    String[] options = {"Edit", "Delete"};
    new AlertDialog.Builder(this)
            .setTitle(collection.name)
            .setItems(options, (dialog, which) -> {
              if (which == 0) { // Edit
                showEditCollectionDialog(collection);
              } else if (which == 1) { // Delete
                executor.execute(() -> {
                  db.quizCollectionDao().deleteIfNotDefault(collection.id);
                  refreshCollections();
                });
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
              }
            })
            .show();
  }
}