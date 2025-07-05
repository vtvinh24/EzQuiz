package dev.vtvinh24.ezquiz.util;

import android.content.Context;
import android.content.Intent;

public class ShareManager {
  public static void shareText(Context context, String subject, String text) {
    Intent sendIntent = new Intent(Intent.ACTION_SEND);
    sendIntent.setType("text/plain");
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
    Intent shareIntent = Intent.createChooser(sendIntent, null);
    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(shareIntent);
  }

  public static void shareFile(Context context, String subject, String text, android.net.Uri fileUri, String mimeType) {
    Intent sendIntent = new Intent(Intent.ACTION_SEND);
    sendIntent.setType(mimeType);
    sendIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
    sendIntent.putExtra(Intent.EXTRA_TEXT, text);
    sendIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
    sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
    Intent shareIntent = Intent.createChooser(sendIntent, null);
    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(shareIntent);
  }

  public static void shareDeepLink(Context context, String title, String description, String deeplink) {
    Intent sendIntent = new Intent(Intent.ACTION_SEND);
    sendIntent.setType("text/plain");
    String shareText = title + "\n" + description + "\n" + deeplink;
    sendIntent.putExtra(Intent.EXTRA_TEXT, shareText);
    Intent shareIntent = Intent.createChooser(sendIntent, null);
    shareIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    context.startActivity(shareIntent);
  }
}
