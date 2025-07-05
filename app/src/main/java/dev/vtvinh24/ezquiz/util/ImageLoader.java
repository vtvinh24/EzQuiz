package dev.vtvinh24.ezquiz.util;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageLoader {
  private static ImageLoader instance;
  private final Context context;

  private ImageLoader(Context context) {
    this.context = context.getApplicationContext();
  }

  public static synchronized ImageLoader getInstance(Context context) {
    if (instance == null) {
      instance = new ImageLoader(context);
    }
    return instance;
  }

  public void load(String url, ImageView imageView) {
    Glide.with(context).load(url).into(imageView);
  }

  public void load(int resId, ImageView imageView) {
    Glide.with(context).load(resId).into(imageView);
  }
}

