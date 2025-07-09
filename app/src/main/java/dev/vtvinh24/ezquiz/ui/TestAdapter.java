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


    private static final int VIEW_TYPE_MULTIPLE_CHOICE = 1;
    private static final int VIEW_TYPE_TRUE_FALSE = 2;

    public TestAdapter(TestViewModel viewModel) {
        super(TestQuestionDiffCallback);
        this.viewModel = viewModel;
    }

    @Override
    public int getItemViewType(int position) {
        TestViewModel.TestQuestionItem item = getItem(position);

        if (item.quiz.getAnswers() != null && item.quiz.getAnswers().size() == 2) {

            // Note: These strings are for logic, not display. Do not extract.
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
            Context context = itemView.getContext();
            textQuestionHeader.setText(R.string.question_header_multiple_choice);
            textQuestionNumber.setText(context.getString(R.string.test_question_progress_format, position, total));
            textQuestionContent.setText(item.quiz.getQuestion());

            if (item.quiz.getCorrectAnswerIndices() != null && item.quiz.getCorrectAnswerIndices().size() > 1) {
                textAnswerPrompt.setText(R.string.prompt_select_multiple_answers);
            } else {
                textAnswerPrompt.setText(R.string.prompt_select_one_answer);
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


                    viewModel.onAnswerSelected(item.id, newSelectedIndices);
                    notifyItemChanged(getAdapterPosition());
                });
                containerAnswers.addView(answerButton);
            }
        }
    }


    class TrueFalseViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView cardQuestion;
        TextView textQuestionHeader, textQuestionNumber, textQuestionContent;

        MaterialButton btnTrue, btnFalse;


        public TrueFalseViewHolder(@NonNull View itemView) {
            super(itemView);
            cardQuestion = itemView.findViewById(R.id.card_question);
            textQuestionHeader = itemView.findViewById(R.id.text_question_header);
            textQuestionNumber = itemView.findViewById(R.id.text_question_number);
            textQuestionContent = itemView.findViewById(R.id.text_question_content);

            btnTrue = itemView.findViewById(R.id.btn_true);
            btnFalse = itemView.findViewById(R.id.btn_false);
        }


        void bind(TestViewModel.TestQuestionItem item, int position, int total) {
            Context context = itemView.getContext();
            textQuestionHeader.setText(R.string.question_header_true_false);
            textQuestionNumber.setText(context.getString(R.string.test_question_progress_format, position, total));

            String fullQuestion = item.quiz.getQuestion();
            String[] parts = fullQuestion.split("\n\n");
            if (parts.length == 2) {
                textQuestionContent.setText(parts[0]);
                // Tìm TextView cho đáp án được hiển thị
                TextView textDisplayedAnswer = itemView.findViewById(R.id.text_displayed_answer);
                if (textDisplayedAnswer != null) {
                    textDisplayedAnswer.setText(parts[1]);
                }
            } else {

                textQuestionContent.setText(fullQuestion);
            }

            updateCardHighlight(cardQuestion, item);


            final int TRUE_INDEX = 0;
            final int FALSE_INDEX = 1;


            updateButtonState(btnTrue, item.userAnswerIndices.contains(TRUE_INDEX));
            updateButtonState(btnFalse, item.userAnswerIndices.contains(FALSE_INDEX));


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

            return oldItem.userAnswerIndices.equals(newItem.userAnswerIndices);
        }
    };
}