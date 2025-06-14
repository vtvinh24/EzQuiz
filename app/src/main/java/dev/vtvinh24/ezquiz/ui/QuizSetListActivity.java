package dev.vtvinh24.ezquiz.ui;

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
import dev.vtvinh24.ezquiz.data.entity.QuizSetEntity;

public class QuizSetListActivity extends AppCompatActivity {
  private RecyclerView recyclerView;
  private List<QuizSetEntity> sets;
  private AppDatabase db;
  private long collectionId;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_quiz_set_list);
    recyclerView = findViewById(R.id.recyclerView);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    collectionId = getIntent().getLongExtra("collectionId", -1);
    db = AppDatabaseProvider.getDatabase(this);
    sets = db.quizSetDao().getByCollectionId(collectionId);
    FloatingActionButton fab = findViewById(R.id.fab_add_set);
    fab.setOnClickListener(v -> showAddSetDialog());
    refreshSets();
  }

  private void showAddSetDialog() {
    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_edit_set, null);
    EditText nameInput = dialogView.findViewById(R.id.edit_set_name);
    EditText descInput = dialogView.findViewById(R.id.edit_set_description);
    new AlertDialog.Builder(this)
            .setTitle("Add Quiz Set")
            .setView(dialogView)
            .setPositiveButton("Add", (d, w) -> {
              String name = nameInput.getText().toString();
              String desc = descInput.getText().toString();
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
            .setPositiveButton("Save", (d, w) -> {
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
    sets.addAll(db.quizSetDao().getByCollectionId(collectionId));
    View emptyView = findViewById(R.id.empty_view);
    if (sets.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
      recyclerView.setVisibility(View.GONE);
    } else {
      emptyView.setVisibility(View.GONE);
      recyclerView.setVisibility(View.VISIBLE);
    }
  }

  public void onItemLongClick(QuizSetEntity set) {
    String[] options = {"Edit", "Delete"};
    new AlertDialog.Builder(this)
            .setTitle(set.name)
            .setItems(options, (dialog, which) -> {
              if (which == 0) showEditSetDialog(set);
              else if (which == 1) {
                db.quizSetDao().delete(set);
                refreshSets();
                Toast.makeText(this, "Deleted", Toast.LENGTH_SHORT).show();
              }
            })
            .show();
  }
}
