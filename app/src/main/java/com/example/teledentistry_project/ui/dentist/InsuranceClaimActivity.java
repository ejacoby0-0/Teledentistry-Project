package com.example.teledentistry_project.ui.dentist;

import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.insurance.InsuranceClaim;
import com.example.teledentistry_project.data.repository.InsuranceClaimRepository;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.card.MaterialCardView;
import android.app.AlertDialog;
import android.text.InputType;
import android.widget.EditText;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.view.View;

import com.example.teledentistry_project.data.model.insurance.InsuranceClaimUpdateRequest;

import com.example.teledentistry_project.data.model.insurance.InsuranceClaimRequest;

import java.util.List;
import java.util.Locale;

public class InsuranceClaimActivity extends AppCompatActivity {

    private SessionManager sessionManager;
    private InsuranceClaimRepository insuranceClaimRepository;

    private TextView tvClaimBilling;
    private TextView tvClaimStatus;
    private LinearLayout layoutInsuranceClaims;
    private Button btnCreateInsuranceClaim;

    private String billingId;
    private double billingAmount;
    private double insuranceCoverage;

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

        setContentView(R.layout.activity_insurance_claim);

        insuranceClaimRepository =
                new InsuranceClaimRepository();

        tvClaimBilling =
                findViewById(R.id.tvClaimBilling);

        tvClaimStatus =
                findViewById(R.id.tvClaimStatus);

        layoutInsuranceClaims =
                findViewById(R.id.layoutInsuranceClaims);

        btnCreateInsuranceClaim =
                findViewById(R.id.btnCreateInsuranceClaim);


        billingId =
                getIntent().getStringExtra("billingId");

        billingAmount =
                getIntent().getDoubleExtra(
                        "billingAmount",
                        0
                );

        insuranceCoverage =
                getIntent().getDoubleExtra(
                        "insuranceCoverage",
                        0
                );


        if (billingId == null
                || billingId.isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid billing record.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }


        tvClaimBilling.setText(
                "Billing: "
                        + formatCurrency(billingAmount)
                        + "\nInsurance Coverage: "
                        + formatCurrency(insuranceCoverage)
        );


        btnCreateInsuranceClaim.setEnabled(true);

        btnCreateInsuranceClaim.setOnClickListener(v ->
                showCreateClaimDialog()
        );

