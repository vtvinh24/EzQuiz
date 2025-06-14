package dev.vtvinh24.ezquiz.ui.play;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.repository.InMemoryQuizRepository;
import dev.vtvinh24.ezquiz.domain.PlaySessionManager;

/**
 * Fragment for displaying quiz results after completion.
 */
public class QuizResultsFragment extends Fragment {

    private PlaySessionManager playSessionManager;
    private String sessionId;

    private TextView scoreTextView;
    private TextView percentageTextView;
    private TextView timeElapsedTextView;
    private ProgressBar scoreProgressBar;
    private Button returnToCollectionsButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get session ID from arguments
        if (getArguments() != null) {
            sessionId = getArguments().getString("session_id");
        }

        // Initialize play session manager
        // Note: In a real app, this would be injected with Hilt
        playSessionManager = new PlaySessionManager();

        // In a real app, we would load the play session from a repository
        // using the session ID
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_quiz_results, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views
        TextView quizTitleTextView = view.findViewById(R.id.quiz_title_text);
        scoreTextView = view.findViewById(R.id.score_text);
        percentageTextView = view.findViewById(R.id.percentage_text);
        timeElapsedTextView = view.findViewById(R.id.time_elapsed_text);
        scoreProgressBar = view.findViewById(R.id.score_progress_bar);
        returnToCollectionsButton = view.findViewById(R.id.return_to_collections_button);

        // Set placeholder data
        quizTitleTextView.setText("Quiz Title Placeholder");
        scoreTextView.setText("Score: 8/10");
        percentageTextView.setText("80%");
        timeElapsedTextView.setText("Time: 2:30");
        scoreProgressBar.setProgress(80);

        // Set click listener for button
        returnToCollectionsButton.setOnClickListener(v -> {
            Navigation.findNavController(view)
                    .navigate(R.id.action_results_to_collections);
        });
    }

    /**
     * Helper method to format elapsed time in minutes and seconds.
     *
     * @param millis Time in milliseconds
     * @return Formatted time string (MM:SS)
     */
    private String formatElapsedTime(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) -
                TimeUnit.MINUTES.toSeconds(minutes);
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds);
    }
}
