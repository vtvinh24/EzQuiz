package dev.vtvinh24.ezquiz.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.db.AppDatabase;
import dev.vtvinh24.ezquiz.data.db.AppDatabaseProvider;
import dev.vtvinh24.ezquiz.data.entity.QuizCollectionEntity;
import dev.vtvinh24.ezquiz.ui.PreImportActivity;
import dev.vtvinh24.ezquiz.ui.QuizCollectionAdapter;
import dev.vtvinh24.ezquiz.ui.QuizSetListActivity;

public class CollectionsFragment extends Fragment implements QuizCollectionAdapter.OnSelectionModeListener {

  private final List<QuizCollectionEntity> collections = new ArrayList<>();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();
  private RecyclerView recyclerView;
  private QuizCollectionAdapter adapter;
  private ExtendedFloatingActionButton fabAddCollection;
  private ExtendedFloatingActionButton fabImport;
  private Chip chipTotalCollections;
  private AppDatabase db;
  private View selectionToolbar;
  private TextView selectionTitle;
  private ImageView btnCloseSelection;
  private ImageView btnDeleteSelected;
  private ImageView btnEditSelected;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_collections, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    initializeViews(view);
    setupRecyclerView();
    setupFab();
    loadCollections();
  }

  private void initializeViews(View view) {
    recyclerView = view.findViewById(R.id.recyclerView);
    fabAddCollection = view.findViewById(R.id.fab_add_collection);
    fabImport = view.findViewById(R.id.fab_import);
    chipTotalCollections = view.findViewById(R.id.chip_total_collections);
    db = AppDatabaseProvider.getDatabase(requireContext());
    selectionToolbar = view.findViewById(R.id.selection_toolbar);
    selectionTitle = view.findViewById(R.id.selection_title);
    btnCloseSelection = view.findViewById(R.id.btn_close_selection);
    btnDeleteSelected = view.findViewById(R.id.btn_delete_selected);
    btnEditSelected = view.findViewById(R.id.btn_edit_selected);

    // Make sure the total collections chip is not clickable
    if (chipTotalCollections != null) {
      chipTotalCollections.setClickable(false);
      chipTotalCollections.setFocusable(false);
    }

    btnCloseSelection.setOnClickListener(v -> {
      adapter.clearSelection();
    });

    btnDeleteSelected.setOnClickListener(v -> {
      List<QuizCollectionEntity> selectedItems = adapter.getSelectedCollections();
      if (!selectedItems.isEmpty()) {
        showDeleteConfirmationDialog(selectedItems);
      }
    });

    btnEditSelected.setOnClickListener(v -> {
      List<QuizCollectionEntity> selectedItems = adapter.getSelectedCollections();
      if (selectedItems.size() == 1) {
        showEditCollectionDialog(selectedItems.get(0));
      } else {
        Toast.makeText(getContext(), "Please select only one collection to edit", Toast.LENGTH_SHORT).show();
      }
    });
  }

  private void setupRecyclerView() {
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    adapter = new QuizCollectionAdapter(getContext(), collections, this);
    recyclerView.setAdapter(adapter);
  }

  private void setupFab() {
    fabAddCollection.setOnClickListener(v -> showAddCollectionDialog());
    fabImport.setOnClickListener(v -> startImportActivity());
  }

  private void startImportActivity() {
    if (collections.isEmpty()) {
      // Show dialog explaining that a collection is needed first
      AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
      builder.setTitle("No Collection Available");
      builder.setMessage("Please create a collection first before importing quizzes.");
      builder.setPositiveButton("Create Collection", (dialog, which) -> showAddCollectionDialog());
      builder.setNegativeButton("Cancel", null);
      builder.show();
      return;
    }

    Intent intent = new Intent(getContext(), PreImportActivity.class);
    startActivity(intent);
  }

  private void loadCollections() {
    executor.execute(() -> {
      List<QuizCollectionEntity> loadedCollections = db.quizCollectionDao().getAll();

      // Thêm test data nếu không có collection nào
      if (loadedCollections.isEmpty()) {
        // Tạo một collection test
        QuizCollectionEntity testCollection = new QuizCollectionEntity();
        testCollection.name = "Test Collection";
        testCollection.description = "This is a test collection";
        testCollection.createdAt = System.currentTimeMillis();
        testCollection.updatedAt = System.currentTimeMillis();

        long id = db.quizCollectionDao().insert(testCollection);
        testCollection.id = id;
        loadedCollections.add(testCollection);
      }

      requireActivity().runOnUiThread(() -> {
        collections.clear();
        collections.addAll(loadedCollections);
        adapter.notifyDataSetChanged();
        updateCollectionCount();
      });
    });
  }

  private void showAddCollectionDialog() {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle("Add New Collection");

    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_collection, null);
    EditText editCollectionName = dialogView.findViewById(R.id.edit_collection_name);
    builder.setView(dialogView);

    builder.setPositiveButton("Create", (dialog, which) -> {
      String collectionName = editCollectionName.getText().toString().trim();
      if (!collectionName.isEmpty()) {
        createCollection(collectionName);
      } else {
        Toast.makeText(getContext(), "Please enter a collection name", Toast.LENGTH_SHORT).show();
      }
    });

    builder.setNegativeButton("Cancel", null);
    builder.show();
  }

  private void createCollection(String name) {
    executor.execute(() -> {
      QuizCollectionEntity collection = new QuizCollectionEntity();
      collection.name = name;
      collection.createdAt = System.currentTimeMillis();
      collection.updatedAt = System.currentTimeMillis();

      long id = db.quizCollectionDao().insert(collection);
      collection.id = id;

      requireActivity().runOnUiThread(() -> {
        collections.add(collection);
        adapter.notifyItemInserted(collections.size() - 1);
        updateCollectionCount();
        Toast.makeText(getContext(), "Collection created successfully", Toast.LENGTH_SHORT).show();
      });
    });
  }

  private void updateCollectionCount() {
    if (chipTotalCollections != null) {
      int count = collections.size();
      chipTotalCollections.setText(String.valueOf(count));
    }
  }

  @Override
  public void onItemClick(QuizCollectionEntity collection) {
    Intent intent = new Intent(getContext(), QuizSetListActivity.class);
    intent.putExtra("collectionId", collection.id); // Sử dụng "collectionId" thay vì "collection_id"
    intent.putExtra("collection_name", collection.name);
    startActivity(intent);
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (executor != null && !executor.isShutdown()) {
      executor.shutdown();
    }
  }

  @Override
  public void onSelectionModeStarted() {
    if (selectionToolbar != null) {
      selectionToolbar.setVisibility(View.VISIBLE);
    }
    // Hide FABs during selection mode
    fabAddCollection.setVisibility(View.GONE);
    fabImport.setVisibility(View.GONE);
  }

  @Override
  public void onSelectionModeEnded() {
    if (selectionToolbar != null) {
      selectionToolbar.setVisibility(View.GONE);
    }
    // Show FABs again
    fabAddCollection.setVisibility(View.VISIBLE);
    fabImport.setVisibility(View.VISIBLE);
  }

  @Override
  public void onSelectionChanged(int selectedCount) {
    if (selectionTitle != null) {
      selectionTitle.setText(selectedCount + " selected");
    }

    // Show/hide edit button based on selection count
    if (btnEditSelected != null) {
      btnEditSelected.setVisibility(selectedCount == 1 ? View.VISIBLE : View.GONE);
    }
  }

  private void showDeleteConfirmationDialog(List<QuizCollectionEntity> selectedItems) {
    String message = selectedItems.size() == 1
        ? "Are you sure you want to delete \"" + selectedItems.get(0).name + "\"?"
        : "Are you sure you want to delete " + selectedItems.size() + " collections?";

    new AlertDialog.Builder(requireContext())
        .setTitle("Delete Collections")
        .setMessage(message)
        .setPositiveButton("Delete", (dialog, which) -> deleteSelectedCollections(selectedItems))
        .setNegativeButton("Cancel", null)
        .show();
  }

  private void deleteSelectedCollections(List<QuizCollectionEntity> selectedItems) {
    executor.execute(() -> {
      for (QuizCollectionEntity collection : selectedItems) {
        db.quizCollectionDao().delete(collection);
      }

      requireActivity().runOnUiThread(() -> {
        collections.removeAll(selectedItems);
        adapter.notifyDataSetChanged();
        updateCollectionCount();
        adapter.clearSelection();

        String message = selectedItems.size() == 1
            ? "Collection deleted successfully"
            : selectedItems.size() + " collections deleted successfully";
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
      });
    });
  }

  private void showEditCollectionDialog(QuizCollectionEntity collection) {
    AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
    builder.setTitle("Edit Collection");

    View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_collection, null);
    EditText editCollectionName = dialogView.findViewById(R.id.edit_collection_name);
    editCollectionName.setText(collection.name);
    editCollectionName.setSelection(collection.name.length());
    builder.setView(dialogView);

    builder.setPositiveButton("Save", (dialog, which) -> {
      String newName = editCollectionName.getText().toString().trim();
      if (!newName.isEmpty() && !newName.equals(collection.name)) {
        updateCollectionName(collection, newName);
      } else if (newName.isEmpty()) {
        Toast.makeText(getContext(), "Please enter a collection name", Toast.LENGTH_SHORT).show();
      } else {
        adapter.clearSelection();
      }
    });

    builder.setNegativeButton("Cancel", (dialog, which) -> adapter.clearSelection());
    builder.show();
  }

  private void updateCollectionName(QuizCollectionEntity collection, String newName) {
    executor.execute(() -> {
      collection.name = newName;
      collection.updatedAt = System.currentTimeMillis();
      db.quizCollectionDao().update(collection);

      requireActivity().runOnUiThread(() -> {
        adapter.notifyDataSetChanged();
        adapter.clearSelection();
        Toast.makeText(getContext(), "Collection name updated successfully", Toast.LENGTH_SHORT).show();
      });
    });
  }
}
