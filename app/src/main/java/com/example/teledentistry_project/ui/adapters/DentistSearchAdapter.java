package com.example.teledentistry_project.ui.adapters;

import android.content.Context;
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

public class DentistSearchAdapter
        extends RecyclerView.Adapter<DentistSearchAdapter.DentistViewHolder> {

    /** What the search screen needs to know about one practitioner. */
    public static class DentistItem {
        public final String id;
        public final String name;          // display name, e.g. "Dr. Jane Smith"
        public final String practiceName;
        public final String location;
        public final double cost;

        public DentistItem(String id, String name, String practiceName,
                           String location, double cost) {
            this.id = id;
            this.name = name;
            this.practiceName = practiceName;
            this.location = location;
            this.cost = cost;
        }
    }

    public interface OnDentistClickListener {
        void onDentistClick(DentistItem item);
    }

    private final List<DentistItem> items = new ArrayList<>();
    private final OnDentistClickListener listener;

    public DentistSearchAdapter(OnDentistClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<DentistItem> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DentistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dentist_search, parent, false);
        return new DentistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DentistViewHolder holder, int position) {
        DentistItem item = items.get(position);
        Context context = holder.itemView.getContext();

        holder.tvName.setText(item.name);
        holder.tvPractice.setText(item.practiceName);
        holder.tvLocation.setText(item.location);

        // Whole numbers show without decimals: 650 instead of 650.00
        String amount = (item.cost == Math.floor(item.cost))
                ? String.valueOf((long) item.cost)
                : String.format(Locale.getDefault(), "%.2f", item.cost);
        holder.tvCost.setText(context.getString(R.string.session_cost_format, amount));

        holder.itemView.setOnClickListener(v -> listener.onDentistClick(item));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class DentistViewHolder extends RecyclerView.ViewHolder {
        final ImageView ivProfile;
        final TextView tvName;
        final TextView tvPractice;
        final TextView tvLocation;
        final TextView tvCost;

        DentistViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivDentistProfile);
            tvName = itemView.findViewById(R.id.tvDentistName);
            tvPractice = itemView.findViewById(R.id.tvPracticeName);
            tvLocation = itemView.findViewById(R.id.tvPracticeLocation);
            tvCost = itemView.findViewById(R.id.tvSessionCost);
        }
    }
}