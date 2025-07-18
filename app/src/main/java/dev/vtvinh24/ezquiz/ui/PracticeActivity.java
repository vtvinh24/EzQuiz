package dev.vtvinh24.ezquiz.ui;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.button.MaterialButton;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import dev.vtvinh24.ezquiz.R;

public class PracticeActivity extends AppCompatActivity {

    public static final String EXTRA_SET_ID = "setId";

    private PracticeViewModel viewModel;
    private PracticeAdapter practiceAdapter;

    private ViewPager2 viewPager;
    private TextView textProgress;
    private TextView textProgressPercentage;
    private TextView textMotivation;
    private ProgressBar progressBar;
    private MaterialButton btnCheckAnswer, btnNextQuestion;
    private ImageButton btnBack;
    private long setId;

    private List<String> motivationalMessages = Arrays.asList(
        "Bạn đang làm rất tốt!",
        "Tiếp tục như vậy!",
        "Tuyệt vời!",
        "Bạn đang tiến bộ!",
        "Xuất sắc!",
        "Cố gắng lên!",
        "Gần đến đích rồi!",
        "Hoàn hảo!",
        "Bạn thật giỏi!",
        "Sắp hoàn thành rồi!"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_practice);

        setId = getIntent().getLongExtra(EXTRA_SET_ID, -1);
        boolean shouldContinueSession = getIntent().getBooleanExtra("continueSession", false);

