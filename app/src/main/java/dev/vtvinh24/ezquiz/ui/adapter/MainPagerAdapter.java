package dev.vtvinh24.ezquiz.ui.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import dev.vtvinh24.ezquiz.ui.fragment.CollectionsFragment;
import dev.vtvinh24.ezquiz.ui.fragment.GenerateQuizFragment;
import dev.vtvinh24.ezquiz.ui.fragment.HistoryFragment;
import dev.vtvinh24.ezquiz.ui.fragment.ProgressFragment;
import dev.vtvinh24.ezquiz.ui.fragment.SubscriptionFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new CollectionsFragment();
            case 1:
                return new GenerateQuizFragment();
            case 2:
                return new HistoryFragment();
            case 3:
                return new ProgressFragment();
            case 4:
                return new SubscriptionFragment();
            default:
                return new CollectionsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
