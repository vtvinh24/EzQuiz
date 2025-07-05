package dev.vtvinh24.ezquiz.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import java.util.ArrayList;
import java.util.List;
import dev.vtvinh24.ezquiz.data.model.Quiz;

// Import lớp QuizDisplayItem từ ViewModel
import dev.vtvinh24.ezquiz.ui.FlashcardViewModel.QuizDisplayItem;

public class FlashcardAdapter extends FragmentStateAdapter {

    // THAY ĐỔI 1:
    // Danh sách bây giờ chứa các đối tượng QuizDisplayItem, không phải Quiz.
    private List<QuizDisplayItem> items = new ArrayList<>();

    public FlashcardAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // THAY ĐỔI 2:
        // Lấy đối tượng QuizDisplayItem tại vị trí hiện tại.
        QuizDisplayItem currentItem = items.get(position);

        // Truyền đối tượng Quiz (nằm bên trong QuizDisplayItem) vào Fragment.
        // Giao diện của FlashcardFragment không cần thay đổi, nó vẫn chỉ cần đối tượng Quiz.
        return FlashcardFragment.newInstance(currentItem.quiz);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // THAY ĐỔI 3:
    // Phương thức submitList bây giờ nhận một danh sách các QuizDisplayItem.
    public void submitList(List<QuizDisplayItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }
}