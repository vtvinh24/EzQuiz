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

// =================== BƯỚC 1: IMPLEMENT INTERFACE MỚI ===================
public class QuizSetListActivity extends AppCompatActivity
        implements QuizSetAdapter.OnItemClickListener,
        QuizSetAdapter.OnPlayFlashcardClickListener,
        QuizSetAdapter.OnPracticeClickListener,
        QuizSetAdapter.OnTestClickListener { // <<< THÊM INTERFACE MỚI VÀO ĐÂY

  public static final String EXTRA_COLLECTION_ID = "collectionId";
  private final List<QuizSetEntity> quizSets = new ArrayList<>();
  private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
  private final java.util.concurrent.ExecutorService executor = Executors.newSingleThreadExecutor();

  // Khai báo biến ở đây
  private QuizSetRepository quizSetRepository;
  private RecyclerView recyclerView;
  private QuizSetAdapter adapter;
  private AppDatabase db;
  private long currentCollectionId;

  // Xóa bỏ constructor

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_quiz_set_list);

    // Khởi tạo các đối tượng cần Context ở đây
    db = AppDatabaseProvider.getDatabase(this);
    quizSetRepository = new QuizSetRepository(db);

    currentCollectionId = getIntent().getLongExtra(EXTRA_COLLECTION_ID, -1);
    if (currentCollectionId == -1) {
      Toast.makeText(this, "Lỗi: Không tìm thấy ID bộ sưu tập", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    recyclerView = findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    // =================== BƯỚC 2: TRUYỀN LISTENER MỚI VÀO ADAPTER ===================
    // Dòng này bây giờ đã đúng vì có đủ 6 tham số
    adapter = new QuizSetAdapter(this, quizSets, this, this, this, this);
    recyclerView.setAdapter(adapter);

    FloatingActionButton fabAdd = findViewById(R.id.fab_add_set);
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
    View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_edit_set, null);
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
                entity.collectionId = currentCollectionId;
                entity.createdAt = System.currentTimeMillis();
                entity.updatedAt = System.currentTimeMillis();
                quizSetRepository.insertWithDefaultCollectionIfNeeded(entity);
                refreshQuizSets();
              });
            })
            .setNegativeButton("Cancel", null)
            .show();
  }

  // region: Xử lý sự kiện click từ Adapter
  @Override
  public void onItemClick(QuizSetEntity quizSet) {
    Intent intent = new Intent(this, QuizListActivity.class);
    intent.putExtra("quizSetId", quizSet.id);
    startActivity(intent);
  }

  @Override
  public void onPlayFlashcardClick(long quizSetId) {
    Intent intent = new Intent(this, FlashcardActivity.class);
    intent.putExtra(FlashcardActivity.EXTRA_SET_ID, quizSetId);
    startActivity(intent);
  }

  @Override
  public void onPracticeClick(long quizSetId) {
    Intent intent = new Intent(this, PracticeActivity.class);
    intent.putExtra(PracticeActivity.EXTRA_SET_ID, quizSetId);
    startActivity(intent);
  }

  // =================== BƯỚC 3: OVERRIDE PHƯƠNG THỨC onTestClick ===================
  @Override
  public void onTestClick(long quizSetId) {
    Toast.makeText(this, "Bắt đầu Chế độ Kiểm tra cho Set ID: " + quizSetId, Toast.LENGTH_SHORT).show();

    // Chuyển sang màn hình cấu hình bài kiểm tra (TestConfigActivity)
    Intent intent = new Intent(this, TestConfigActivity.class);
    intent.putExtra("quizSetId", quizSetId); // Truyền ID của bộ câu hỏi
    startActivity(intent);
  }
  // endregion
}