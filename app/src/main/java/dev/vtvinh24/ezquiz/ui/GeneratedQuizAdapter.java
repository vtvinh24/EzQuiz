package dev.vtvinh24.ezquiz.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.model.GeneratedQuizItem;

public class GeneratedQuizAdapter extends RecyclerView.Adapter<GeneratedQuizAdapter.ViewHolder> {

    private final List<GeneratedQuizItem> quizItems;
    private final Context context;

    public GeneratedQuizAdapter(Context context, List<GeneratedQuizItem> quizItems) {
        this.context = context;
        this.quizItems = quizItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_generated_quiz, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GeneratedQuizItem item = quizItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return quizItems.size();
    }

    // Phương thức để lấy dữ liệu đã được chỉnh sửa
    public List<GeneratedQuizItem> getUpdatedQuizItems() {
        return this.quizItems;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        EditText questionEditText;
        LinearLayout answersContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionEditText = itemView.findViewById(R.id.edit_quiz_question);
            answersContainer = itemView.findViewById(R.id.answers_container);
        }

        void bind(GeneratedQuizItem item) {
            questionEditText.setText(item.question);
            questionEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    item.question = s.toString();
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });

            answersContainer.removeAllViews();
            for (int i = 0; i < item.answers.size(); i++) {
                View answerRow = LayoutInflater.from(context).inflate(R.layout.item_answer_row, answersContainer, false);
                EditText answerText = answerRow.findViewById(R.id.edit_answer_text);
                CheckBox correctCheckbox = answerRow.findViewById(R.id.checkbox_correct);

                answerText.setText(item.answers.get(i));
                correctCheckbox.setChecked(item.correctAnswerIndices.contains(i));

                final int answerIndex = i;

                answerText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        item.answers.set(answerIndex, s.toString());
                    }
                    @Override
                    public void afterTextChanged(Editable s) {}
                });

                correctCheckbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (item.type == Quiz.Type.SINGLE_CHOICE) {
                        item.correctAnswerIndices.clear();
                        if (isChecked) {
                            item.correctAnswerIndices.add(answerIndex);
                        }
                        // Cần thông báo cho adapter cập nhật lại các view khác
                        notifyDataSetChanged();
                    } else { // MULTIPLE_CHOICE
                        if (isChecked) {
                            if (!item.correctAnswerIndices.contains(answerIndex)) {
                                item.correctAnswerIndices.add(answerIndex);
                            }
                        } else {
                            item.correctAnswerIndices.remove(Integer.valueOf(answerIndex));
                        }
                    }
                });
                answersContainer.addView(answerRow);
            }
        }
    }
}