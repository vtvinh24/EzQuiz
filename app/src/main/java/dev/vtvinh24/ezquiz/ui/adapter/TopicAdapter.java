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
        holder.bind(topic, listener);
    }

    @Override
    public int getItemCount() {
        return topics.size();
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        private final Chip chipTopic;

        public TopicViewHolder(@NonNull View itemView) {
            super(itemView);
            chipTopic = (Chip) itemView;
        }

        public void bind(String topic, OnTopicClickListener listener) {
            chipTopic.setText(topic);
            chipTopic.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onTopicClick(topic);
                }
            });
        }
    }
}
