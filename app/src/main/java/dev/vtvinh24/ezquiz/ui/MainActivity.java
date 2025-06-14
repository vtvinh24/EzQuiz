package dev.vtvinh24.ezquiz.ui;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.repository.InMemoryQuizRepository;
import dev.vtvinh24.ezquiz.domain.QuizManager;

/**
 * Main entry point Activity for the EzQuiz application.
 * Handles app navigation and serves as the container for all fragments.
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    private QuizManager quizManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize toolbar
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Initialize Navigation Controller - FIXED to use NavHostFragment properly
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);

        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();

            // Setup app bar with navigation
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.collectionsFragment, R.id.editorFragment, R.id.importQuizFragment)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

            // Setup bottom navigation
            BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNav, navController);
        }

        // Initialize managers (in a real app, this would be handled by dependency injection)
        quizManager = new QuizManager(new InMemoryQuizRepository());
    }

    @Override
    public boolean onSupportNavigateUp() {
        // Handle navigation up button
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }
}
