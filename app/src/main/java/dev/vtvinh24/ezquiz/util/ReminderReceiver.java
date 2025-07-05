package dev.vtvinh24.ezquiz.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    int notificationId = intent.getIntExtra("notification_id", 0);
    String title = intent.getStringExtra("title");
    String content = intent.getStringExtra("content");
    GlobalNotificationManager.showNotification(context, notificationId, title, content);
  }
}

