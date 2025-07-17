package dev.vtvinh24.ezquiz.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.ui.adapter.MainPagerAdapter;

public class MainActivity extends AppCompatActivity {

  private BottomNavigationView bottomNavigationView;
  private ViewPager2 viewPager;
  private MainPagerAdapter pagerAdapter;
  private AuthViewModel authViewModel;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    initializeViews();
    initViewModel();
    setupViewPager();
    setupBottomNavigation();
    observeUser();

    // Set default selection
    if (savedInstanceState == null) {
      viewPager.setCurrentItem(0, false);
      bottomNavigationView.setSelectedItemId(R.id.nav_collections);
    }
  }

  private void initializeViews() {
    bottomNavigationView = findViewById(R.id.bottom_navigation);
    viewPager = findViewById(R.id.viewPager);
  }

  private void initViewModel() {
    authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
  }

  private void setupViewPager() {
    pagerAdapter = new MainPagerAdapter(this);
    viewPager.setAdapter(pagerAdapter);
    viewPager.setOffscreenPageLimit(3);

    viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
      @Override
      public void onPageSelected(int position) {
        super.onPageSelected(position);
        updateBottomNavigationSelection(position);
      }
    });
  }

  private void updateBottomNavigationSelection(int position) {
    switch (position) {
      case 0:
        bottomNavigationView.setSelectedItemId(R.id.nav_collections);
        updateNavbarTheme(R.color.collections_active, R.color.collections_background);
        break;
      case 1:
        bottomNavigationView.setSelectedItemId(R.id.nav_generate);
        updateNavbarTheme(R.color.generate_ai_active, R.color.generate_ai_background);
        break;
      case 2:
        bottomNavigationView.setSelectedItemId(R.id.nav_history);
        updateNavbarTheme(R.color.bottom_nav_active, R.color.bottom_nav_indicator_light);
        break;
      case 3:
        bottomNavigationView.setSelectedItemId(R.id.nav_progress);
        updateNavbarTheme(R.color.progress_active, R.color.progress_background);
        break;
      case 4:
        bottomNavigationView.setSelectedItemId(R.id.nav_subscription);
        updateNavbarTheme(R.color.bottom_nav_active_secondary, R.color.bottom_nav_indicator_light);
        break;
    }
  }

  private void updateNavbarTheme(int activeColor, int backgroundColor) {
    // Chỉ tạo hiệu ứng màu sắc nhẹ nhàng, không scale cả navbar
    int currentActiveColor = getResources().getColor(activeColor, getTheme());

    // Có thể thêm hiệu ứng subtle cho từng icon riêng lẻ ở đây nếu cần
    // Nhưng không làm gì với cả thanh navbar
  }

  private void observeUser() {
    authViewModel.getCurrentUser().observe(this, user -> {
      if (user != null) {
        setTitle("Welcome, " + user.getName());
      } else {
        redirectToLogin();
      }
    });
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
    }
    return super.onOptionsItemSelected(item);
  }

  private void performLogout() {
    authViewModel.logout();
    Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    redirectToLogin();
  }

  private void redirectToLogin() {
    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
    startActivity(intent);
    finish();
  }

  private void setupBottomNavigation() {
    bottomNavigationView.setOnItemSelectedListener(item -> {
      int position = getPositionFromMenuId(item.getItemId());
      if (position != -1) {
        viewPager.setCurrentItem(position, true);
        return true;
      }
      return false;
    });
  }

  private int getPositionFromMenuId(int menuId) {
    if (menuId == R.id.nav_collections) {
      return 0;
    } else if (menuId == R.id.nav_generate) {
      return 1;
    } else if (menuId == R.id.nav_history) {
      return 2;
    } else if (menuId == R.id.nav_progress) {
      return 3;
    } else if (menuId == R.id.nav_subscription) {
      return 4;
    }
    return -1;
  }
}