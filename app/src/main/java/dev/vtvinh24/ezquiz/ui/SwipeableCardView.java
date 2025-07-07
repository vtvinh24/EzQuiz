package dev.vtvinh24.ezquiz.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

public class SwipeableCardView extends FrameLayout {
    private static final float SWIPE_THRESHOLD = 0.25f;
    private static final float MIN_SWIPE_DISTANCE = 50f;

    private Paint borderPaint;
    private Paint textPaint;
    private Paint backgroundPaint;
    private RectF borderRect;

    private float currentTranslationX = 0f;
    private boolean isSwipeInProgress = false;
    private boolean isDragging = false;
    private float initialX, initialY; // Add initial touch coordinates

    public interface SwipeListener {
        void onSwipeLeft();
        void onSwipeRight();
    }

    private SwipeListener swipeListener;

    public SwipeableCardView(Context context) {
        super(context);
        init();
    }

    public SwipeableCardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SwipeableCardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(6f);

        textPaint = new Paint();
        textPaint.setAntiAlias(true);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(48f);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);

        backgroundPaint = new Paint();
        backgroundPaint.setAntiAlias(true);
        backgroundPaint.setStyle(Paint.Style.FILL);

        borderRect = new RectF();
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isDragging && Math.abs(currentTranslationX) > MIN_SWIPE_DISTANCE) {
            float progress = Math.min(1f, Math.abs(currentTranslationX) / (getWidth() * SWIPE_THRESHOLD));

            // Draw full border around the card
            borderRect.set(0, 0, getWidth(), getHeight());

            String text;
            int borderColor;
            int backgroundColor;
            int textColor;

            if (currentTranslationX > 0) {
                // Swipe right - Know (Green)
                borderColor = Color.rgb(76, 175, 80);
                backgroundColor = Color.argb((int)(120 * progress), 76, 175, 80);
                textColor = Color.rgb(76, 175, 80);
                text = "BIẾT RỒI";
            } else {
                // Swipe left - Don't Know (Orange)
                borderColor = Color.rgb(255, 152, 0);
                backgroundColor = Color.argb((int)(120 * progress), 255, 152, 0);
                textColor = Color.rgb(255, 152, 0);
                text = "CHƯA BIẾT";
            }

            // Draw background overlay
            backgroundPaint.setColor(backgroundColor);
            canvas.drawRoundRect(borderRect, 16, 16, backgroundPaint);

            // Draw complete border
            borderPaint.setColor(Color.argb((int)(255 * progress), Color.red(borderColor), Color.green(borderColor), Color.blue(borderColor)));
            canvas.drawRoundRect(borderRect, 16, 16, borderPaint);

            // Draw large text covering the card content
            textPaint.setColor(Color.argb((int)(255 * progress), Color.red(textColor), Color.green(textColor), Color.blue(textColor)));
            textPaint.setTextSize(64f); // Larger text size
            float textY = getHeight() / 2f + textPaint.getTextSize() / 3f;
            canvas.drawText(text, getWidth() / 2f, textY, textPaint);

            // Hide child views when dragging
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.setAlpha(1f - progress);
            }
        } else {
            // Show child views normally
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                child.setAlpha(1f);
            }
        }

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialX = event.getRawX();
                initialY = event.getRawY();
                isDragging = false;
                // Call super to allow child views to receive the touch
                super.onTouchEvent(event);
                return true;

            case MotionEvent.ACTION_MOVE:
                float deltaX = Math.abs(event.getRawX() - initialX);
                float deltaY = Math.abs(event.getRawY() - initialY);

                // Only start dragging if horizontal movement is significant
                if (deltaX > 50 && deltaX > deltaY * 2) {
                    if (!isDragging) {
                        isDragging = true;
                        getParent().requestDisallowInterceptTouchEvent(true);
                    }

                    float newTranslationX = event.getRawX() - initialX;
                    float maxTranslation = getWidth() * 0.7f;
                    newTranslationX = Math.max(-maxTranslation, Math.min(maxTranslation, newTranslationX));

                    setTranslationX(newTranslationX);
                    currentTranslationX = newTranslationX;
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                getParent().requestDisallowInterceptTouchEvent(false);

                if (isDragging) {
                    float swipeThreshold = getWidth() * SWIPE_THRESHOLD;

                    if (Math.abs(currentTranslationX) > swipeThreshold) {
                        if (currentTranslationX > 0) {
                            animateSwipeRight();
                        } else {
                            animateSwipeLeft();
                        }
                    } else {
                        animateReset();
                    }
                    isDragging = false;
                    return true;
                } else {
                    // This is a tap - pass to super for child views to handle
                    isDragging = false;
                    return super.onTouchEvent(event);
                }
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Don't intercept, let child views handle their own touch events
        return false;
    }

    private void animateSwipeLeft() {
        isSwipeInProgress = true;
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "translationX", currentTranslationX, -getWidth() * 1.2f);
        animator.setDuration(250);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (swipeListener != null) {
                    swipeListener.onSwipeLeft();
                }
                resetCard();
                isSwipeInProgress = false;
            }
        });
        animator.start();
    }

    private void animateSwipeRight() {
        isSwipeInProgress = true;
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "translationX", currentTranslationX, getWidth() * 1.2f);
        animator.setDuration(250);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (swipeListener != null) {
                    swipeListener.onSwipeRight();
                }
                resetCard();
                isSwipeInProgress = false;
            }
        });
        animator.start();
    }

    private void animateReset() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(this, "translationX", currentTranslationX, 0);
        animator.setDuration(200);
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                resetCard();
            }
        });
        animator.start();
    }

    private void resetCard() {
        currentTranslationX = 0;
        setTranslationX(0);
        isDragging = false;
        invalidate();
    }

    public void setSwipeListener(SwipeListener listener) {
        this.swipeListener = listener;
    }
}
