package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;

public class MainActivity extends AppCompatActivity implements QuizCollectionAdapter.OnItemClickListener {

    private RecyclerView recyclerView;
    private QuizCollectionAdapter adapter;
    private List<QuizCollectionEntity> collections;
    private AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabaseProvider.getDatabase(this);
        collections = db.quizCollectionDao().getAll();

        adapter = new QuizCollectionAdapter(collections, this);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.fab_add_collection);
        fab.setOnClickListener(v -> showAddCollectionDialog());

        findViewById(R.id.fab_generate_ai).setOnClickListener(v ->
                startActivity(new Intent(this, GenerateQuizAIActivity.class)));


        refreshCollections();
    }

    private void showAddCollectionDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_collection, null);
        EditText nameInput = dialogView.findViewById(R.id.edit_collection_name);
        EditText descInput = dialogView.findViewById(R.id.edit_collection_description);

        new AlertDialog.Builder(this)
                .setTitle("Add Collection")
                .setView(dialogView)
                .setPositiveButton("Add", (d, w) -> {
                    String name = nameInput.getText().toString();
                    String desc = descInput.getText().toString();
                    QuizCollectionEntity entity = new QuizCollectionEntity();
                    entity.name = name;
                    entity.description = desc;
                    db.quizCollectionDao().insert(entity);
                    refreshCollections();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditCollectionDialog(QuizCollectionEntity collection) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_collection, null);
        EditText nameInput = dialogView.findViewById(R.id.edit_collection_name);
        EditText descInput = dialogView.findViewById(R.id.edit_collection_description);
        nameInput.setText(collection.name);
        descInput.setText(collection.description);

        new AlertDialog.Builder(this)
                .setTitle("Edit Collection")
                .setView(dialogView)
                .setPositiveButton("Save", (d, w) -> {
                    collection.name = nameInput.getText().toString();
                    collection.description = descInput.getText().toString();
                    db.quizCollectionDao().update(collection);
                    refreshCollections();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshCollections() {
        collections.clear();
        collections.addAll(db.quizCollectionDao().getAll());
        adapter.notifyDataSetChanged();

        View emptyView = findViewById(R.id.empty_view);
        if (collections.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(QuizCollectionEntity collection) {
        Intent intent = new Intent(this, QuizSetListActivity.class);
        intent.putExtra("collectionId", collection.id);
        intent.putExtra("collection_name", collection.name); // ✅ TRUYỀN TÊN COLLECTION
        startActivity(intent);
    }

    @Override
    public void onItemLongClick(QuizCollectionEntity collection) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.option_edit).setOnClickListener(v -> {
            dialog.dismiss();
            showEditCollectionDialog(collection);
        });

        dialogView.findViewById(R.id.option_delete).setOnClickListener(v -> {
            dialog.dismiss();
            db.quizCollectionDao().delete(collection);
            refreshCollections();
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }
}
