package dev.vtvinh24.ezquiz.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;
import java.util.List;

public class PracticeAdapter extends FragmentStateAdapter {

    private List<PracticeViewModel.PracticeItem> items = new ArrayList<>();

    public PracticeAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        PracticeViewModel.PracticeItem item = items.get(position);
        return PracticeQuestionFragment.newInstance(item.id, item.quiz);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void submitList(List<PracticeViewModel.PracticeItem> newItems) {
        this.items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }
}