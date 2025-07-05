package dev.vtvinh24.ezquiz.util;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class GlobalNotificationManager {
  private static final String CHANNEL_ID = "quiz_reminder_channel";
  private static final String CHANNEL_NAME = "Quiz/Flashcard Reminders";
  private static final String CHANNEL_DESC = "Reminders to practice quizzes and flashcards";
  private static GlobalNotificationManager instance;
  private final Context context;

  private GlobalNotificationManager(Context context) {
    this.context = context.getApplicationContext();
    createNotificationChannel();
  }

  public static synchronized GlobalNotificationManager getInstance(Context context) {
    if (instance == null) {
      instance = new GlobalNotificationManager(context);
    }
    return instance;
  }

  public static void showNotification(Context context, int notificationId, String title, String content) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true);
    NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    if (manager != null) {
      manager.notify(notificationId, builder.build());
    }
  }

  private void createNotificationChannel() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      NotificationChannel channel = new NotificationChannel(
              CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
      channel.setDescription(CHANNEL_DESC);
      NotificationManager manager = context.getSystemService(NotificationManager.class);
      if (manager != null) {
        manager.createNotificationChannel(channel);
      }
    }
  }

  public void scheduleReminder(long triggerAtMillis, int notificationId, String title, String content) {
    Intent intent = new Intent(context, ReminderReceiver.class);
    intent.putExtra("notification_id", notificationId);
    intent.putExtra("title", title);
    intent.putExtra("content", content);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    if (alarmManager != null) {
      alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }
  }

  public void cancelReminder(int notificationId) {
    Intent intent = new Intent(context, ReminderReceiver.class);
    PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    if (alarmManager != null) {
      alarmManager.cancel(pendingIntent);
    }
  }
}

