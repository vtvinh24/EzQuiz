package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.ui.fragment.CollectionsFragment;
import dev.vtvinh24.ezquiz.ui.fragment.HistoryFragment;
import dev.vtvinh24.ezquiz.ui.fragment.ProgressFragment;
import dev.vtvinh24.ezquiz.ui.fragment.SubscriptionFragment;

public class MainActivity extends AppCompatActivity {

  private BottomNavigationView bottomNavigationView;
  private FragmentManager fragmentManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initializeViews();
    setupBottomNavigation();

    // Load default fragment (Collections)
    if (savedInstanceState == null) {
      loadFragment(new CollectionsFragment());
      bottomNavigationView.setSelectedItemId(R.id.nav_collections);
    }
  }

  private void initializeViews() {
    bottomNavigationView = findViewById(R.id.bottom_navigation);
    fragmentManager = getSupportFragmentManager();
  }

  private void setupBottomNavigation() {
    bottomNavigationView.setOnItemSelectedListener(item -> {
      Fragment selectedFragment = null;

      int itemId = item.getItemId();
      if (itemId == R.id.nav_collections) {
        selectedFragment = new CollectionsFragment();
      } else if (itemId == R.id.nav_progress) {
        selectedFragment = new ProgressFragment();
      } else if (itemId == R.id.nav_generate_ai) {
        // Handle Generate AI - open AI activity
        Intent intent = new Intent(MainActivity.this, GenerateQuizAIActivity.class);
        startActivity(intent);
        return false; // Don't change selection
      } else if (itemId == R.id.nav_history) {
        selectedFragment = new HistoryFragment();
      } else if (itemId == R.id.nav_subscription) {
        selectedFragment = new SubscriptionFragment();
      }

      if (selectedFragment != null) {
        loadFragment(selectedFragment);
        return true;
      }

      return false;
    });
  }


  private void loadFragment(Fragment fragment) {
    FragmentTransaction transaction = fragmentManager.beginTransaction();
    transaction.replace(R.id.fragment_container, fragment);
    transaction.commit();
  }
}