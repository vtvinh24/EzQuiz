package dev.vtvinh24.ezquiz.ui.collections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.repository.InMemoryQuizRepository;

/**
 * Fragment for displaying and managing quiz collections.
 */
public class CollectionsFragment extends Fragment {

    private CollectionsViewModel viewModel;

    public static CollectionsFragment newInstance() {
        return new CollectionsFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel with repository
        // Note: In a real app, this would be injected with Hilt or another DI framework
        viewModel = new CollectionsViewModel(new InMemoryQuizRepository());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_collections, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe quizzes data
        viewModel.getQuizzes().observe(getViewLifecycleOwner(), quizzes -> {
            // Update UI with quizzes when data changes
            // For now, this is empty as we're just setting up the minimal structure
        });

        // Observe loading state
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Show/hide loading indicator
            // For now, this is empty as we're just setting up the minimal structure
        });
    }
}
