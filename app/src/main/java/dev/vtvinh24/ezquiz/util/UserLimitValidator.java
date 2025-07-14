package dev.vtvinh24.ezquiz.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import dev.vtvinh24.ezquiz.data.entity.UserEntity;

public class UserLimitValidator {
    private static final String TAG = "UserLimitValidator";
    private static final String PREFS_NAME = "user_limits_prefs";
    private static final String KEY_LAST_REQUEST_TIME = "last_request_time";
    private static final String KEY_DAILY_QUIZ_COUNT = "daily_quiz_count";
    private static final String KEY_LAST_RESET_DATE = "last_reset_date";
    private static final String KEY_SESSION_IMAGE_COUNT = "session_image_count";
    private static final String KEY_SESSION_START_TIME = "session_start_time";

    private final SharedPreferences prefs;
    private final SimpleDateFormat dateFormat;

    public UserLimitValidator(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public ValidationResult validateQuizGeneration(UserEntity user, int requestedQuestions, boolean hasImage) {
        if (user == null) {
            return new ValidationResult(false, "Vui lòng đăng nhập để sử dụng tính năng này.");
        }

        // Check rate limiting
        ValidationResult rateLimitResult = checkRateLimit(user.isPremium());
        if (!rateLimitResult.isValid) {
            return rateLimitResult;
        }

        // Check daily limits for free users
        if (!user.isPremium()) {
            ValidationResult dailyLimitResult = checkDailyLimit();
            if (!dailyLimitResult.isValid) {
                return dailyLimitResult;
            }
        }

        // Check question count limits
        ValidationResult questionLimitResult = checkQuestionLimit(user.isPremium(), requestedQuestions);
        if (!questionLimitResult.isValid) {
            return questionLimitResult;
        }

        // Check image limits
        if (hasImage) {
            ValidationResult imageLimitResult = checkImageLimit(user.isPremium());
            if (!imageLimitResult.isValid) {
                return imageLimitResult;
            }
        }

        return new ValidationResult(true, "Validation passed");
    }

    private ValidationResult checkRateLimit(boolean isPremium) {
        long currentTime = System.currentTimeMillis();
        long lastRequestTime = prefs.getLong(KEY_LAST_REQUEST_TIME, 0);

        long minInterval = isPremium ?
            UserLimits.PREMIUM_MIN_REQUEST_INTERVAL_MS :
            UserLimits.FREE_MIN_REQUEST_INTERVAL_MS;

        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (timeSinceLastRequest < minInterval) {
            long waitTime = (minInterval - timeSinceLastRequest) / 1000;
            return new ValidationResult(false,
                String.format(UserLimits.ERROR_REQUEST_TOO_FREQUENT, waitTime));
        }

        return new ValidationResult(true, "Rate limit passed");
    }

    private ValidationResult checkDailyLimit() {
        String today = dateFormat.format(new Date());
        String lastResetDate = prefs.getString(KEY_LAST_RESET_DATE, "");

        int dailyCount = prefs.getInt(KEY_DAILY_QUIZ_COUNT, 0);

        // Reset counter if it's a new day
        if (!today.equals(lastResetDate)) {
            dailyCount = 0;
            prefs.edit()
                .putString(KEY_LAST_RESET_DATE, today)
                .putInt(KEY_DAILY_QUIZ_COUNT, 0)
                .apply();
        }

        if (dailyCount >= UserLimits.FREE_MAX_QUIZ_SETS_PER_DAY) {
            return new ValidationResult(false, UserLimits.ERROR_DAILY_LIMIT_EXCEEDED);
        }

        return new ValidationResult(true, "Daily limit passed");
    }

    private ValidationResult checkQuestionLimit(boolean isPremium, int requestedQuestions) {
        int maxQuestions = isPremium ?
            UserLimits.PREMIUM_MAX_QUIZ_QUESTIONS :
            UserLimits.FREE_MAX_QUIZ_QUESTIONS;

        if (requestedQuestions > maxQuestions) {
            return new ValidationResult(false, UserLimits.ERROR_QUIZ_LIMIT_EXCEEDED);
        }

        return new ValidationResult(true, "Question limit passed");
    }

    private ValidationResult checkImageLimit(boolean isPremium) {
        if (isPremium) {
            return new ValidationResult(true, "Premium user - no image limit");
        }

        // Check if this is a new session (24 hours)
        long currentTime = System.currentTimeMillis();
        long sessionStartTime = prefs.getLong(KEY_SESSION_START_TIME, 0);
        long sessionDuration = 24 * 60 * 60 * 1000; // 24 hours in milliseconds

        int sessionImageCount = prefs.getInt(KEY_SESSION_IMAGE_COUNT, 0);

        // Reset session if 24 hours have passed
        if (currentTime - sessionStartTime > sessionDuration) {
            sessionImageCount = 0;
            prefs.edit()
                .putLong(KEY_SESSION_START_TIME, currentTime)
                .putInt(KEY_SESSION_IMAGE_COUNT, 0)
                .apply();
        }

        if (sessionImageCount >= UserLimits.FREE_MAX_IMAGES_PER_SESSION) {
            return new ValidationResult(false, UserLimits.ERROR_IMAGE_LIMIT_EXCEEDED);
        }

        return new ValidationResult(true, "Image limit passed");
    }

    public void recordSuccessfulRequest(boolean hasImage) {
        SharedPreferences.Editor editor = prefs.edit();

        // Update last request time
        editor.putLong(KEY_LAST_REQUEST_TIME, System.currentTimeMillis());

        // Update daily count
        String today = dateFormat.format(new Date());
        String lastResetDate = prefs.getString(KEY_LAST_RESET_DATE, "");

        if (!today.equals(lastResetDate)) {
            editor.putString(KEY_LAST_RESET_DATE, today);
            editor.putInt(KEY_DAILY_QUIZ_COUNT, 1);
        } else {
            int currentCount = prefs.getInt(KEY_DAILY_QUIZ_COUNT, 0);
            editor.putInt(KEY_DAILY_QUIZ_COUNT, currentCount + 1);
        }

        // Update image count if applicable
        if (hasImage) {
            long currentTime = System.currentTimeMillis();
            long sessionStartTime = prefs.getLong(KEY_SESSION_START_TIME, currentTime);

            if (sessionStartTime == 0) {
                editor.putLong(KEY_SESSION_START_TIME, currentTime);
            }

            int currentImageCount = prefs.getInt(KEY_SESSION_IMAGE_COUNT, 0);
            editor.putInt(KEY_SESSION_IMAGE_COUNT, currentImageCount + 1);
        }

        editor.apply();
    }

    public void resetLimitsForPremiumUpgrade() {
        // Clear all limits when user upgrades to premium
        prefs.edit().clear().apply();
    }

    public LimitStatus getCurrentLimitStatus(boolean isPremium) {
        String today = dateFormat.format(new Date());
        String lastResetDate = prefs.getString(KEY_LAST_RESET_DATE, "");

        int dailyCount = prefs.getInt(KEY_DAILY_QUIZ_COUNT, 0);
        if (!today.equals(lastResetDate)) {
            dailyCount = 0;
        }

        int sessionImageCount = prefs.getInt(KEY_SESSION_IMAGE_COUNT, 0);
        long sessionStartTime = prefs.getLong(KEY_SESSION_START_TIME, 0);

        // Reset image count if session expired
        if (System.currentTimeMillis() - sessionStartTime > 24 * 60 * 60 * 1000) {
            sessionImageCount = 0;
        }

        return new LimitStatus(dailyCount, sessionImageCount, isPremium);
    }

    public static class ValidationResult {
        public final boolean isValid;
        public final String message;

        public ValidationResult(boolean isValid, String message) {
            this.isValid = isValid;
            this.message = message;
        }
    }

    public static class LimitStatus {
        public final int dailyQuizCount;
        public final int sessionImageCount;
        public final boolean isPremium;
        public final int remainingDailyQuizzes;
        public final int remainingSessionImages;

        public LimitStatus(int dailyQuizCount, int sessionImageCount, boolean isPremium) {
            this.dailyQuizCount = dailyQuizCount;
            this.sessionImageCount = sessionImageCount;
            this.isPremium = isPremium;

            if (isPremium) {
                this.remainingDailyQuizzes = -1; // Unlimited
                this.remainingSessionImages = -1; // Unlimited
            } else {
                this.remainingDailyQuizzes = Math.max(0, UserLimits.FREE_MAX_QUIZ_SETS_PER_DAY - dailyQuizCount);
                this.remainingSessionImages = Math.max(0, UserLimits.FREE_MAX_IMAGES_PER_SESSION - sessionImageCount);
            }
        }
    }
}
