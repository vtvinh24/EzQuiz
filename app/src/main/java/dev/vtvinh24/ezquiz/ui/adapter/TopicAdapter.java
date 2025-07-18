package dev.vtvinh24.ezquiz.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.chip.Chip;
import dev.vtvinh24.ezquiz.R;
import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {

    public interface OnTopicClickListener {
        void onTopicClick(String topic);
    }

    private final List<String> topics;
    private final OnTopicClickListener listener;
    private int selectedPosition = -1;

    public TopicAdapter(List<String> topics, OnTopicClickListener listener) {
        this.topics = topics;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_topic_chip, parent, false);
        return new TopicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        String topic = topics.get(position);
        boolean isSelected = position == selectedPosition;
        holder.bind(topic, isSelected, position, listener, this);
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    public void setSelectedPosition(int position) {
        int previousSelected = selectedPosition;
        selectedPosition = position;

        if (previousSelected != -1) {
            notifyItemChanged(previousSelected);
        }
        if (position != -1) {
            notifyItemChanged(position);
        }
    }

    public String getSelectedTopic() {
        if (selectedPosition >= 0 && selectedPosition < topics.size()) {
            return topics.get(selectedPosition);
        }
        return null;
    }

    public void clearSelection() {
        int previousSelected = selectedPosition;
        selectedPosition = -1;
        if (previousSelected != -1) {
            notifyItemChanged(previousSelected);
        }
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        private final Chip chipTopic;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            chipTopic = (Chip) itemView;
        }

        public void bind(String topic, boolean isSelected, int position, OnTopicClickListener listener, TopicAdapter adapter) {
            chipTopic.setText(topic);
            chipTopic.setChecked(isSelected);
            chipTopic.setOnClickListener(v -> {
                if (isSelected) {
                    adapter.clearSelection();
                    if (listener != null) {
                        listener.onTopicClick(null);
                    }
                } else {
                    adapter.setSelectedPosition(position);
                    if (listener != null) {
                        listener.onTopicClick(topic);
                    }
                }
            });
        }
    }
}
