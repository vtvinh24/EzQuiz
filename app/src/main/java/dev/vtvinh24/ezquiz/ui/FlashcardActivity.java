package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import dev.vtvinh24.ezquiz.R;

public class FlashcardActivity extends AppCompatActivity {

  public static final String EXTRA_SET_ID = "setId"; // Dùng hằng số này khi gọi Intent

  private FlashcardViewModel viewModel;
  private ViewPager2 viewPager;
  private FlashcardAdapter adapter;
  private Button btnKnow, btnDontKnow;
  private TextView textProgress;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // QUAN TRỌNG: Đảm bảo bạn có layout này
    setContentView(R.layout.activity_flashcard_new);

    // --- 1. Lấy ID từ Intent và Log ra để kiểm tra ---
    long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
    android.util.Log.d("DEBUG_FLASHCARD", "Activity received setId: " + setId);

    if (setId == -1) {
      Toast.makeText(this, "Error: Invalid Set ID.", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    // --- 2. Khởi tạo Views ---
    viewPager = findViewById(R.id.flashcard_view_pager);
    btnKnow = findViewById(R.id.btn_know);
    btnDontKnow = findViewById(R.id.btn_dont_know);
    textProgress = findViewById(R.id.text_flashcard_progress);

    // --- 3. Setup ViewModel và Adapter ---
    viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);
    adapter = new FlashcardAdapter(this);
    viewPager.setAdapter(adapter);

    // --- 4. Kết nối UI và ViewModel ---
    setupObservers();
    setupListeners();

    // --- 5. Bắt đầu phiên học ---
    viewModel.startSession(setId);
  }

  private void setupListeners() {
    btnKnow.setOnClickListener(v -> viewModel.markAsKnown());
    btnDontKnow.setOnClickListener(v -> viewModel.markAsUnknown());

    // Cập nhật ViewModel khi người dùng vuốt thẻ
    viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
      @Override
      public void onPageSelected(int position) {
        super.onPageSelected(position);
        viewModel.onUserSwiped(position);
      }
    });
  }

  private void setupObservers() {
    // Quan sát danh sách flashcards
    viewModel.flashcards.observe(this, quizDisplayItems -> {
      if (quizDisplayItems != null && !quizDisplayItems.isEmpty()) {
        adapter.submitList(quizDisplayItems);
      } else {
        // ViewModel đã log lỗi, ở đây chỉ cần kiểm tra lại
        // để chắc chắn không submit list rỗng gây crash
        if (viewModel.flashcards.getValue() == null || viewModel.flashcards.getValue().isEmpty()) {
          Toast.makeText(this, "No flashcards found in this set.", Toast.LENGTH_LONG).show();
          finish();
        }
      }
    });

    // Quan sát vị trí thẻ hiện tại để di chuyển ViewPager
    viewModel.currentCardPosition.observe(this, position -> {
      if (position != null && viewPager.getCurrentItem() != position) {
        viewPager.setCurrentItem(position, true);
      }
    });

    // Quan sát text tiến trình
    viewModel.sessionProgressText.observe(this, progress -> {
      textProgress.setText(progress);
    });

    // Quan sát sự kiện kết thúc phiên học
    viewModel.sessionFinished.observe(this, event -> {
      if (event.getContentIfNotHandled() != null) {
        Intent intent = new Intent(this, FlashcardSummaryActivity.class);
        // Truyền kết quả dưới dạng Serializable
        intent.putExtra(FlashcardSummaryActivity.EXTRA_RESULTS, new java.util.ArrayList<>(event.peekContent()));
        startActivity(intent);
        finish(); // Đóng activity hiện tại
      }
    });
  }
}