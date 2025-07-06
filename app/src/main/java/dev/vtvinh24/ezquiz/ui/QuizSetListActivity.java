package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;
import dev.vtvinh24.ezquiz.data.repo.QuizSetRepository;

// Thêm implements QuizSetAdapter.OnPlayFlashcardClickListener
public class QuizSetListActivity extends AppCompatActivity
        implements QuizSetAdapter.OnItemClickListener, QuizSetAdapter.OnPlayFlashcardClickListener, QuizSetAdapter.OnPracticeClickListener {

  public static final String EXTRA_COLLECTION_ID = "collectionId"; // Hằng số cho ID bộ sưu tập
  private final List<QuizSetEntity> quizSets = new ArrayList<>(); // Đổi từ collections sang quizSets
  private final QuizSetRepository quizSetRepository; // Sử dụng repository
  private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
  private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();
  private RecyclerView recyclerView;
  private QuizSetAdapter adapter;
  private AppDatabase db;
  private long currentCollectionId; // Để biết đang ở bộ sưu tập nào

  public QuizSetListActivity() {
    // Khởi tạo repository ở đây hoặc thông qua DI
    db = AppDatabaseProvider.getDatabase(this); // Sẽ được truyền context từ Application
    quizSetRepository = new QuizSetRepository(db);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_quiz_set_list); // Đảm bảo đúng layout

    currentCollectionId = getIntent().getLongExtra(EXTRA_COLLECTION_ID, -1);
    if (currentCollectionId == -1) {
      Toast.makeText(this, "Lỗi: Không tìm thấy ID bộ sưu tập", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    recyclerView = findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    // Updated constructor call with all three listeners
    adapter = new QuizSetAdapter(this, quizSets, this, this, this);
    recyclerView.setAdapter(adapter);

    FloatingActionButton fabAdd = findViewById(R.id.fab_add_set); // Đảm bảo đúng ID FAB
    fabAdd.setOnClickListener(v -> showAddSetDialog());

    refreshQuizSets();
  }

  private void refreshQuizSets() {
    executor.execute(() -> {
      final List<QuizSetEntity> newSets = db.quizSetDao().getByCollectionId(currentCollectionId);
      mainThreadHandler.post(() -> {
        quizSets.clear();
        quizSets.addAll(newSets);
        adapter.notifyDataSetChanged();
        updateEmptyView();
      });
    });
  }

  private void updateEmptyView() {
    View emptyView = findViewById(R.id.empty_view);
    if (quizSets.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
      recyclerView.setVisibility(View.GONE);
    } else {
      emptyView.setVisibility(View.GONE);
      recyclerView.setVisibility(View.VISIBLE);
    }
  }

  private void showAddSetDialog() {
    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_set, null); // Tạo layout này
    EditText nameInput = dialogView.findViewById(R.id.edit_set_name);
    EditText descInput = dialogView.findViewById(R.id.edit_set_description);

    new AlertDialog.Builder(this)
            .setTitle("Add Quiz Set")
            .setView(dialogView)
            .setPositiveButton("Add", (d, w) -> {
              String name = nameInput.getText().toString();
              String desc = descInput.getText().toString();
              executor.execute(() -> {
                QuizSetEntity entity = new QuizSetEntity();
                entity.name = name;
                entity.description = desc;
                entity.collectionId = currentCollectionId; // Gán ID bộ sưu tập hiện tại
                entity.createdAt = System.currentTimeMillis();
                entity.updatedAt = System.currentTimeMillis();
                quizSetRepository.insertWithDefaultCollectionIfNeeded(entity); // Sử dụng repository
                refreshQuizSets();
              });
            })
            .setNegativeButton("Cancel", null)
            .show();
  }

  // region: Xử lý sự kiện click từ Adapter
  @Override
  public void onItemClick(QuizSetEntity quizSet) {
    // Xử lý khi nhấn vào toàn bộ item (ví dụ: mở danh sách các Quiz trong Set)
    Intent intent = new Intent(this, QuizListActivity.class);
    intent.putExtra("quizSetId", quizSet.id);
    startActivity(intent);
  }

  @Override
  public void onPlayFlashcardClick(long quizSetId) {
    // NEW: Xử lý khi nhấn nút "Play Flashcards"
    Intent intent = new Intent(this, FlashcardActivity.class);
    intent.putExtra(FlashcardActivity.EXTRA_SET_ID, quizSetId); // Sử dụng EXTRA_SET_ID từ FlashcardActivity
    startActivity(intent);
  }

  @Override
  public void onPracticeClick(long quizSetId) {
    Intent intent = new Intent(this, PracticeActivity.class);
    intent.putExtra("quiz_set_id", quizSetId);
    startActivity(intent);
  }

  // endregion

  // Bạn có thể thêm các phương thức showEditSetDialog, onItemLongClick tương tự như MainActivity
  // để quản lý các Quiz Set.
}