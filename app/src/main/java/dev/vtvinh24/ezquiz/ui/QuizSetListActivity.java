package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

public class QuizSetListActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private List<QuizSetEntity> sets = new ArrayList<>();
    private AppDatabase db;
    private long collectionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_set_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        db = AppDatabaseProvider.getDatabase(this);
        collectionId = getIntent().getLongExtra("collectionId", -1);

        TextView titleSet = findViewById(R.id.title_set);
        String collectionName = getIntent().getStringExtra("collection_name");
        if (collectionName != null && !collectionName.isEmpty()) {
            titleSet.setText(collectionName);
        }

        FloatingActionButton fab = findViewById(R.id.fab_add_set);
        fab.setOnClickListener(v -> showAddSetDialog());

        ImageView backButton = findViewById(R.id.btn_back);
        backButton.setOnClickListener(v -> finish());

        refreshSets();
    }

    private void showAddSetDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_set, null);
        EditText nameInput = dialogView.findViewById(R.id.edit_set_name);
        EditText descInput = dialogView.findViewById(R.id.edit_set_description);

        new AlertDialog.Builder(this)
                .setTitle("Add Quiz Set")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    String desc = descInput.getText().toString().trim();

                    if (name.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    QuizSetEntity entity = new QuizSetEntity();
                    entity.name = name;
                    entity.description = desc;
                    entity.collectionId = collectionId;

                    db.quizSetDao().insert(entity);
                    refreshSets();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showEditSetDialog(QuizSetEntity set) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_set, null);
        EditText nameInput = dialogView.findViewById(R.id.edit_set_name);
        EditText descInput = dialogView.findViewById(R.id.edit_set_description);
        nameInput.setText(set.name);
        descInput.setText(set.description);

        new AlertDialog.Builder(this)
                .setTitle("Edit Quiz Set")
                .setView(dialogView)
                .setPositiveButton("Save", (dialog, which) -> {
                    set.name = nameInput.getText().toString();
                    set.description = descInput.getText().toString();
                    db.quizSetDao().update(set);
                    refreshSets();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void refreshSets() {
        sets.clear();
        List<QuizSetEntity> newData = db.quizSetDao().getByCollectionId(collectionId);
        if (newData != null) sets.addAll(newData);

        View emptyView = findViewById(R.id.empty_view);
        if (sets.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }

        recyclerView.setAdapter(new QuizSetAdapter(sets, new QuizSetAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(QuizSetEntity set) {
                Intent intent = new Intent(QuizSetListActivity.this, QuizListActivity.class);
                intent.putExtra("setId", set.id);
                intent.putExtra("set_name", set.name);
                startActivity(intent);
            }

            @Override
            public void onItemLongClick(QuizSetEntity set) {
                showOptionDialog(set);
            }
        }));
    }

    private void showOptionDialog(QuizSetEntity set) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_options, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.option_edit).setOnClickListener(v -> {
            dialog.dismiss();
            showEditSetDialog(set);
        });

        dialogView.findViewById(R.id.option_delete).setOnClickListener(v -> {
            dialog.dismiss();
            db.quizSetDao().delete(set);
            refreshSets();
            Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
        });

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialog.show();
    }
}
