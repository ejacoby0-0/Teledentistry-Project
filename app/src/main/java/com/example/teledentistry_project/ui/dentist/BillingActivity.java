package com.example.teledentistry_project.ui.dentist;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.billing.Billing;
import com.example.teledentistry_project.data.repository.BillingRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import com.example.teledentistry_project.data.model.billing.BillingRequest;
import com.example.teledentistry_project.data.model.billing.BillingUpdateRequest;

import java.util.List;
import java.util.Locale;

public class BillingActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private BillingRepository billingRepository;

    private TextView tvBillingAppointment;
    private TextView tvBillingStatus;
    private LinearLayout layoutBillingRecords;
    private Button btnCreateBilling;
    private Button btnPayNow;

    private String appointmentId;
    private String appointmentDate;
    private String appointmentStartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        if (!"practitioner".equals(sessionManager.getRole())) {

            Toast.makeText(
                    this,
                    "Access denied.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
            return;
        }

        setContentView(R.layout.activity_billing);

        billingRepository = new BillingRepository();

        tvBillingAppointment =
                findViewById(R.id.tvBillingAppointment);

        tvBillingStatus =
                findViewById(R.id.tvBillingStatus);

        layoutBillingRecords =
                findViewById(R.id.layoutBillingRecords);

        btnCreateBilling =
                findViewById(R.id.btnCreateBilling);

        btnPayNow =
                findViewById(R.id.btnPayNow);


        appointmentId =
                getIntent().getStringExtra("appointmentId");

        appointmentDate =
                getIntent().getStringExtra("appointmentDate");

        appointmentStartTime =
                getIntent().getStringExtra("appointmentStartTime");


        if (appointmentId == null
                || appointmentId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid appointment.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }


        if (appointmentDate != null
                && !appointmentDate.isEmpty()) {

            String appointmentText =
                    "Appointment: " + appointmentDate;

            if (appointmentStartTime != null
                    && !appointmentStartTime.isEmpty()) {

                appointmentText +=
                        " at " + appointmentStartTime;
            }

            tvBillingAppointment.setText(
                    appointmentText
            );

        } else {

            tvBillingAppointment.setText(
                    "Appointment ID: " + appointmentId
            );
        }


        btnCreateBilling.setEnabled(true);

        btnCreateBilling.setOnClickListener(v ->
                showCreateBillingDialog()
        );

        // Real payment integration comes later.
        btnPayNow.setVisibility(View.GONE);

        loadBilling();
    }

    private void showCreateBillingDialog() {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 0);


        EditText etAmount = new EditText(this);
        etAmount.setHint("Total Amount (R)");
        etAmount.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        layout.addView(etAmount);


        EditText etInsuranceCoverage = new EditText(this);
        etInsuranceCoverage.setHint("Insurance Coverage (R)");
        etInsuranceCoverage.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );
        layout.addView(etInsuranceCoverage);


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Create Billing Record")
                        .setView(layout)
                        .setNegativeButton("Cancel", null)
                        .setPositiveButton("Create", null)
                        .create();


        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String amountText =
                        etAmount.getText()
                                .toString()
                                .trim();

                String insuranceText =
                        etInsuranceCoverage.getText()
                                .toString()
                                .trim();


                if (amountText.isEmpty()) {

                    Toast.makeText(
                            BillingActivity.this,
                            "Please enter the total amount.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                // Allow no insurance coverage.
                if (insuranceText.isEmpty()) {
                    insuranceText = "0";
                }


                double amount;
                double insuranceCoverage;

                try {

                    amount =
                            Double.parseDouble(
                                    amountText
                            );

                    insuranceCoverage =
                            Double.parseDouble(
                                    insuranceText
                            );

                } catch (NumberFormatException e) {

                    Toast.makeText(
                            BillingActivity.this,
                            "Please enter valid amounts.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (amount <= 0) {

                    Toast.makeText(
                            BillingActivity.this,
                            "Total amount must be greater than zero.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (insuranceCoverage < 0) {

                    Toast.makeText(
                            BillingActivity.this,
                            "Insurance coverage cannot be negative.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (insuranceCoverage > amount) {

                    Toast.makeText(
                            BillingActivity.this,
                            "Insurance coverage cannot exceed the total amount.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                createBillingRecord(
                        amount,
                        insuranceCoverage,
                        dialog
                );
            });
        });

        dialog.show();
    }

    private void createBillingRecord(
            double amount,
            double insuranceCoverage,
            AlertDialog dialog
    ) {

        BillingRequest request =
                new BillingRequest(
                        appointmentId,
                        amount,
                        insuranceCoverage,
                        "pending"
                );


        billingRepository.createBilling(
                sessionManager.getAuthToken(),
                request,
                new BillingRepository.BillingCallback() {

                    @Override
                    public void onSuccess(Billing billing) {

                        Toast.makeText(
                                BillingActivity.this,
                                "Billing record created successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();

                        loadBilling();
                    }


                    @Override
                    public void onError(String message) {

                        new AlertDialog.Builder(
                                BillingActivity.this
                        )
                                .setTitle("Billing Error")
                                .setMessage(message)
                                .setPositiveButton(
                                        "OK",
                                        null
                                )
                                .show();
                    }
                }
        );
    }


    private void loadBilling() {

        tvBillingStatus.setText(
                "Loading billing..."
        );

        layoutBillingRecords.removeAllViews();

        billingRepository.getBillingByAppointment(
                sessionManager.getAuthToken(),
                appointmentId,
                new BillingRepository.BillingListCallback() {

                    @Override
                    public void onSuccess(
                            List<Billing> billingRecords
                    ) {

                        layoutBillingRecords.removeAllViews();

                        if (billingRecords == null
                                || billingRecords.isEmpty()) {

                            tvBillingStatus.setText(
                                    "No billing record found."
                            );

                            btnCreateBilling.setEnabled(true);
                            btnCreateBilling.setVisibility(View.VISIBLE);

                            return;
                        }

                        // One primary billing record per appointment
                        btnCreateBilling.setEnabled(false);
                        btnCreateBilling.setVisibility(View.GONE);


                        tvBillingStatus.setText(
                                billingRecords.size()
                                        + " billing record(s)"
                        );


                        for (Billing billing
                                : billingRecords) {

                            displayBillingRecord(
                                    billing
                            );
                        }
                    }


                    @Override
                    public void onError(String message) {

                        tvBillingStatus.setText(
                                "Unable to load billing."
                        );

                        Toast.makeText(
                                BillingActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void displayBillingRecord(
            Billing billing
    ) {

        MaterialCardView card =
                new MaterialCardView(this);

        LinearLayout.LayoutParams cardParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        cardParams.setMargins(
                0,
                0,
                0,
                24
        );

        card.setLayoutParams(cardParams);
        card.setRadius(16);
        card.setCardElevation(4);


        LinearLayout content =
                new LinearLayout(this);

        content.setOrientation(
                LinearLayout.VERTICAL
        );

        content.setPadding(
                32,
                32,
                32,
                32
        );


        TextView tvTitle =
                new TextView(this);

        tvTitle.setText("Billing Record");
        tvTitle.setTextSize(18);

        tvTitle.setTypeface(
                null,
                Typeface.BOLD
        );

        content.addView(tvTitle);


        addBillingText(
                content,
                "Amount: "
                        + formatCurrency(
                        billing.getAmount()
                )
        );


        addBillingText(
                content,
                "Insurance Coverage: "
                        + formatCurrency(
                        billing.getInsuranceCoverage()
                )
        );


        double patientBalance =
                billing.getAmount()
                        - billing.getInsuranceCoverage();

        // Avoid displaying a negative balance.
        if (patientBalance < 0) {
            patientBalance = 0;
        }

        addBillingText(
                content,
                "Patient Balance: "
                        + formatCurrency(
                        patientBalance
                )
        );


        addBillingText(
                content,
                "Status: "
                        + formatStatus(
                        billing.getPaymentStatus()
                )
        );


        card.addView(content);

        Button btnEditBilling =
                new Button(this);

        btnEditBilling.setText(
                "Edit Billing"
        );

        btnEditBilling.setAllCaps(false);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        buttonParams.setMargins(
                0,
                24,
                0,
                0
        );

        btnEditBilling.setLayoutParams(
                buttonParams
        );

        btnEditBilling.setOnClickListener(v ->
                showEditBillingDialog(billing)
        );

        content.addView(
                btnEditBilling
        );

        layoutBillingRecords.addView(card);
    }

    private void showEditBillingDialog(
            Billing billing
    ) {

        LinearLayout layout =
                new LinearLayout(this);

        layout.setOrientation(
                LinearLayout.VERTICAL
        );

        layout.setPadding(
                40,
                20,
                40,
                0
        );


        EditText etAmount =
                new EditText(this);

        etAmount.setHint(
                "Total Amount (R)"
        );

        etAmount.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        etAmount.setText(
                String.valueOf(
                        billing.getAmount()
                )
        );

        layout.addView(etAmount);


        EditText etInsuranceCoverage =
                new EditText(this);

        etInsuranceCoverage.setHint(
                "Insurance Coverage (R)"
        );

        etInsuranceCoverage.setInputType(
                InputType.TYPE_CLASS_NUMBER
                        | InputType.TYPE_NUMBER_FLAG_DECIMAL
        );

        etInsuranceCoverage.setText(
                String.valueOf(
                        billing.getInsuranceCoverage()
                )
        );

        layout.addView(
                etInsuranceCoverage
        );


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Edit Billing")
                        .setView(layout)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Save",
                                null
                        )
                        .create();


        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String amountText =
                        etAmount.getText()
                                .toString()
                                .trim();

                String insuranceText =
                        etInsuranceCoverage.getText()
                                .toString()
                                .trim();


                if (amountText.isEmpty()) {

                    Toast.makeText(
                            BillingActivity.this,
                            "Please enter the total amount.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (insuranceText.isEmpty()) {
                    insuranceText = "0";
                }


                double amount;
                double insuranceCoverage;

                try {

                    amount =
                            Double.parseDouble(
                                    amountText
                            );

                    insuranceCoverage =
                            Double.parseDouble(
                                    insuranceText
                            );

                } catch (NumberFormatException e) {

                    Toast.makeText(
                            BillingActivity.this,
                            "Please enter valid amounts.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (amount <= 0) {

                    Toast.makeText(
                            BillingActivity.this,
                            "Total amount must be greater than zero.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (insuranceCoverage < 0
                        || insuranceCoverage > amount) {

                    Toast.makeText(
                            BillingActivity.this,
                            "Insurance coverage must be between R0 and the total amount.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                updateBillingRecord(
                        billing,
                        amount,
                        insuranceCoverage,
                        dialog
                );
            });
        });

        dialog.show();
    }

    private void updateBillingRecord(
            Billing billing,
            double amount,
            double insuranceCoverage,
            AlertDialog dialog
    ) {

        BillingUpdateRequest request =
                new BillingUpdateRequest(
                        amount,
                        insuranceCoverage,
                        billing.getPaymentStatus()
                );


        billingRepository.updateBilling(
                sessionManager.getAuthToken(),
                billing.getId(),
                request,
                new BillingRepository.BillingCallback() {

                    @Override
                    public void onSuccess(
                            Billing updatedBilling
                    ) {

                        Toast.makeText(
                                BillingActivity.this,
                                "Billing updated successfully.",
                                Toast.LENGTH_SHORT
                        ).show();

                        dialog.dismiss();

                        loadBilling();
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        new AlertDialog.Builder(
                                BillingActivity.this
                        )
                                .setTitle(
                                        "Billing Error"
                                )
                                .setMessage(message)
                                .setPositiveButton(
                                        "OK",
                                        null
                                )
                                .show();
                    }
                }
        );
    }

    private void addBillingText(
            LinearLayout layout,
            String text
    ) {

        TextView textView =
                new TextView(this);

        textView.setText(text);
        textView.setTextSize(16);

        textView.setPadding(
                0,
                10,
                0,
                0
        );

        layout.addView(textView);
    }


    private String formatCurrency(
            double amount
    ) {

        return String.format(
                Locale.US,
                "R %.2f",
                amount
        );
    }


    private String formatStatus(
            String status
    ) {

        if (status == null
                || status.trim().isEmpty()) {

            return "UNKNOWN";
        }

        return status
                .replace("_", " ")
                .toUpperCase(Locale.US);
    }
}
