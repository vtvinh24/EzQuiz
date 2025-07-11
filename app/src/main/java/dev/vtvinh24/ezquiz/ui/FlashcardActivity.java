package dev.vtvinh24.ezquiz.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Objects;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.ui.FlashcardViewModel.QuizDisplayItem;

public class FlashcardActivity extends AppCompatActivity {

  public static final String EXTRA_SET_ID = "setId";

  private FlashcardViewModel viewModel;
  private SwipeableCardView swipeableCard;
  private TextView tvQuestion, tvAnswer, tvTapToReveal, tvCardCounter;
  private View cardFront, cardBack;
  private ProgressBar progressBar;
  private TextView btnKnow, btnDontKnow;
  private Button btnJumpTo;
  private ImageButton btnPreviousCard, btnNextCard;

  private QuizDisplayItem currentCard;
  private boolean isAnswerRevealed = false;

  private final ActivityResultLauncher<Intent> summaryLauncher = registerForActivityResult(
          new ActivityResultContracts.StartActivityForResult(),
          result -> {
            if (result.getResultCode() == FlashcardSummaryActivity.RESULT_RESTART) {
              long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
              if (setId != -1) {
                Toast.makeText(this, getString(R.string.toast_restarting_session), Toast.LENGTH_SHORT).show();
                viewModel.startSession(setId);
              }
            } else if (result.getResultCode() == FlashcardSummaryActivity.RESULT_STUDY_UNKNOWN) {
              Intent data = result.getData();
              if (data != null) {
                ArrayList<Long> unknownIds = (ArrayList<Long>) data.getSerializableExtra(FlashcardSummaryActivity.EXTRA_UNKNOWN_CARD_IDS);
                if (unknownIds != null && !unknownIds.isEmpty()) {
                  Toast.makeText(this, getString(R.string.toast_studying_unknown), Toast.LENGTH_SHORT).show();
                  viewModel.startSessionWithSpecificCards(unknownIds);
                } else {
                  finish();
                }
              }
            } else {
              finish();
            }
          });

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_flashcard);

    long setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
    if (setId == -1) {
      Toast.makeText(this, getString(R.string.error_invalid_set_id), Toast.LENGTH_SHORT).show();
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
    swipeableCard = findViewById(R.id.swipeable_card);
    tvQuestion = findViewById(R.id.tv_question);
    tvAnswer = findViewById(R.id.tv_answer);
    tvTapToReveal = findViewById(R.id.tv_tap_to_reveal);
    tvCardCounter = findViewById(R.id.tv_card_counter);

    cardFront = findViewById(R.id.card_front);
    cardBack = findViewById(R.id.card_back);

    progressBar = findViewById(R.id.progress_bar);
    btnKnow = findViewById(R.id.btn_know);
    btnDontKnow = findViewById(R.id.btn_dont_know);
    btnJumpTo = findViewById(R.id.btn_jump_to);
    btnPreviousCard = findViewById(R.id.btn_previous_card);
    btnNextCard = findViewById(R.id.btn_next_card);

    setupEntryAnimations();
  }

  private void setupEntryAnimations() {
    progressBar.setTranslationY(-100f);
    progressBar.animate()
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(new DecelerateInterpolator())
            .start();

    tvCardCounter.setTranslationX(200f);
    tvCardCounter.setAlpha(0f);
    tvCardCounter.animate()
            .translationX(0f)
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(200)
            .setInterpolator(new OvershootInterpolator(1.2f))
            .start();

    btnPreviousCard.setScaleX(0f);
    btnPreviousCard.setScaleY(0f);
    btnNextCard.setScaleX(0f);
    btnNextCard.setScaleY(0f);

    btnPreviousCard.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(400)
            .setInterpolator(new OvershootInterpolator(1.5f))
            .start();

    btnNextCard.animate()
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(500)
            .setStartDelay(500)
            .setInterpolator(new OvershootInterpolator(1.5f))
            .start();

    btnDontKnow.setTranslationY(100f);
    btnKnow.setTranslationY(100f);
    btnDontKnow.setAlpha(0f);
    btnKnow.setAlpha(0f);

    btnDontKnow.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(600)
            .setInterpolator(new DecelerateInterpolator())
            .start();

    btnKnow.animate()
            .translationY(0f)
            .alpha(1f)
            .setDuration(600)
            .setStartDelay(700)
            .setInterpolator(new DecelerateInterpolator())
            .start();
  }

  private void initViewModel() {
    viewModel = new ViewModelProvider(this).get(FlashcardViewModel.class);
  }

  private void setupListeners() {
    swipeableCard.setSwipeListener(new SwipeableCardView.SwipeListener() {
      @Override
      public void onSwipeLeft() {
        animateActionFeedback(false);
        viewModel.markAsUnknown();
      }

      @Override
      public void onSwipeRight() {
        animateActionFeedback(true);
        viewModel.markAsKnown();
      }
    });

    swipeableCard.setOnClickListener(v -> toggleAnswerWithAnimation());

    btnKnow.setOnClickListener(v -> {
      animateActionFeedback(true);
      viewModel.markAsKnown();
    });

    btnDontKnow.setOnClickListener(v -> {
      animateActionFeedback(false);
      viewModel.markAsUnknown();
    });

    btnJumpTo.setOnClickListener(v -> showJumpToDialog());

    btnPreviousCard.setOnClickListener(v -> {
      animateNavigationButton(btnPreviousCard);
      viewModel.goToPreviousCard();
    });

    btnNextCard.setOnClickListener(v -> {
      animateNavigationButton(btnNextCard);
      viewModel.goToNextCard();
    });
  }

  private void setupObservers() {
    viewModel.flashcards.observe(this, quizDisplayItems -> {
      if (quizDisplayItems == null || quizDisplayItems.isEmpty()) {
        if (viewModel.flashcards.getValue() == null || viewModel.flashcards.getValue().isEmpty()) {
          Toast.makeText(this, getString(R.string.toast_no_flashcards_in_set), Toast.LENGTH_LONG).show();
          finish();
        }
      } else {
        updateNavigationButtons();
      }
    });

    viewModel.currentCardPosition.observe(this, position -> {
      if (position != null && viewModel.flashcards.getValue() != null
              && position < viewModel.flashcards.getValue().size()) {
        currentCard = viewModel.flashcards.getValue().get(position);
        displayCard(currentCard);
        updateNavigationButtons();
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

  private void displayCard(QuizDisplayItem card) {
    if (card != null && card.quiz != null) {
      tvQuestion.setText(card.quiz.getQuestion());
      tvAnswer.setText(card.quiz.getAnswer());
      hideAnswer();
    }
  }

  private void toggleAnswerWithAnimation() {
    if (isAnswerRevealed) {
      hideAnswerWithAnimation();
    } else {
      showAnswerWithAnimation();
    }
  }

  private void showAnswerWithAnimation() {
    cardFront.animate()
            .rotationY(90f)
            .setDuration(150)
            .withEndAction(() -> {
              cardFront.setVisibility(View.GONE);
              cardBack.setVisibility(View.VISIBLE);
              cardBack.setRotationY(-90f);
              cardBack.animate()
                      .rotationY(0f)
                      .setDuration(150)
                      .start();
            })
            .start();

    isAnswerRevealed = true;
  }

  private void hideAnswerWithAnimation() {
    cardBack.animate()
            .rotationY(90f)
            .setDuration(150)
            .withEndAction(() -> {
              cardBack.setVisibility(View.GONE);
              cardFront.setVisibility(View.VISIBLE);
              cardFront.setRotationY(-90f);
              cardFront.animate()
                      .rotationY(0f)
                      .setDuration(150)
                      .start();
            })
            .start();

    isAnswerRevealed = false;
  }

  private void hideAnswer() {
    cardBack.setVisibility(View.GONE);
    cardFront.setVisibility(View.VISIBLE);
    isAnswerRevealed = false;
  }

  private void updateNavigationButtons() {
    if (viewModel.flashcards.getValue() == null || viewModel.flashcards.getValue().size() <= 1) {
      btnPreviousCard.setVisibility(View.GONE);
      btnNextCard.setVisibility(View.GONE);
      return;
    }

    Integer currentPos = viewModel.currentCardPosition.getValue();
    if (currentPos != null) {
      int totalCards = viewModel.flashcards.getValue().size();
      btnPreviousCard.setVisibility(currentPos == 0 ? View.INVISIBLE : View.VISIBLE);
      btnNextCard.setVisibility(currentPos == totalCards - 1 ? View.INVISIBLE : View.VISIBLE);
      updateProgressAndCounter(currentPos, totalCards);
    }
  }

  private void updateProgressAndCounter(int currentPosition, int totalCards) {
    int newProgress = (int) (((float) (currentPosition + 1) / totalCards) * 100);

    ObjectAnimator progressAnimator =
            ObjectAnimator.ofInt(progressBar, "progress", progressBar.getProgress(), newProgress);
    progressAnimator.setDuration(400);
    progressAnimator.setInterpolator(new DecelerateInterpolator());
    progressAnimator.start();

    tvCardCounter.animate()
            .scaleX(1.2f)
            .scaleY(1.2f)
            .setDuration(150)
            .withEndAction(() -> {
              tvCardCounter.setText(String.format("%d / %d", currentPosition + 1, totalCards));
              tvCardCounter.animate()
                      .scaleX(1f)
                      .scaleY(1f)
                      .setDuration(150)
                      .setInterpolator(new OvershootInterpolator())
                      .start();
            })
            .start();
  }

  private void showJumpToDialog() {
    if (viewModel.flashcards.getValue() == null || viewModel.flashcards.getValue().isEmpty()) {
      Toast.makeText(this, getString(R.string.toast_no_cards_to_jump_to), Toast.LENGTH_SHORT).show();
      return;
    }

    int totalCards = Objects.requireNonNull(viewModel.flashcards.getValue()).size();
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.dialog_title_jump_to_card);

    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_NUMBER);
    input.setHint(getString(R.string.dialog_hint_jump_to_card, totalCards));
    int padding = (int) (16 * getResources().getDisplayMetrics().density);
    input.setPadding(padding, padding, padding, padding);
    builder.setView(input);

    builder.setPositiveButton(R.string.dialog_btn_go, (dialog, which) -> {
      String inputText = input.getText().toString();
      if (!inputText.isEmpty()) {
        try {
          int cardNumber = Integer.parseInt(inputText);
          int position = cardNumber - 1;
          if (position >= 0 && position < totalCards) {
            viewModel.jumpToPosition(position);
          } else {
            Toast.makeText(this, getString(R.string.toast_error_invalid_card_number, totalCards), Toast.LENGTH_SHORT).show();
          }
        } catch (NumberFormatException e) {
          Toast.makeText(this, getString(R.string.toast_error_invalid_input_number), Toast.LENGTH_SHORT).show();
        }
      }
    });
    builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());
    builder.show();
  }

  private void animateActionFeedback(boolean isKnow) {
    TextView targetAction = isKnow ? btnKnow : btnDontKnow;
    targetAction.animate()
            .alpha(0.8f)
            .setDuration(80)
            .withEndAction(() -> targetAction.animate()
                    .alpha(1f)
                    .setDuration(80)
                    .start())
            .start();
  }

  private void animateNavigationButton(ImageButton button) {
    button.animate()
            .scaleX(0.8f)
            .scaleY(0.8f)
            .setDuration(100)
            .withEndAction(() -> button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(200)
                    .setInterpolator(new OvershootInterpolator())
                    .start())
            .start();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (progressBar != null) progressBar.clearAnimation();
    if (tvCardCounter != null) tvCardCounter.clearAnimation();
    if (btnPreviousCard != null) btnPreviousCard.clearAnimation();
    if (btnNextCard != null) btnNextCard.clearAnimation();
    if (btnKnow != null) btnKnow.clearAnimation();
    if (btnDontKnow != null) btnDontKnow.clearAnimation();
    if (cardFront != null) cardFront.clearAnimation();
    if (cardBack != null) cardBack.clearAnimation();
  }
}