package dev.vtvinh24.ezquiz.util;

import android.content.Context;
import android.content.SharedPreferences;

public class UsageTracker {
    private static final String PREF_NAME = "EzQuizUsage";
    private static final String KEY_PROMPT_COUNT = "prompt_count";
    private static final String KEY_IMAGE_COUNT = "image_count";
    private static final String KEY_LAST_RESET_DATE = "last_reset_date";

    // Free user limits
    public static final int FREE_PROMPT_LIMIT = 10;
    public static final int FREE_IMAGE_LIMIT = 3;

    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    public UsageTracker(Context context) {
        preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();

        // Reset daily counters if needed
        checkAndResetDailyLimits();
    }

    public boolean canUsePrompt(boolean isPremium) {
        if (isPremium) return true;
        return getPromptCount() < FREE_PROMPT_LIMIT;
    }

    public boolean canUseImage(boolean isPremium) {
        if (isPremium) return true;
        return getImageCount() < FREE_IMAGE_LIMIT;
    }

    public void incrementPromptUsage() {
        int currentCount = getPromptCount();
        editor.putInt(KEY_PROMPT_COUNT, currentCount + 1);
        editor.apply();
    }

    public void incrementImageUsage() {
        int currentCount = getImageCount();
        editor.putInt(KEY_IMAGE_COUNT, currentCount + 1);
        editor.apply();
    }

    public int getPromptCount() {
        return preferences.getInt(KEY_PROMPT_COUNT, 0);
    }

    public int getImageCount() {
        return preferences.getInt(KEY_IMAGE_COUNT, 0);
    }

    public int getRemainingPrompts(boolean isPremium) {
        if (isPremium) return -1; // Unlimited
        return Math.max(0, FREE_PROMPT_LIMIT - getPromptCount());
    }

    public int getRemainingImages(boolean isPremium) {
        if (isPremium) return -1; // Unlimited
        return Math.max(0, FREE_IMAGE_LIMIT - getImageCount());
    }

    public String getUsageStatus(boolean isPremium) {
        if (isPremium) {
            return "Premium Active • Unlimited access";
        } else {
            return String.format("Free Plan • %d prompts, %d images remaining",
                getRemainingPrompts(false), getRemainingImages(false));
        }
    }

    private void checkAndResetDailyLimits() {
        long today = System.currentTimeMillis() / (24 * 60 * 60 * 1000); // Days since epoch
        long lastReset = preferences.getLong(KEY_LAST_RESET_DATE, 0);

        if (today > lastReset) {
            // Reset daily counters
            editor.putInt(KEY_PROMPT_COUNT, 0);
            editor.putInt(KEY_IMAGE_COUNT, 0);
            editor.putLong(KEY_LAST_RESET_DATE, today);
            editor.apply();
        }
    }

    public void resetUsage() {
        editor.putInt(KEY_PROMPT_COUNT, 0);
        editor.putInt(KEY_IMAGE_COUNT, 0);
        editor.apply();
    }
}
