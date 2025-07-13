package dev.vtvinh24.ezquiz.util;

import android.content.Context;
import android.util.Log;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

import java.util.List;

public class DatabaseDebugHelper {
    private static final String TAG = "DatabaseDebug";

    public static void logDatabaseContents(Context context) {
        AppDatabase db = AppDatabaseProvider.getDatabase(context);

        // Kiểm tra collections
        List<QuizCollectionEntity> collections = db.quizCollectionDao().getAll();
        Log.d(TAG, "Total collections: " + collections.size());
        for (QuizCollectionEntity collection : collections) {
            Log.d(TAG, "Collection ID: " + collection.id + ", Name: " + collection.name);
        }

        // Kiểm tra quiz sets
        List<QuizSetEntity> quizSets = db.quizSetDao().getAll();
        Log.d(TAG, "Total quiz sets: " + quizSets.size());
        for (QuizSetEntity quizSet : quizSets) {
            Log.d(TAG, "QuizSet ID: " + quizSet.id + ", Name: " + quizSet.name + ", CollectionID: " + quizSet.collectionId);
        }
    }
}
