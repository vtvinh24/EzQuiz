package dev.vtvinh24.ezquiz.ui;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.vtvinh24.ezquiz.R;

public class TestAdapter extends ListAdapter<TestViewModel.TestQuestionItem, RecyclerView.ViewHolder> {

    private final TestViewModel viewModel;

    // Định nghĩa các loại View
    private static final int VIEW_TYPE_MULTIPLE_CHOICE = 1;
    private static final int VIEW_TYPE_TRUE_FALSE = 2;

    public TestAdapter(TestViewModel viewModel) {
        super(TestQuestionDiffCallback);
        this.viewModel = viewModel;
    }

    @Override
    public int getItemViewType(int position) {
        TestViewModel.TestQuestionItem item = getItem(position);
        // Câu hỏi được tạo ra cho chế độ Đúng/Sai sẽ luôn có 2 đáp án
        if (item.quiz.getAnswers() != null && item.quiz.getAnswers().size() == 2) {
            // Kiểm tra thêm nội dung để chắc chắn đây là câu Đúng/Sai
            if (item.quiz.getAnswers().get(0).equalsIgnoreCase("Đúng") &&
                    item.quiz.getAnswers().get(1).equalsIgnoreCase("Sai")) {
                return VIEW_TYPE_TRUE_FALSE;
            }
        }
        return VIEW_TYPE_MULTIPLE_CHOICE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_TRUE_FALSE) {
            View view = inflater.inflate(R.layout.item_test_question_tf, parent, false);
            return new TrueFalseViewHolder(view);
        }
        View view = inflater.inflate(R.layout.item_test_question_mc, parent, false);
        return new MultipleChoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        TestViewModel.TestQuestionItem item = getItem(position);
        if (holder.getItemViewType() == VIEW_TYPE_TRUE_FALSE) {
            ((TrueFalseViewHolder) holder).bind(item, position + 1, getItemCount());
        } else {
            ((MultipleChoiceViewHolder) holder).bind(item, position + 1, getItemCount());
        }
    }

    // ViewHolder cho Trắc nghiệm
    class MultipleChoiceViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardQuestion;
        TextView textQuestionHeader, textQuestionNumber, textQuestionContent, textAnswerPrompt;
        LinearLayout containerAnswers;

        public MultipleChoiceViewHolder(@NonNull View itemView) {
            super(itemView);
            cardQuestion = itemView.findViewById(R.id.card_question);
            textQuestionHeader = itemView.findViewById(R.id.text_question_header);
            textQuestionNumber = itemView.findViewById(R.id.text_question_number);
            textQuestionContent = itemView.findViewById(R.id.text_question_content);
            textAnswerPrompt = itemView.findViewById(R.id.text_answer_prompt);
            containerAnswers = itemView.findViewById(R.id.container_answers);
        }

        void bind(TestViewModel.TestQuestionItem item, int position, int total) {
            textQuestionHeader.setText("Trắc nghiệm");
            textQuestionNumber.setText(position + " / " + total);
            textQuestionContent.setText(item.quiz.getQuestion());

            if (item.quiz.getCorrectAnswerIndices() != null && item.quiz.getCorrectAnswerIndices().size() > 1) {
                textAnswerPrompt.setText("Chọn một hoặc nhiều đáp án đúng");
            } else {
                textAnswerPrompt.setText("Chọn một đáp án đúng");
            }
            updateCardHighlight(cardQuestion, item);

            containerAnswers.removeAllViews();
            List<String> answers = item.quiz.getAnswers();
            if (answers == null) return;

            for (int i = 0; i < answers.size(); i++) {
                MaterialButton answerButton = new MaterialButton(itemView.getContext(), null, com.google.android.material.R.attr.materialButtonOutlinedStyle);
                answerButton.setText(answers.get(i));
                answerButton.setTag(i);

                boolean isSelected = item.userAnswerIndices.contains(i);
                updateButtonState(answerButton, isSelected);

                answerButton.setOnClickListener(v -> {
                    int clickedIndex = (int) v.getTag();
                    List<Integer> newSelectedIndices = new ArrayList<>(item.userAnswerIndices);

                    if (newSelectedIndices.contains(clickedIndex)) {
                        newSelectedIndices.remove(Integer.valueOf(clickedIndex));
                    } else {
                        if (item.quiz.getCorrectAnswerIndices() != null && item.quiz.getCorrectAnswerIndices().size() == 1) {
                            newSelectedIndices.clear();
                        }
                        newSelectedIndices.add(clickedIndex);
                    }

                    // Cập nhật ViewModel và thông báo cho adapter vẽ lại item này
                    viewModel.onAnswerSelected(item.id, newSelectedIndices);
                    notifyItemChanged(getAdapterPosition());
                });
                containerAnswers.addView(answerButton);
            }
        }
    }

    // ViewHolder cho Đúng/Sai
    class TrueFalseViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardQuestion;
        TextView textQuestionHeader, textQuestionNumber, textQuestionContent;
        // Đổi tên biến cho khớp với ID để dễ đọc hơn (tùy chọn nhưng nên làm)
        MaterialButton btnTrue, btnFalse;

        // === THAY ĐỔI TRONG HÀM KHỞI TẠO ===
        public TrueFalseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardQuestion = itemView.findViewById(R.id.card_question);
            textQuestionHeader = itemView.findViewById(R.id.text_question_header);
            textQuestionNumber = itemView.findViewById(R.id.text_question_number);
            textQuestionContent = itemView.findViewById(R.id.text_question_content);

            // Sử dụng ID đúng từ file XML
            btnTrue = itemView.findViewById(R.id.btn_true);
            btnFalse = itemView.findViewById(R.id.btn_false);
        }

        // === THAY ĐỔI TRONG PHƯƠNG THỨC BIND ===
        void bind(TestViewModel.TestQuestionItem item, int position, int total) {
            textQuestionHeader.setText("Đúng / Sai");
            textQuestionNumber.setText(position + " / " + total);
            // Nội dung câu hỏi giờ đây là sự kết hợp của câu hỏi gốc và một đáp án
            // Để hiển thị đúng theo layout mới của bạn, chúng ta cần tách chúng ra
            String fullQuestion = item.quiz.getQuestion();
            String[] parts = fullQuestion.split("\n\n");
            if (parts.length == 2) {
                textQuestionContent.setText(parts[0]); // Câu hỏi gốc
                // Tìm TextView cho đáp án được hiển thị
                TextView textDisplayedAnswer = itemView.findViewById(R.id.text_displayed_answer);
                if (textDisplayedAnswer != null) {
                    textDisplayedAnswer.setText(parts[1]); // Đáp án đề xuất
                }
            } else {
                // Trường hợp dự phòng nếu không tách được
                textQuestionContent.setText(fullQuestion);
            }

            updateCardHighlight(cardQuestion, item);

            // Logic của bạn giả định đáp án 0 là "Đúng" và 1 là "Sai"
            final int TRUE_INDEX = 0;
            final int FALSE_INDEX = 1;

            // Cập nhật trạng thái cho các nút mới
            updateButtonState(btnTrue, item.userAnswerIndices.contains(TRUE_INDEX));
            updateButtonState(btnFalse, item.userAnswerIndices.contains(FALSE_INDEX));

            // Gắn sự kiện click
            btnTrue.setOnClickListener(v -> {
                viewModel.onAnswerSelected(item.id, Collections.singletonList(TRUE_INDEX));
                notifyItemChanged(getAdapterPosition());
            });
            btnFalse.setOnClickListener(v -> {
                viewModel.onAnswerSelected(item.id, Collections.singletonList(FALSE_INDEX));
                notifyItemChanged(getAdapterPosition());
            });
        }
    }

    private void updateCardHighlight(MaterialCardView card, TestViewModel.TestQuestionItem item) {
        if (item.userAnswerIndices.isEmpty()) {
            card.setStrokeWidth(4);
            card.setStrokeColor(card.getContext().getColor(R.color.unanswered_highlight));
        } else {
            card.setStrokeWidth(0);
        }
    }

    private void updateButtonState(MaterialButton button, boolean isSelected) {
        Context context = button.getContext();
        if (isSelected) {
            int colorPrimary = ContextCompat.getColor(context, R.color.purple_500);
            button.setBackgroundColor(colorPrimary);
            button.setTextColor(ContextCompat.getColor(context, android.R.color.white));
            button.setStrokeWidth(0);
        } else {
            int colorPrimary = ContextCompat.getColor(context, R.color.purple_500);
            button.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            button.setTextColor(colorPrimary);
            button.setStrokeWidth(2);
            button.setStrokeColor(ColorStateList.valueOf(colorPrimary));
        }
    }

    private static final DiffUtil.ItemCallback<TestViewModel.TestQuestionItem> TestQuestionDiffCallback = new DiffUtil.ItemCallback<TestViewModel.TestQuestionItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull TestViewModel.TestQuestionItem oldItem, @NonNull TestViewModel.TestQuestionItem newItem) {
            return oldItem.id == newItem.id;
        }

        @Override
        public boolean areContentsTheSame(@NonNull TestViewModel.TestQuestionItem oldItem, @NonNull TestViewModel.TestQuestionItem newItem) {
            // So sánh lựa chọn của người dùng là đủ cho trường hợp này
            return oldItem.userAnswerIndices.equals(newItem.userAnswerIndices);
        }
    };
}