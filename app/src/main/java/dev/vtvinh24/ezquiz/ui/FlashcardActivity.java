package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import dev.vtvinh24.ezquiz.R;

public class FlashcardActivity extends AppCompatActivity {

  public static final String EXTRA_SET_ID = "setId";

  private FlashcardViewModel viewModel;
  private ViewPager2 viewPager;
  private FlashcardAdapter adapter;
  private Button btnKnow, btnDontKnow, btnJumpTo;
  private ImageButton btnPreviousCard, btnNextCard;

  private final ActivityResultLauncher<Intent> summaryLauncher = registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            // Xử lý kết quả từ màn hình tổng kết
            if (result.getResultCode() == FlashcardSummaryActivity.RESULT_RESTART) {
              long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
              if (setId != -1) {
                Toast.makeText(this, "Restarting session...", Toast.LENGTH_SHORT).show();
                viewModel.startSession(setId);
              }
            } else if (result.getResultCode() == FlashcardSummaryActivity.RESULT_STUDY_UNKNOWN) {
              Intent data = result.getData();
              if (data != null) {
                ArrayList<Long> unknownIds = (ArrayList<Long>) data.getSerializableExtra(FlashcardSummaryActivity.EXTRA_UNKNOWN_CARD_IDS);
                if (unknownIds != null && !unknownIds.isEmpty()) {
                  Toast.makeText(this, "Studying unknown cards...", Toast.LENGTH_SHORT).show();
                  viewModel.startSessionWithSpecificCards(unknownIds);
                } else {
                  finish();
                }
              }
            } else {
              // Mặc định (nhấn "Done" hoặc nút Back)
              finish();
            }
          });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_flashcard_new);

    long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
    if (setId == -1) {
      Toast.makeText(this, "Error: Invalid Set ID.", Toast.LENGTH_SHORT).show();
      finish();
      return;
    }

    initViews();
    initViewModel();
    setupListeners();
    setupObservers();

    viewModel.startSession(setId);
  }

  private void initViews() {
    viewPager = findViewById(R.id.flashcard_view_pager);
    btnKnow = findViewById(R.id.btn_know);
    btnDontKnow = findViewById(R.id.btn_dont_know);
    btnJumpTo = findViewById(R.id.btn_jump_to);
    btnPreviousCard = findViewById(R.id.btn_previous_card);
    btnNextCard = findViewById(R.id.btn_next_card);
  }

  private void initViewModel() {
    viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);
    adapter = new FlashcardAdapter(this);
    viewPager.setAdapter(adapter);
  }

  private void setupListeners() {
    btnKnow.setOnClickListener(v -> viewModel.markAsKnown());
    btnDontKnow.setOnClickListener(v -> viewModel.markAsUnknown());
    btnJumpTo.setOnClickListener(v -> showJumpToDialog());

    btnPreviousCard.setOnClickListener(v -> {
      int currentItem = viewPager.getCurrentItem();
      if (currentItem > 0) {
        viewPager.setCurrentItem(currentItem - 1);
      }
    });

    btnNextCard.setOnClickListener(v -> {
      int currentItem = viewPager.getCurrentItem();
      if (adapter != null && currentItem < adapter.getItemCount() - 1) {
        viewPager.setCurrentItem(currentItem + 1);
      }
    });

    viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
      @Override
      public void onPageSelected(int position) {
        super.onPageSelected(position);
        viewModel.onUserSwiped(position);
        updateNavigationButtons(position);
      }
    });
  }

  private void setupObservers() {
    viewModel.flashcards.observe(this, quizDisplayItems -> {
      if (quizDisplayItems != null && !quizDisplayItems.isEmpty()) {
        adapter.submitList(quizDisplayItems);
        updateNavigationButtons(viewPager.getCurrentItem());
      } else {
        if (viewModel.flashcards.getValue() == null || viewModel.flashcards.getValue().isEmpty()) {
          Toast.makeText(this, "No flashcards found in this set.", Toast.LENGTH_LONG).show();
          finish();
        }
      }
    });

    viewModel.currentCardPosition.observe(this, position -> {
      if (position != null && viewPager.getCurrentItem() != position) {
        viewPager.setCurrentItem(position, true);
      }
    });

    viewModel.sessionProgressText.observe(this, progress -> btnJumpTo.setText(progress));

    viewModel.sessionFinished.observe(this, event -> {
      if (event.getContentIfNotHandled() != null) {
        Intent intent = new Intent(this, FlashcardSummaryActivity.class);
        intent.putExtra(FlashcardSummaryActivity.EXTRA_RESULTS, new ArrayList<>(event.peekContent()));
        summaryLauncher.launch(intent);
      }
    });
  }

  private void updateNavigationButtons(int currentPosition) {
    if (adapter == null || adapter.getItemCount() <= 1) {
      btnPreviousCard.setVisibility(View.GONE);
      btnNextCard.setVisibility(View.GONE);
      return;
    }
    btnPreviousCard.setVisibility(currentPosition == 0 ? View.INVISIBLE : View.VISIBLE);
    btnNextCard.setVisibility(currentPosition == adapter.getItemCount() - 1 ? View.INVISIBLE : View.VISIBLE);
  }

  private void showJumpToDialog() {
    if (viewModel.flashcards.getValue() == null || viewModel.flashcards.getValue().isEmpty()) {
      Toast.makeText(this, "No cards to jump to.", Toast.LENGTH_SHORT).show();
      return;
    }

    int totalCards = Objects.requireNonNull(viewModel.flashcards.getValue()).size();
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Jump to card");

    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    input.setHint("Enter card number (1 - " + totalCards + ")");
    int padding = (int) (16 * getResources().getDisplayMetrics().density);
    input.setPadding(padding, padding, padding, padding);
    builder.setView(input);

    builder.setPositiveButton("Go", (dialog, which) -> {
      String inputText = input.getText().toString();
      if (!inputText.isEmpty()) {
        try {
          int cardNumber = Integer.parseInt(inputText);
          int position = cardNumber - 1;
          if (position >= 0 && position < totalCards) {
            viewModel.jumpToPosition(position);
          } else {
            Toast.makeText(this, "Invalid number. Please enter a value between 1 and " + totalCards, Toast.LENGTH_SHORT).show();
          }
        } catch (NumberFormatException e) {
          Toast.makeText(this, "Invalid input. Please enter a number.", Toast.LENGTH_SHORT).show();
        }
      }
    });
    builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
    builder.show();
  }
}