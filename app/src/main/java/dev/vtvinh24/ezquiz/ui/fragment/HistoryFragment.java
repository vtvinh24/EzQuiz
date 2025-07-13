package dev.vtvinh24.ezquiz.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.List;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.HistoryItem;
import dev.vtvinh24.ezquiz.data.repo.HistoryRepository;
import dev.vtvinh24.ezquiz.ui.MainActivity;
import dev.vtvinh24.ezquiz.ui.adapter.HistoryAdapter;

public class HistoryFragment extends Fragment {

    private RecyclerView recyclerHistory;
    private LinearLayout layoutLoading, layoutEmpty;
    private ChipGroup chipGroupFilters;
    private Chip chipAll, chipInProgress, chipCompleted;
    private MaterialButton buttonBrowseQuizzes;

    private HistoryAdapter historyAdapter;
    private HistoryRepository historyRepository;
    private LiveData<List<HistoryItem>> currentLiveData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_history, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initializeViews(view);
        setupRecyclerView();
        setupFilters();
        setupRepository();
        loadInitialData();
    }

    private void initializeViews(View view) {
        recyclerHistory = view.findViewById(R.id.recycler_history);
        layoutLoading = view.findViewById(R.id.layout_loading);
        layoutEmpty = view.findViewById(R.id.layout_empty);
        chipGroupFilters = view.findViewById(R.id.chip_group_filters);
        chipAll = view.findViewById(R.id.chip_all);
        chipInProgress = view.findViewById(R.id.chip_in_progress);
        chipCompleted = view.findViewById(R.id.chip_completed);
        buttonBrowseQuizzes = view.findViewById(R.id.button_browse_quizzes);
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(getContext());
        recyclerHistory.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerHistory.setAdapter(historyAdapter);
    }

    private void setupFilters() {
        chipGroupFilters.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty()) return;

            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chip_all) {
                loadAllHistory();
            } else if (checkedId == R.id.chip_in_progress) {
                loadInProgressHistory();
            } else if (checkedId == R.id.chip_completed) {
                loadCompletedHistory();
            }
        });

        buttonBrowseQuizzes.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                MainActivity mainActivity = (MainActivity) getActivity();
                mainActivity.findViewById(R.id.nav_collections).performClick();
            }
        });
    }

    private void setupRepository() {
        historyRepository = new HistoryRepository(getContext());
    }

    private void loadInitialData() {
        loadAllHistory();
    }

    private void loadAllHistory() {
        showLoading();
        if (currentLiveData != null) {
            currentLiveData.removeObservers(getViewLifecycleOwner());
        }

        currentLiveData = historyRepository.getAllHistoryItems();
        currentLiveData.observe(getViewLifecycleOwner(), historyItems -> {
            hideLoading();
            if (historyItems == null || historyItems.isEmpty()) {
                showEmpty();
            } else {
                showContent();
                historyAdapter.setHistoryItems(historyItems);
            }
        });
    }

    private void loadInProgressHistory() {
        showLoading();
        if (currentLiveData != null) {
            currentLiveData.removeObservers(getViewLifecycleOwner());
        }

        currentLiveData = historyRepository.getInProgressItems();
        currentLiveData.observe(getViewLifecycleOwner(), historyItems -> {
            hideLoading();
            if (historyItems == null || historyItems.isEmpty()) {
                showEmpty();
            } else {
                showContent();
                historyAdapter.setHistoryItems(historyItems);
            }
        });
    }

    private void loadCompletedHistory() {
        showLoading();
        if (currentLiveData != null) {
            currentLiveData.removeObservers(getViewLifecycleOwner());
        }

        currentLiveData = historyRepository.getCompletedItems();
        currentLiveData.observe(getViewLifecycleOwner(), historyItems -> {
            hideLoading();
            if (historyItems == null || historyItems.isEmpty()) {
                showEmpty();
            } else {
                showContent();
                historyAdapter.setHistoryItems(historyItems);
            }
        });
    }

    private void showLoading() {
        layoutLoading.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        recyclerHistory.setVisibility(View.GONE);
    }

    private void hideLoading() {
        layoutLoading.setVisibility(View.GONE);
    }

    private void showEmpty() {
        layoutEmpty.setVisibility(View.VISIBLE);
        recyclerHistory.setVisibility(View.GONE);
    }

    private void showContent() {
        layoutEmpty.setVisibility(View.GONE);
        recyclerHistory.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (chipAll.isChecked()) {
            loadAllHistory();
        } else if (chipInProgress.isChecked()) {
            loadInProgressHistory();
        } else if (chipCompleted.isChecked()) {
            loadCompletedHistory();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentLiveData != null) {
            currentLiveData.removeObservers(getViewLifecycleOwner());
        }
    }
}
