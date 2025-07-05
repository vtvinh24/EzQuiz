package dev.vtvinh24.ezquiz.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

import dev.vtvinh24.ezquiz.ui.FlashcardViewModel.QuizDisplayItem;

public class FlashcardAdapter extends FragmentStateAdapter {

  // Danh sách chứa các đối tượng QuizDisplayItem
  private List<QuizDisplayItem> items = new ArrayList<>();

  public FlashcardAdapter(@NonNull FragmentActivity fragmentActivity) {
    super(fragmentActivity);
  }

  @NonNull
  @Override
  public Fragment createFragment(int position) {
    // Lấy đối tượng QuizDisplayItem tại vị trí hiện tại.
    QuizDisplayItem currentItem = items.get(position);

    // Truyền đối tượng Quiz (nằm bên trong QuizDisplayItem) vào Fragment.
    // FlashcardFragment chỉ cần đối tượng Quiz model (không có ID).
    return FlashcardFragment.newInstance(currentItem.quiz);
  }

  @Override
  public int getItemCount() {
    return items.size();
  }

  // Phương thức submitList nhận một danh sách các QuizDisplayItem.
  public void submitList(List<QuizDisplayItem> newItems) {
    this.items = newItems;
    notifyDataSetChanged(); // Cân nhắc dùng DiffUtil cho hiệu suất
  }
}