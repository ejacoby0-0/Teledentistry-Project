
package com.example.teledentistry_project.ui.dentist;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.R;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class PrescriptionActivity extends AppCompatActivity {

    private static final String BASE_URL = "http://10.0.2.2:8090";
    private static final MediaType JSON =
            MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient client = new OkHttpClient();

    private SessionManager session;

    private String patientId;
    private String practitionerId;
    private String token;

    private TextView tvPatientName;
    private TextView tvPatientPhone;
    private TextView tvPatientEmail;
    private TextView tvPatientDob;
    private TextView tvNoPrescriptionsGiven;

    private TextInputEditText etPatientNotes;
    private TextInputEditText etNewPrescription;

    private RecyclerView rvPrescriptionsGiven;
    private PrescriptionGivenAdapter prescriptionAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_details);

        session = new SessionManager(this);

        if (!session.isLoggedIn()) {
            finish();
            return;
        }

        patientId = getIntent().getStringExtra("patientId");
        practitionerId = session.getUserId();
        token = session.getAuthToken();

        if (patientId == null || patientId.trim().isEmpty()) {
            Toast.makeText(this, "Patient ID is missing.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        bindViews();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        rvPrescriptionsGiven.setLayoutManager(
                new LinearLayoutManager(this));

        prescriptionAdapter = new PrescriptionGivenAdapter();
        rvPrescriptionsGiven.setAdapter(prescriptionAdapter);

        findViewById(R.id.btnSaveNotes).setOnClickListener(
                v -> saveNotes());

        findViewById(R.id.btnSendPrescription).setOnClickListener(
                v -> sendPrescription());

        loadPatient();
        loadPrescriptions();
    }

    private void bindViews() {
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientPhone = findViewById(R.id.tvPatientPhone);
        tvPatientEmail = findViewById(R.id.tvPatientEmail);
        tvPatientDob = findViewById(R.id.tvPatientDob);

        etPatientNotes = findViewById(R.id.etPatientNotes);
        etNewPrescription = findViewById(R.id.etNewPrescription);

        rvPrescriptionsGiven = findViewById(R.id.rvPrescriptionsGiven);
        tvNoPrescriptionsGiven =
                findViewById(R.id.tvNoPrescriptionsGiven);
    }

    private void loadPatient() {
        Request request = new Request.Builder()
                .url(BASE_URL + "/api/collections/users/records/"
                        + patientId)
                .header("Authorization", token)
                .get()
                .build();

        executeAsync(request, (body, error) -> {
            if (error != null) {
                showToast("Could not load patient details.");
                return;
            }

            JsonObject user = JsonParser.parseString(body)
                    .getAsJsonObject();

            runOnUiThread(() -> {
                tvPatientName.setText(
                        (getString(user, "name") + " "
                                + getString(user, "surname")).trim());

                tvPatientPhone.setText(
                        valueOrFallback(getString(user, "phone")));

                tvPatientEmail.setText(
                        valueOrFallback(getString(user, "email")));

                String dob = getString(user, "dateOfBirth");
                if (dob.isEmpty()) {
                    dob = getString(user, "dob");
                }
                tvPatientDob.setText(valueOrFallback(dob));

                etPatientNotes.setText(getString(user, "notes"));
            });
        });
    }

    private void loadPrescriptions() {
        HttpUrl url = HttpUrl.parse(
                BASE_URL + "/api/collections/prescriptions/records");

        if (url == null) {
            showToast("Invalid PocketBase URL.");
            return;
        }

        url = url.newBuilder()
                .addQueryParameter(
                        "filter",
                        "patient = \"" + patientId + "\"")
                .addQueryParameter("expand", "practitioner")
                .addQueryParameter("sort", "-created")
                .addQueryParameter("perPage", "200")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", token)
                .get()
                .build();

        executeAsync(request, (body, error) -> {
            if (error != null) {
                showToast("Could not load prescriptions.");
                return;
            }

            JsonObject result = JsonParser.parseString(body)
                    .getAsJsonObject();

            JsonArray items = result.has("items")
                    && result.get("items").isJsonArray()
                    ? result.getAsJsonArray("items")
                    : new JsonArray();

            runOnUiThread(() -> {
                prescriptionAdapter.setItems(items);

                boolean empty = items.size() == 0;
                tvNoPrescriptionsGiven.setVisibility(
                        empty ? View.VISIBLE : View.GONE);
                rvPrescriptionsGiven.setVisibility(
                        empty ? View.GONE : View.VISIBLE);
            });
        });
    }

    private void saveNotes() {
        String notes = etPatientNotes.getText() == null
                ? ""
                : etPatientNotes.getText().toString().trim();

        JsonObject data = new JsonObject();
        data.addProperty("notes", notes);

        RequestBody body = RequestBody.create(
                data.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL + "/api/collections/users/records/"
                        + patientId)
                .header("Authorization", token)
                .patch(body)
                .build();

        executeAsync(request, (responseBody, error) -> {
            if (error != null) {
                showToast("Could not save notes. Check your permissions "
                        + "and the users collection fields.");
            } else {
                showToast("Patient notes saved.");
            }
        });
    }

    private void sendPrescription() {
        String note = etNewPrescription.getText() == null
                ? ""
                : etNewPrescription.getText().toString().trim();

        if (note.isEmpty()) {
            etNewPrescription.setError(
                    "Please enter the prescription.");
            return;
        }

        if (practitionerId == null || practitionerId.trim().isEmpty()) {
            showToast("Practitioner session is missing.");
            return;
        }

        JsonObject data = new JsonObject();
        data.addProperty("patient", patientId);
        data.addProperty("practitioner", practitionerId);
        data.addProperty("prescriptionNote", note);

        RequestBody body = RequestBody.create(
                data.toString(), JSON);

        Request request = new Request.Builder()
                .url(BASE_URL
                        + "/api/collections/prescriptions/records")
                .header("Authorization", token)
                .post(body)
                .build();

        executeAsync(request, (responseBody, error) -> {
            if (error != null) {
                showToast("Could not send prescription. Check the "
                        + "PocketBase collection rules and fields.");
                return;
            }

            runOnUiThread(() -> {
                etNewPrescription.setText("");
                Toast.makeText(this, "Prescription sent.",
                        Toast.LENGTH_SHORT).show();
            });

            loadPrescriptions();
        });
    }

    private interface HttpResult {
        void onResult(String body, IOException error);
    }

    private void executeAsync(Request request, HttpResult callback) {
        client.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(
                    okhttp3.Call call, IOException e) {
                callback.onResult(null, e);
            }

            @Override
            public void onResponse(
                    okhttp3.Call call, Response response) {
                try (Response res = response) {
                    String body = res.body() == null
                            ? "" : res.body().string();

                    if (!res.isSuccessful()) {
                        callback.onResult(null,
                                new IOException("HTTP "
                                        + res.code() + ": " + body));
                    } else {
                        callback.onResult(body, null);
                    }
                } catch (IOException e) {
                    callback.onResult(null, e);
                }
            }
        });
    }

    private String getString(JsonObject object, String field) {
        if (object == null || !object.has(field)
                || object.get(field).isJsonNull()) {
            return "";
        }

        return object.get(field).getAsString();
    }

    private String valueOrFallback(String value) {
        return value == null || value.trim().isEmpty()
                ? "Not provided" : value;
    }

    private void showToast(String message) {
        runOnUiThread(() ->
                Toast.makeText(this, message,
                        Toast.LENGTH_SHORT).show());
    }

    private class PrescriptionGivenAdapter
            extends RecyclerView.Adapter<
            PrescriptionGivenAdapter.PrescriptionViewHolder> {

        private final java.util.List<JsonObject> items =
                new java.util.ArrayList<>();

        void setItems(JsonArray array) {
            items.clear();

            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isJsonObject()) {
                    items.add(array.get(i).getAsJsonObject());
                }
            }

            notifyDataSetChanged();
        }

        @Override
        public PrescriptionViewHolder onCreateViewHolder(
                android.view.ViewGroup parent, int viewType) {

            android.widget.LinearLayout layout =
                    new android.widget.LinearLayout(
                            parent.getContext());

            layout.setOrientation(
                    android.widget.LinearLayout.VERTICAL);
            layout.setPadding(0, 12, 0, 12);

            TextView date = new TextView(parent.getContext());
            date.setTextSize(12);
            date.setTextColor(
                    android.graphics.Color.DKGRAY);

            TextView note = new TextView(parent.getContext());
            note.setTextSize(15);

            layout.addView(date);
            layout.addView(note);

            return new PrescriptionViewHolder(layout, date, note);
        }

        @Override
        public void onBindViewHolder(
                PrescriptionViewHolder holder, int position) {

            JsonObject item = items.get(position);

            holder.date.setText(
                    "Date: " + formatDate(getString(item, "created")));

            String note = getString(item, "prescriptionNote");
            holder.note.setText(note.isEmpty()
                    ? "No prescription details." : note);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class PrescriptionViewHolder
                extends RecyclerView.ViewHolder {

            TextView date;
            TextView note;

            PrescriptionViewHolder(
                    View itemView, TextView date, TextView note) {
                super(itemView);
                this.date = date;
                this.note = note;
            }
        }
    }

    private String formatDate(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "Date unavailable";
        }

        String[] patterns = {
                "yyyy-MM-dd HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",
                "yyyy-MM-dd'T'HH:mm:ssX"
        };

        for (String pattern : patterns) {
            try {
                SimpleDateFormat input =
                        new SimpleDateFormat(pattern, Locale.US);
                input.setTimeZone(TimeZone.getTimeZone("UTC"));

                Date date = input.parse(raw);
                if (date != null) {
                    return new SimpleDateFormat(
                            "dd/MM/yyyy", Locale.getDefault())
                            .format(date);
                }
            } catch (Exception ignored) {
                // Try the next supported PocketBase timestamp format.
            }
        }

        return raw;
    }
}
