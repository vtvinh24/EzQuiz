package dev.vtvinh24.ezquiz.util;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
  private static final String PREF_NAME = "ezquiz_prefs";
  private static PrefManager instance;
  private final SharedPreferences prefs;

  private PrefManager(Context context) {
    prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
  }

  public static synchronized PrefManager getInstance(Context context) {
    if (instance == null) {
      instance = new PrefManager(context);
    }
    return instance;
  }

  public void putString(String key, String value) {
    prefs.edit().putString(key, value).apply();
  }

  public String getString(String key, String defValue) {
    return prefs.getString(key, defValue);
  }

  public void putInt(String key, int value) {
    prefs.edit().putInt(key, value).apply();
  }

  public int getInt(String key, int defValue) {
    return prefs.getInt(key, defValue);
  }

  public void putBoolean(String key, boolean value) {
    prefs.edit().putBoolean(key, value).apply();
  }

  public boolean getBoolean(String key, boolean defValue) {
    return prefs.getBoolean(key, defValue);
  }

  public void remove(String key) {
    prefs.edit().remove(key).apply();
  }

  public void clear() {
    prefs.edit().clear().apply();
  }
}

