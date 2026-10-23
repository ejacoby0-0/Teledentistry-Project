package com.example.teledentistry_project.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ViewChatAdapter extends RecyclerView.Adapter<ViewChatAdapter.ChatViewHolder> {

    public static class ChatItem {
        public final String chatId;
        public final String otherUserId;
        public final String displayName;
        public final String timestamp;

        public ChatItem(String chatId, String otherUserId, String displayName, String timestamp) {
            this.chatId = chatId;
            this.otherUserId = otherUserId;
            this.displayName = displayName;
            this.timestamp = timestamp;
        }
    }

    public interface OnChatClickListener {
        void onChatClick(ChatItem item);
    }

    private final List<ChatItem> allItems = new ArrayList<>();
    private final List<ChatItem> shownItems = new ArrayList<>();
    private final OnChatClickListener listener;
    private String query = "";

    public ViewChatAdapter(OnChatClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ChatItem> items) {
        allItems.clear();
        allItems.addAll(items);
        applyFilter();
    }

    public void filter(String newQuery) {
        query = newQuery == null ? "" : newQuery.trim().toLowerCase(Locale.getDefault());
        applyFilter();
    }

    private void applyFilter() {
        shownItems.clear();
        for (ChatItem item : allItems) {
            if (query.isEmpty()
                    || item.displayName.toLowerCase(Locale.getDefault()).contains(query)) {
                shownItems.add(item);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem item = shownItems.get(position);
        holder.tvName.setText(item.displayName);
        holder.tvTimestamp.setText(item.timestamp);
        holder.tvUnread.setVisibility(View.GONE);   // unread counts come later, with messages
        holder.itemView.setOnClickListener(v -> listener.onChatClick(item));
    }

    @Override
    public int getItemCount() {
        return shownItems.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivProfile;
        final TextView tvName;
        final TextView tvTimestamp;
        final TextView tvUnread;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivChatProfile);
            tvName = itemView.findViewById(R.id.tvChatUsername);
            tvTimestamp = itemView.findViewById(R.id.tvChatTimestamp);
            tvUnread = itemView.findViewById(R.id.tvUnreadCount);
        }
    }
}