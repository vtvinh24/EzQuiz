package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.ui.fragment.CollectionsFragment;
import dev.vtvinh24.ezquiz.ui.fragment.GenerateQuizFragment;
import dev.vtvinh24.ezquiz.ui.fragment.HistoryFragment;
import dev.vtvinh24.ezquiz.ui.fragment.ProgressFragment;
import dev.vtvinh24.ezquiz.ui.fragment.SubscriptionFragment;
import dev.vtvinh24.ezquiz.ui.viewmodel.QuizSyncViewModel;

public class MainActivity extends AppCompatActivity {

  private BottomNavigationView bottomNavigationView;
  private FragmentManager fragmentManager;
  private AuthViewModel authViewModel;
  private QuizSyncViewModel quizSyncViewModel;
  private Fragment currentFragment;
  private int currentSelectedItemId = R.id.nav_collections;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initializeViews();
    initViewModels();
    setupBottomNavigation();
    observeUser();
    setupQuizSync();

    if (savedInstanceState == null) {
      loadFragment(new CollectionsFragment(), false);
      bottomNavigationView.setSelectedItemId(R.id.nav_collections);
    }
  }

  private void initializeViews() {
    bottomNavigationView = findViewById(R.id.bottom_navigation);
    fragmentManager = getSupportFragmentManager();
  }

  private void initViewModels() {
    authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    quizSyncViewModel = new ViewModelProvider(this).get(QuizSyncViewModel.class);
  }

  private void observeUser() {
    authViewModel.getCurrentUser().observe(this, user -> {
      if (user != null) {
        setTitle("Welcome, " + user.getName());

        // Tự động sync quiz khi user đăng nhập
        quizSyncViewModel.performAutoSync();
      } else {
        redirectToLogin();
      }
    });

    authViewModel.getIsLoggedIn().observe(this, isLoggedIn -> {
      if (!isLoggedIn) {
        redirectToLogin();
      }
    });
  }

  private void setupQuizSync() {
    // Observe sync status để hiển thị thông báo
    quizSyncViewModel.getSyncStatus().observe(this, status -> {
      if (status != null && !status.isEmpty()) {
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
      }
    });

    // Verify token khi app khởi động
    if (authViewModel.getCurrentUser().getValue() != null) {
      authViewModel.verifyToken();
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.main_menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.action_logout) {
      performLogout();
      return true;
    } else if (itemId == R.id.action_sync) {
      performManualSync();
      return true;
    } else if (itemId == R.id.action_refresh) {
      refreshUserData();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  private void performLogout() {
    authViewModel.logout();
    Toast.makeText(this, "Đăng xuất thành công", Toast.LENGTH_SHORT).show();
    redirectToLogin();
  }

  private void performManualSync() {
    if (quizSyncViewModel.isUserLoggedIn()) {
      quizSyncViewModel.syncQuizzesFromServer();
      Toast.makeText(this, "Đang đồng bộ quiz từ server...", Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(this, "Cần đăng nhập để đồng bộ", Toast.LENGTH_SHORT).show();
    }
  }

  private void refreshUserData() {
    authViewModel.refreshUserProfile();
    quizSyncViewModel.refreshUserQuizSets();
    Toast.makeText(this, "Đang cập nhật dữ liệu...", Toast.LENGTH_SHORT).show();
  }

  private void redirectToLogin() {
    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
  }

  private void setupBottomNavigation() {
    bottomNavigationView.setOnItemSelectedListener(item -> {
      Fragment selectedFragment = null;
      boolean useAnimation = true;

      int itemId = item.getItemId();
      if (itemId == currentSelectedItemId) {
        return false;
      }

      // Updated order: Collections > Progress > Generate AI > History > Subscription
      if (itemId == R.id.nav_collections) {
        selectedFragment = new CollectionsFragment();
      } else if (itemId == R.id.nav_progress) {
        selectedFragment = new ProgressFragment();
      } else if (itemId == R.id.nav_generate) {
        selectedFragment = new GenerateQuizFragment();
      } else if (itemId == R.id.nav_history) {
        selectedFragment = new HistoryFragment();
      } else if (itemId == R.id.nav_subscription) {
        selectedFragment = new SubscriptionFragment();
      }

      if (selectedFragment != null) {
        int previousSelectedItemId = currentSelectedItemId;
        currentSelectedItemId = itemId;

        boolean isMovingRight = isMovingToRight(previousSelectedItemId, itemId);
        loadFragment(selectedFragment, useAnimation, isMovingRight);
        return true;
      }

      return false;
    });

    bottomNavigationView.setOnItemReselectedListener(item -> {
      Fragment fragment = fragmentManager.findFragmentById(R.id.fragment_container);
      if (fragment instanceof ScrollableFragment) {
        ((ScrollableFragment) fragment).scrollToTop();
      }
    });
  }

  private boolean isMovingToRight(int previousItemId, int currentItemId) {
    // Updated order array: Collections > Progress > Generate AI > History > Subscription
    int[] itemOrder = {R.id.nav_collections, R.id.nav_progress, R.id.nav_generate,
                      R.id.nav_history, R.id.nav_subscription};

    int previousIndex = -1, currentIndex = -1;

    for (int i = 0; i < itemOrder.length; i++) {
      if (itemOrder[i] == previousItemId) previousIndex = i;
      if (itemOrder[i] == currentItemId) currentIndex = i;
    }

    return currentIndex > previousIndex;
  }

  private void loadFragment(Fragment fragment, boolean animate) {
    loadFragment(fragment, animate, true);
  }

  private void loadFragment(Fragment fragment, boolean animate, boolean isMovingRight) {
    FragmentTransaction transaction = fragmentManager.beginTransaction();

    if (animate && currentFragment != null) {
      if (isMovingRight) {
        transaction.setCustomAnimations(
          R.anim.slide_in_right,
          R.anim.slide_out_left
        );
      } else {
        transaction.setCustomAnimations(
          R.anim.slide_in_left,
          R.anim.slide_out_right
        );
      }
    }

    transaction.replace(R.id.fragment_container, fragment);
    transaction.commit();

    currentFragment = fragment;
  }

  public interface ScrollableFragment {
    void scrollToTop();
  }
}
