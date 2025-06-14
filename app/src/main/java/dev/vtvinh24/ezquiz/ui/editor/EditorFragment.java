package dev.vtvinh24.ezquiz.ui.editor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.repository.InMemoryQuizRepository;

/**
 * Fragment for creating and editing quizzes.
 */
public class EditorFragment extends Fragment {

    private EditorViewModel viewModel;

    public static EditorFragment newInstance() {
        return new EditorFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel with repository
        // Note: In a real app, this would be injected with Hilt or another DI framework
        viewModel = new EditorViewModel(new InMemoryQuizRepository());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_editor, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe current quiz data
        viewModel.getCurrentQuiz().observe(getViewLifecycleOwner(), quiz -> {
            // Update UI with quiz data when it changes
            // For now, this is empty as we're just setting up the minimal structure
        });

        // Observe saving state
        viewModel.getIsSaving().observe(getViewLifecycleOwner(), isSaving -> {
            // Show/hide saving indicator
            // For now, this is empty as we're just setting up the minimal structure
        });

        // Observe save success
        viewModel.getSaveSuccess().observe(getViewLifecycleOwner(), isSuccess -> {
            // Show success/failure message
            // For now, this is empty as we're just setting up the minimal structure
        });
    }
}
