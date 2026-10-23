package com.example.teledentistry_project.ui.userActivities;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.receipt.Receipt;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.utils.SessionManager;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReceiptActivity extends AppCompatActivity {

    private RecyclerView rvReceipts;
    private TextView tvNoReceipts;

    private View navbarPatient;
    private View navbarPractitioner;

    private ReceiptAdapter adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipts);

        sessionManager = new SessionManager(this);

        rvReceipts = findViewById(R.id.rvReceipts);
        tvNoReceipts = findViewById(R.id.tvNoReceipts);
        navbarPatient = findViewById(R.id.navbarPatient);
        navbarPractitioner = findViewById(R.id.navbarPractitioner);

        rvReceipts.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ReceiptAdapter();
        rvReceipts.setAdapter(adapter);

        if (!sessionManager.isLoggedIn()) {
            Toast.makeText(
                    this,
                    "Please log in to view receipts.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        String userId = sessionManager.getUserId();
        String token = sessionManager.getAuthToken();
        String role = sessionManager.getRole();

        if (userId == null || userId.trim().isEmpty()
                || token == null || token.trim().isEmpty()) {
            Toast.makeText(
                    this,
                    "Your session is incomplete. Please log in again.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        if (role == null) {
            Toast.makeText(
                    this,
                    "Could not determine your account role.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        role = role.trim().toLowerCase(Locale.ROOT);

        if (!role.equals("patient")
                && !role.equals("practitioner")) {
            Toast.makeText(
                    this,
                    "Unsupported account role.",
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }

        showCorrectNavbar(role);
        loadReceipts(token, userId, role);
    }

    private void showCorrectNavbar(String role) {
        navbarPatient.setVisibility(
                "patient".equals(role) ? View.VISIBLE : View.GONE
        );

        navbarPractitioner.setVisibility(
                "practitioner".equals(role) ? View.VISIBLE : View.GONE
        );
    }

    private void loadReceipts(
            String token,
            String userId,
            String role
    ) {
        tvNoReceipts.setVisibility(View.GONE);
        rvReceipts.setVisibility(View.VISIBLE);

        // Filter receipts belonging to the logged-in user.
        String filter;

        if ("patient".equals(role)) {
            filter = "patient = \"" + userId + "\"";
        } else {
            filter = "practitioner = \"" + userId + "\"";
        }

        PocketBaseClient.api.getReceipts(
                token,
                filter,
                "-created",
                200
        ).enqueue(new Callback<PocketBaseListResponse<Receipt>>() {

            @Override
            public void onResponse(
                    @NonNull Call<PocketBaseListResponse<Receipt>> call,
                    @NonNull Response<PocketBaseListResponse<Receipt>> response
            ) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<Receipt> receipts = response.body().getItems();

                    if (receipts == null || receipts.isEmpty()) {
                        adapter.setReceipts(new ArrayList<>());
                        tvNoReceipts.setVisibility(View.VISIBLE);
                        rvReceipts.setVisibility(View.GONE);
                    } else {
                        adapter.setReceipts(receipts);
                        tvNoReceipts.setVisibility(View.GONE);
                        rvReceipts.setVisibility(View.VISIBLE);
                    }
                } else {
                    tvNoReceipts.setVisibility(View.GONE);
                    Toast.makeText(
                            ReceiptActivity.this,
                            "Could not load receipts. HTTP "
                                    + response.code(),
                            Toast.LENGTH_LONG
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<PocketBaseListResponse<Receipt>> call,
                    @NonNull Throwable t
            ) {
                if (isFinishing() || isDestroyed()) {
                    return;
                }

                tvNoReceipts.setVisibility(View.GONE);

                Toast.makeText(
                        ReceiptActivity.this,
                        "Network error while loading receipts.",
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }

    private class ReceiptAdapter
            extends RecyclerView.Adapter<ReceiptAdapter.ReceiptViewHolder> {

        private final List<Receipt> receipts = new ArrayList<>();

        void setReceipts(List<Receipt> newReceipts) {
            receipts.clear();

            if (newReceipts != null) {
                receipts.addAll(newReceipts);
            }

            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ReceiptViewHolder onCreateViewHolder(
                @NonNull ViewGroup parent,
                int viewType
        ) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_reciept, parent, false);

            return new ReceiptViewHolder(view);
        }

        @Override
        public void onBindViewHolder(
                @NonNull ReceiptViewHolder holder,
                int position
        ) {
            Receipt receipt = receipts.get(position);

            String role = sessionManager.getRole();
            boolean isPatient = role != null
                    && role.equalsIgnoreCase("patient");

            holder.tvReceiptLabel.setText(
                    isPatient ? "Paid to" : "Paid by"
            );

            // These fields contain relation IDs, not actual names.
            String otherPartyId = isPatient
                    ? receipt.getPractitioner()
                    : receipt.getPatient();

            if (otherPartyId == null || otherPartyId.trim().isEmpty()) {
                holder.tvReceiptName.setText("Unknown");
            } else {
                holder.tvReceiptName.setText(otherPartyId);
            }

            holder.tvReceiptDate.setText(
                    formatDate(receipt.getCreated())
            );

            holder.tvReceiptCost.setText(
                    new DecimalFormat("'R' #,##0.00")
                            .format(receipt.getCost())
            );
        }

        @Override
        public int getItemCount() {
            return receipts.size();
        }

        class ReceiptViewHolder extends RecyclerView.ViewHolder {

            TextView tvReceiptLabel;
            TextView tvReceiptName;
            TextView tvReceiptDate;
            TextView tvReceiptCost;

            ReceiptViewHolder(@NonNull View itemView) {
                super(itemView);

                tvReceiptLabel = itemView.findViewById(
                        R.id.tvReceiptLabel
                );
                tvReceiptName = itemView.findViewById(
                        R.id.tvReceiptName
                );
                tvReceiptDate = itemView.findViewById(
                        R.id.tvReceiptDate
                );
                tvReceiptCost = itemView.findViewById(
                        R.id.tvReceiptCost
                );
            }
        }
    }

    private String formatDate(String created) {
        if (created == null || created.trim().isEmpty()) {
            return "Date unavailable";
        }

        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd HH:mm:ss.SSS'Z'"
        };

        for (String pattern : patterns) {
            SimpleDateFormat input = new SimpleDateFormat(
                    pattern,
                    Locale.US
            );
            input.setTimeZone(TimeZone.getTimeZone("UTC"));
            input.setLenient(false);

            try {
                Date date = input.parse(created);

                if (date != null) {
                    SimpleDateFormat output = new SimpleDateFormat(
                            "dd MMMM yyyy",
                            Locale.getDefault()
                    );
                    return output.format(date);
                }
            } catch (ParseException ignored) {
                // Try the next supported timestamp format.
            }
        }

        // Keep the screen usable if PocketBase returns an
        // unexpected timestamp format.
        return created;
    }
}
