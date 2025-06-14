package dev.vtvinh24.ezquiz.ui.play;

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
 * Fragment for playing quizzes.
 */
public class PlayFragment extends Fragment {

    private PlayViewModel viewModel;

    public static PlayFragment newInstance(String quizId) {
        PlayFragment fragment = new PlayFragment();
        Bundle args = new Bundle();
        args.putString("quiz_id", quizId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize ViewModel with repository
        // Note: In a real app, this would be injected with Hilt or another DI framework
        viewModel = new PlayViewModel(new InMemoryQuizRepository());

        // Load quiz if ID is provided
        if (getArguments() != null && getArguments().containsKey("quiz_id")) {
            String quizId = getArguments().getString("quiz_id");
            if (quizId != null) {
                viewModel.loadQuiz(quizId);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_play, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Observe current question
        viewModel.getCurrentQuestion().observe(getViewLifecycleOwner(), question -> {
            // Update UI with current question
            // For now, this is empty as we're just setting up the minimal structure
        });

        // Observe score
        viewModel.getScore().observe(getViewLifecycleOwner(), score -> {
            // Update score display
            // For now, this is empty as we're just setting up the minimal structure
        });

        // Observe quiz completion
        viewModel.getIsQuizFinished().observe(getViewLifecycleOwner(), isFinished -> {
            // Show completion screen if finished
            // For now, this is empty as we're just setting up the minimal structure
        });
    }
}
