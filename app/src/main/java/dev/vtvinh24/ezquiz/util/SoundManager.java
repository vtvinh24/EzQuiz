package dev.vtvinh24.ezquiz.util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Map;

public class SoundManager {
  private static SoundManager instance;
  private final SoundPool soundPool;
  private final Map<Integer, Integer> soundMap = new HashMap<>();
  private boolean loaded = false;

  private SoundManager(Context context) {
    AudioAttributes attrs = new AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build();
    soundPool = new SoundPool.Builder()
            .setMaxStreams(4)
            .setAudioAttributes(attrs)
            .build();
    loaded = true;
  }

  public static synchronized SoundManager getInstance(Context context) {
    if (instance == null) {
      instance = new SoundManager(context);
    }
    return instance;
  }

  public void loadSound(Context context, int resId) {
    int soundId = soundPool.load(context, resId, 1);
    soundMap.put(resId, soundId);
  }

  public void playSound(int resId) {
    Integer soundId = soundMap.get(resId);
    if (loaded && soundId != null) {
      soundPool.play(soundId, 1, 1, 1, 0, 1f);
    }
  }

  public void release() {
    soundPool.release();
    soundMap.clear();
    loaded = false;
  }
}