        if (setId == -1) {
            Toast.makeText(this, "Lỗi: ID bộ câu hỏi không hợp lệ", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupViewModel();
        setupViewPager();
        setupClickListeners();
        setupObservers();

        if (shouldContinueSession) {
            viewModel.continueExistingSession(setId);
        } else {
            viewModel.startSession(setId);
        }
    }

    private void initializeViews() {
        textProgress = findViewById(R.id.text_practice_progress);
        textProgressPercentage = findViewById(R.id.text_progress_percentage);
        textMotivation = findViewById(R.id.text_motivation);
        progressBar = findViewById(R.id.progress_bar);
        viewPager = findViewById(R.id.view_pager_practice);
        btnCheckAnswer = findViewById(R.id.btn_check_answer);
        btnNextQuestion = findViewById(R.id.btn_next_question);
        btnBack = findViewById(R.id.btn_back);

        viewPager.setUserInputEnabled(false);

        // Initialize with motivational message
        updateMotivationalMessage();
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(PracticeViewModel.class);
    }

    private void setupViewPager() {
        practiceAdapter = new PracticeAdapter(this);
        viewPager.setAdapter(practiceAdapter);
    }

    private void setupClickListeners() {
        btnCheckAnswer.setOnClickListener(v -> viewModel.checkCurrentAnswer());
        btnNextQuestion.setOnClickListener(v -> {
            viewModel.moveToNextQuestion();
            // Add celebration animation when moving to next question
            animateProgressUpdate();
        });
        btnBack.setOnClickListener(v -> onBackPressed());
    }

    private void setupObservers() {
        viewModel.quizItems.observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                practiceAdapter.submitList(items);
                updateProgress();
            } else {
                Toast.makeText(this, "Không có câu hỏi nào để luyện tập.", Toast.LENGTH_LONG).show();
                finish();
            }
        });

        viewModel.currentQuestionPosition.observe(this, position -> {
            if (position != null) {
                viewPager.setCurrentItem(position, false);
                updateProgressWithAnimation();
                btnCheckAnswer.setVisibility(View.VISIBLE);
                btnNextQuestion.setVisibility(View.GONE);
            }
        });

        viewModel.isAnswerChecked.observe(this, isChecked -> {
            if (isChecked) {
                btnCheckAnswer.setVisibility(View.GONE);
                btnNextQuestion.setVisibility(View.VISIBLE);
                // Update motivational message when answer is checked
                updateMotivationalMessage();
            }
        });

        viewModel.canCheckAnswer.observe(this, canCheck -> {
            btnCheckAnswer.setEnabled(canCheck);
        });

        viewModel.sessionFinished.observe(this, event -> {
            if (event.getContentIfNotHandled() != null) {
                Intent intent = new Intent(this, ScoreActivity.class);
                intent.putExtra("results", (Serializable) new ArrayList<>(event.peekContent()));
                intent.putExtra("quiz_set_id", setId);
                startActivity(intent);
                finish();
            }
        });

        viewModel.hasExistingProgress.observe(this, hasProgress -> {
            if (hasProgress) {
                showResumeDialog();
            } else {
                viewModel.startNewSession(setId);
            }
        });
    }

    private void showResumeDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Tiếp tục luyện tập")
                .setMessage("Bạn có muốn tiếp tục từ vị trí đã dừng hay bắt đầu lại từ đầu?")
                .setPositiveButton("Tiếp tục", (dialog, which) -> {
                    viewModel.resumeSession(setId);
                    dialog.dismiss();
                })
                .setNegativeButton("Bắt đầu lại", (dialog, which) -> {
                    viewModel.startNewSession(setId);
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
    }

    private void updateProgress() {
        Integer position = viewModel.currentQuestionPosition.getValue();
        int total = practiceAdapter.getItemCount();

        if (position != null && total > 0) {
            int progressValue = (int) (((position + 1) * 100.0f) / total);
            progressBar.setProgress(progressValue);
            textProgress.setText(String.format("%d/%d", position + 1, total));
            textProgressPercentage.setText(String.format("%d%%", progressValue));
        }
    }

    private void updateProgressWithAnimation() {
        Integer position = viewModel.currentQuestionPosition.getValue();
        int total = practiceAdapter.getItemCount();

        if (position != null && total > 0) {
            int newProgressValue = (int) (((position + 1) * 100.0f) / total);
            int currentProgress = progressBar.getProgress();

            // Only animate if there's actual progress change
            if (newProgressValue != currentProgress) {
                // Animate progress bar with smoother interpolation
                ObjectAnimator progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", currentProgress, newProgressValue);
                progressAnimator.setDuration(1200); // Longer duration for smoother animation
                progressAnimator.setInterpolator(new AccelerateDecelerateInterpolator());

                // Add celebration bounce effect for significant progress
                if (newProgressValue > currentProgress + 10) {
                    progressAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            // Bounce effect when reaching milestones
                            progressBar.animate()
                                    .scaleY(1.15f)
                                    .setDuration(150)
                                    .withEndAction(() -> {
                                        progressBar.animate()
                                                .scaleY(1f)
                                                .setDuration(150)
                                                .start();
                                    })
                                    .start();
                        }
                    });
                }
                progressAnimator.start();

                // Animate percentage text with counter effect
                ValueAnimator percentageAnimator = ValueAnimator.ofInt(currentProgress, newProgressValue);
                percentageAnimator.setDuration(1200);
                percentageAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                percentageAnimator.addUpdateListener(animation -> {
                    int animatedValue = (int) animation.getAnimatedValue();
                    textProgressPercentage.setText(String.format("%d%%", animatedValue));
                });
                percentageAnimator.start();

                // Add pulse animation to percentage text for emphasis
                textProgressPercentage.animate()
                        .scaleX(1.3f)
                        .scaleY(1.3f)
                        .setDuration(300)
                        .withEndAction(() -> {
                            textProgressPercentage.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(300)
                                    .start();
                        })
                        .start();
            }

            // Update progress text with scale animation
            textProgress.setText(String.format("%d/%d", position + 1, total));
            textProgress.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        textProgress.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(200)
                                .start();
                    })
                    .start();
        }
    }

    private void animateProgressUpdate() {
        // Add a bounce animation to the progress bar
        progressBar.animate()
                .scaleY(1.3f)
                .setDuration(200)
                .withEndAction(() -> {
                    progressBar.animate()
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                })
                .start();
    }

    private void updateMotivationalMessage() {
        Random random = new Random();
        String message = motivationalMessages.get(random.nextInt(motivationalMessages.size()));
        textMotivation.setText(message);

        // Add fade in animation
        textMotivation.setAlpha(0f);
        textMotivation.animate()
                .alpha(1f)
                .setDuration(500)
                .start();
    }

    @Override
    public void onBackPressed() {
        if (viewModel != null) {
            viewModel.saveProgress();
        }

        new AlertDialog.Builder(this)
                .setTitle("Thoát luyện tập")
                .setMessage("Bạn có chắc muốn thoát? Tiến độ đã được lưu lại.")
                .setPositiveButton("Thoát", (dialog, which) -> {
                    super.onBackPressed();
                })
                .setNegativeButton("Tiếp tục", (dialog, which) -> dialog.dismiss())
                .setOnCancelListener(dialog -> {
                    // If user cancels dialog, don't exit
                })
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (viewModel != null) {
            viewModel.saveProgress();
        }
    }
}