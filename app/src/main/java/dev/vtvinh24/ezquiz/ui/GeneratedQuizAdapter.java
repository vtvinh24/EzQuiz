package dev.vtvinh24.ezquiz.ui;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import dev.vtvinh24.ezquiz.R;
import dev.vtvinh24.ezquiz.data.model.Quiz;
import dev.vtvinh24.ezquiz.data.model.GeneratedQuizItem;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

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
        return quizItems != null ? quizItems.size() : 0;
    }

    public List<GeneratedQuizItem> getUpdatedQuizItems() {
        return quizItems;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView questionTextView;
        private final TextInputEditText correctAnswerEditText;
        private final LinearLayout wrongAnswersContainer;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            questionTextView = itemView.findViewById(R.id.text_question);
            correctAnswerEditText = itemView.findViewById(R.id.edit_correct_answer);
            wrongAnswersContainer = itemView.findViewById(R.id.container_wrong_answers);
        }

        void bind(GeneratedQuizItem item) {
            try {
                // Hiển thị câu hỏi
                questionTextView.setText(item.question);

                // Xử lý câu trả lời đúng
                if (item.correctAnswerIndices != null && !item.correctAnswerIndices.isEmpty()
                    && item.answers != null && !item.answers.isEmpty()) {
                    int correctIndex = item.correctAnswerIndices.get(0);
                    if (correctIndex >= 0 && correctIndex < item.answers.size()) {
                        correctAnswerEditText.setText(item.answers.get(correctIndex));
                    }
                }

                // Xử lý các câu trả lời sai
                wrongAnswersContainer.removeAllViews();
                if (item.answers != null) {
                    for (int i = 0; i < item.answers.size(); i++) {
                        if (!item.correctAnswerIndices.contains(i)) {
                            View wrongAnswerView = LayoutInflater.from(context)
                                .inflate(R.layout.item_wrong_answer, wrongAnswersContainer, false);

                            TextInputEditText wrongAnswerEdit = wrongAnswerView.findViewById(R.id.edit_wrong_answer);
                            wrongAnswerEdit.setText(item.answers.get(i));

                            final int answerIndex = i;
                            wrongAnswerEdit.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    if (answerIndex >= 0 && answerIndex < item.answers.size()) {
                                        item.answers.set(answerIndex, s.toString());
                                    }
                                }

                                @Override
                                public void afterTextChanged(Editable s) {}
                            });

                            wrongAnswersContainer.addView(wrongAnswerView);
                        }
                    }
                }

            } catch (Exception e) {
                Log.e("GeneratedQuizAdapter", "Error binding view holder", e);
            }
        }
    }
}