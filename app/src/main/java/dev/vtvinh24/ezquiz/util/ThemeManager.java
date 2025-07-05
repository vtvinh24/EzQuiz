package dev.vtvinh24.ezquiz.util;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemeManager {
  private static ThemeManager instance;
  private final Context context;

  private ThemeManager(Context context) {
    this.context = context.getApplicationContext();
  }

  public static synchronized ThemeManager getInstance(Context context) {
    if (instance == null) {
      instance = new ThemeManager(context);
    }
    return instance;
  }

  public boolean isDarkMode() {
    int mode = context.getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
    return mode == android.content.res.Configuration.UI_MODE_NIGHT_YES;
  }

  public void setDarkMode(boolean enabled) {
    AppCompatDelegate.setDefaultNightMode(
            enabled ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
    );
  }

  public void applyTheme(Activity activity) {
    activity.recreate();
  }
}