        loadInsuranceClaims();
    }

    private void showCreateClaimDialog() {

        EditText inputPolicyNumber =
                new EditText(this);

        inputPolicyNumber.setHint(
                "Policy Number"
        );

        inputPolicyNumber.setInputType(
                InputType.TYPE_CLASS_TEXT
        );

        int padding =
                (int) (20 * getResources()
                        .getDisplayMetrics().density);

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        container.setPadding(
                padding,
                0,
                padding,
                0
        );

        container.addView(
                inputPolicyNumber
        );


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Create Insurance Claim")
                        .setMessage(
                                "Enter the patient's insurance policy number."
                        )
                        .setView(container)
                        .setNegativeButton(
                                "Cancel",
                                null
                        )
                        .setPositiveButton(
                                "Create",
                                null
                        )
                        .create();


        dialog.setOnShowListener(d -> {

            dialog.getButton(
                    AlertDialog.BUTTON_POSITIVE
            ).setOnClickListener(v -> {

                String policyNumber =
                        inputPolicyNumber
                                .getText()
                                .toString()
                                .trim();


                if (policyNumber.isEmpty()) {

                    inputPolicyNumber.setError(
                            "Policy number is required."
                    );

                    return;
                }


                createInsuranceClaim(
                        policyNumber,
                        dialog
                );
            });
        });


        dialog.show();
    }

    private void createInsuranceClaim(
            String policyNumber,
            AlertDialog dialog
    ) {

        btnCreateInsuranceClaim.setEnabled(false);

        InsuranceClaimRequest request =
                new InsuranceClaimRequest(
                        billingId,
                        policyNumber,
                        "submitted"
                );


        insuranceClaimRepository.createInsuranceClaim(
                sessionManager.getAuthToken(),
                request,
                new InsuranceClaimRepository.InsuranceClaimCallback() {

                    @Override
                    public void onSuccess(
                            InsuranceClaim claim
                    ) {

                        btnCreateInsuranceClaim.setEnabled(
                                true
                        );

                        dialog.dismiss();

                        Toast.makeText(
                                InsuranceClaimActivity.this,
                                "Insurance claim created.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadInsuranceClaims();
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        btnCreateInsuranceClaim.setEnabled(
                                true
                        );

                        Toast.makeText(
                                InsuranceClaimActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void loadInsuranceClaims() {

        tvClaimStatus.setText(
                "Loading insurance claims..."
        );

        layoutInsuranceClaims.removeAllViews();


        insuranceClaimRepository.getClaimsByBilling(
                sessionManager.getAuthToken(),
                billingId,
                new InsuranceClaimRepository.InsuranceClaimListCallback() {

                    @Override
                    public void onSuccess(
                            List<InsuranceClaim> claims
                    ) {

                        layoutInsuranceClaims.removeAllViews();

                        if (claims == null
                                || claims.isEmpty()) {

                            tvClaimStatus.setText(
                                    "No insurance claim found."
                            );

                            btnCreateInsuranceClaim.setVisibility(
                                    Button.VISIBLE
                            );

                            return;
                        }

                        // A billing record should only have one
                        // insurance claim in the current MVP.
                        btnCreateInsuranceClaim.setVisibility(
                                Button.GONE
                        );


                        tvClaimStatus.setText(
                                claims.size()
                                        + " insurance claim(s)"
                        );


                        for (InsuranceClaim claim : claims) {

                            displayInsuranceClaim(
                                    claim
                            );
                        }
                    }


                    @Override
                    public void onError(String message) {

                        tvClaimStatus.setText(
                                "Unable to load insurance claims."
                        );

                        Toast.makeText(
                                InsuranceClaimActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void displayInsuranceClaim(
            InsuranceClaim claim
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

        tvTitle.setText(
                "Insurance Claim"
        );

        tvTitle.setTextSize(18);

        tvTitle.setTypeface(
                null,
                Typeface.BOLD
        );

        content.addView(tvTitle);


        addClaimText(
                content,
                "Policy Number: "
                        + safeText(
                        claim.getPolicyNumber()
                )
        );


        addClaimText(
                content,
                "Status: "
                        + formatStatus(
                        claim.getClaimStatus()
                )
        );

        Button btnEditClaim =
                new Button(this);

        btnEditClaim.setText("Edit Claim");
        btnEditClaim.setAllCaps(false);

        LinearLayout.LayoutParams editButtonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );

        editButtonParams.setMargins(
                0,
                16,
                0,
                0
        );

        btnEditClaim.setLayoutParams(
                editButtonParams
        );

        btnEditClaim.setOnClickListener(v ->
                showEditClaimDialog(claim)
        );

        content.addView(btnEditClaim);

        card.addView(content);

        layoutInsuranceClaims.addView(card);
    }

    private void showEditClaimDialog(
            InsuranceClaim claim
    ) {

        LinearLayout container =
                new LinearLayout(this);

        container.setOrientation(
                LinearLayout.VERTICAL
        );

        int padding =
                (int) (20 * getResources()
                        .getDisplayMetrics().density);

        container.setPadding(
                padding,
                0,
                padding,
                0
        );


        // Policy Number
        EditText inputPolicyNumber =
                new EditText(this);

        inputPolicyNumber.setHint(
                "Policy Number"
        );

        inputPolicyNumber.setText(
                claim.getPolicyNumber()
        );

        container.addView(
                inputPolicyNumber
        );


        // Claim Status
        Spinner spinnerStatus =
                new Spinner(this);

        String[] statuses = {
                "submitted",
                "pending",
                "approved",
                "rejected"
        };

        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_item,
                        statuses
                );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerStatus.setAdapter(
                adapter
        );


        // Select current status
        for (int i = 0; i < statuses.length; i++) {

            if (statuses[i].equals(
                    claim.getClaimStatus()
            )) {

                spinnerStatus.setSelection(i);
                break;
            }
        }


        container.addView(
                spinnerStatus
        );


        AlertDialog dialog =
                new AlertDialog.Builder(this)
                        .setTitle("Edit Insurance Claim")
                        .setView(container)
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

                String policyNumber =
                        inputPolicyNumber
                                .getText()
                                .toString()
                                .trim();

                String claimStatus =
                        spinnerStatus
                                .getSelectedItem()
                                .toString();


                if (policyNumber.isEmpty()) {

                    inputPolicyNumber.setError(
                            "Policy number is required."
                    );

                    return;
                }


                updateInsuranceClaim(
                        claim.getId(),
                        policyNumber,
                        claimStatus,
                        dialog
                );
            });
        });


        dialog.show();
    }

    private void updateInsuranceClaim(
            String claimId,
            String policyNumber,
            String claimStatus,
            AlertDialog dialog
    ) {

        InsuranceClaimUpdateRequest request =
                new InsuranceClaimUpdateRequest(
                        policyNumber,
                        claimStatus
                );


        insuranceClaimRepository.updateInsuranceClaim(
                sessionManager.getAuthToken(),
                claimId,
                request,
                new InsuranceClaimRepository.InsuranceClaimCallback() {

                    @Override
                    public void onSuccess(
                            InsuranceClaim claim
                    ) {

                        dialog.dismiss();

                        Toast.makeText(
                                InsuranceClaimActivity.this,
                                "Insurance claim updated.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadInsuranceClaims();
                    }


                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                InsuranceClaimActivity.this,
                                message,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }


    private void addClaimText(
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


    private String safeText(String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return "Not specified";
        }

        return value;
    }


    private String formatStatus(String status) {

        if (status == null
                || status.trim().isEmpty()) {

            return "UNKNOWN";
        }

        return status
                .replace("_", " ")
                .toUpperCase(Locale.US);
    }


    private String formatCurrency(double amount) {

        return String.format(
                Locale.US,
                "R %.2f",
                amount
        );
    }
}
