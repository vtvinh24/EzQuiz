package dev.vtvinh24.ezquiz.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class DeepLinkUtil {
  public static boolean handleDeepLink(Context context, Uri uri) {
    // Only handle ezquiz:// scheme for now
    if (uri == null || !"ezquiz".equals(uri.getScheme())) return false;
    String path = uri.getPath();
    if (path == null) return false;
    Intent intent = null;
    if (path.startsWith("/quiz/")) {
      String quizId = path.replace("/quiz/", "");
      intent = new Intent(context, dev.vtvinh24.ezquiz.ui.QuizListActivity.class);
      intent.putExtra("quiz_id", quizId);
    } else if (path.startsWith("/flashcard/")) {
      String cardId = path.replace("/flashcard/", "");
      intent = new Intent(context, dev.vtvinh24.ezquiz.ui.FlashcardActivity.class);
      intent.putExtra("flashcard_id", cardId);
    }
    if (intent != null) {
      intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      context.startActivity(intent);
      return true;
    }
    return false;
  }
}
