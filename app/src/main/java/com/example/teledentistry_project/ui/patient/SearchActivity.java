package com.example.teledentistry_project.ui.patient;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.teledentistry_project.MainActivity;
import com.example.teledentistry_project.R;
import com.example.teledentistry_project.data.model.PocketBaseListResponse;
import com.example.teledentistry_project.data.model.UserRecords;
import com.example.teledentistry_project.data.model.practitioner.Practitioner;
import com.example.teledentistry_project.data.remote.PocketBaseClient;
import com.example.teledentistry_project.ui.adapters.DentistSearchAdapter;
import com.example.teledentistry_project.ui.userActivities.HomeActivity;
import com.example.teledentistry_project.ui.userActivities.ProfileViewActivity;
import com.example.teledentistry_project.utils.NavbarHelper;
import com.example.teledentistry_project.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchActivity extends AppCompatActivity {

    private enum SortMode { NONE, COST_ASC, COST_DESC, NAME_ASC, NAME_DESC }

    private SessionManager session;
    private DentistSearchAdapter adapter;

    // Everything loaded from PocketBase; the screen shows a filtered, sorted copy
    private final List<DentistSearchAdapter.DentistItem> allDentists = new ArrayList<>();

    // Current search / filter / sort state
    private String searchQuery = "";
    private Double minBudget = null;
    private Double maxBudget = null;
    private String locationFilter = "";
    private SortMode sortMode = SortMode.NONE;

    private EditText etSearch, etMinBudget, etMaxBudget, etLocation;
    private View cardFilterPanel, cardSortPanel;
    private ViewGroup filterSortSection;
    private MaterialButton btnFilter, btnSort;
    private RadioGroup rgSortOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        session = new SessionManager(this);
        if (!session.isLoggedIn()) {
            goToLogin();
            return;
        }
        // Patients only
        if ("practitioner".equalsIgnoreCase(session.getRole())) {
            startActivity(new Intent(this, HomeActivity.class));
            finish();
            return;
        }

        NavbarHelper.setup(this, NavbarHelper.Tab.SEARCH);

        bindViews();

        RecyclerView rvResults = findViewById(R.id.rvSearchResults);
        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new DentistSearchAdapter(item -> openDentist(item));
        rvResults.setAdapter(adapter);

        setupSearch();
        setupFilterPanel();
        setupSortPanel();

        loadDentists();
    }

    private void bindViews() {
        etSearch = findViewById(R.id.etSearch);
        etMinBudget = findViewById(R.id.etMinBudget);
        etMaxBudget = findViewById(R.id.etMaxBudget);
        etLocation = findViewById(R.id.etLocation);
        cardFilterPanel = findViewById(R.id.cardFilterPanel);
        cardSortPanel = findViewById(R.id.cardSortPanel);
        filterSortSection = findViewById(R.id.filterSortSection);
        btnFilter = findViewById(R.id.btnFilter);
        btnSort = findViewById(R.id.btnSort);
        rgSortOptions = findViewById(R.id.rgSortOptions);
    }

    // loading

    private void loadDentists() {
        // Empty filter = every practitioner
        PocketBaseClient.api
                .searchPractitioners(session.getAuthToken(), "", "user", 200)
                .enqueue(new Callback<PocketBaseListResponse<Practitioner>>() {
                    @Override
                    public void onResponse(Call<PocketBaseListResponse<Practitioner>> call,
                                           Response<PocketBaseListResponse<Practitioner>> response) {
                        if (response.code() == 401) {
                            session.clearSession();
                            goToLogin();
                            return;
                        }
                        if (!response.isSuccessful() || response.body() == null
                                || response.body().getItems() == null) {
                            Toast.makeText(SearchActivity.this,
                                    "Could not load practitioners", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        allDentists.clear();
                        for (Practitioner p : response.body().getItems()) {
                            allDentists.add(toItem(p));
                        }
                        refreshList();
                    }

                    @Override
                    public void onFailure(Call<PocketBaseListResponse<Practitioner>> call,
                                          Throwable t) {
                        Toast.makeText(SearchActivity.this,
                                "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private DentistSearchAdapter.DentistItem toItem(Practitioner p) {
        UserRecords user = p.getUser();
        String firstName = user == null ? "" : safe(user.name);
        String surname = user == null ? "" : safe(user.surname);

        String fullName = ("Dr. " + firstName + " " + surname).trim();

        return new DentistSearchAdapter.DentistItem(
                p.getUserId(),                 // the USERS id, not the practitioners record id
                fullName,
                safe(p.getPracticeName()),
                safe(p.getPracticeLocation()),
                p.getConsultationFee()
        );
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }

    // search

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) { }
            @Override public void afterTextChanged(Editable s) {
                searchQuery = s.toString().trim().toLowerCase(Locale.getDefault());
                refreshList();
            }
        });

        // Keyboard "search" key just closes the keyboard; results are already live
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodManager imm =
                        (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    // filter

    private void setupFilterPanel() {
        btnFilter.setOnClickListener(v -> togglePanel(cardFilterPanel, cardSortPanel));

        findViewById(R.id.btnApplyFilter).setOnClickListener(v -> {
            Double min = parseAmount(etMinBudget.getText().toString());
            Double max = parseAmount(etMaxBudget.getText().toString());

            if (min != null && max != null && min > max) {
                Toast.makeText(this,
                        "Minimum is higher than maximum", Toast.LENGTH_SHORT).show();
                return;
            }

            minBudget = min;
            maxBudget = max;
            locationFilter = etLocation.getText().toString()
                    .trim().toLowerCase(Locale.getDefault());

            closePanels();
            refreshList();
            updateButtonStates();
        });

        findViewById(R.id.btnRemoveFilter).setOnClickListener(v -> {
            etMinBudget.setText("");
            etMaxBudget.setText("");
            etLocation.setText("");
            minBudget = null;
            maxBudget = null;
            locationFilter = "";

            closePanels();
            refreshList();
            updateButtonStates();
        });
    }

    private Double parseAmount(String text) {
        text = text.trim();
        if (text.isEmpty()) return null;
        try {
            return Double.parseDouble(text);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // sort

    private void setupSortPanel() {
        btnSort.setOnClickListener(v -> togglePanel(cardSortPanel, cardFilterPanel));

        findViewById(R.id.btnApplySort).setOnClickListener(v -> {
            int checked = rgSortOptions.getCheckedRadioButtonId();
            if (checked == R.id.rbCostAscending) {
                sortMode = SortMode.COST_ASC;
            } else if (checked == R.id.rbCostDescending) {
                sortMode = SortMode.COST_DESC;
            } else if (checked == R.id.rbNameAscending) {
                sortMode = SortMode.NAME_ASC;
            } else if (checked == R.id.rbNameDescending) {
                sortMode = SortMode.NAME_DESC;
            } else {
                sortMode = SortMode.NONE;
            }

            closePanels();
            refreshList();
            updateButtonStates();
        });

        findViewById(R.id.btnRemoveSort).setOnClickListener(v -> {
            rgSortOptions.clearCheck();
            sortMode = SortMode.NONE;

            closePanels();
            refreshList();
            updateButtonStates();
        });
    }

    // the list the user sees

    private void refreshList() {
        List<DentistSearchAdapter.DentistItem> result = new ArrayList<>();
        for (DentistSearchAdapter.DentistItem d : allDentists) {
            if (matches(d)) {
                result.add(d);
            }
        }
        sort(result);
        adapter.setItems(result);
    }

    private boolean matches(DentistSearchAdapter.DentistItem d) {
        // Search box: matches the practitioner's name or practice name
        if (!searchQuery.isEmpty()) {
            String haystack = (d.name + " " + d.practiceName).toLowerCase(Locale.getDefault());
            if (!haystack.contains(searchQuery)) return false;
        }
        // Budget range
        if (minBudget != null && d.cost < minBudget) return false;
        if (maxBudget != null && d.cost > maxBudget) return false;
        // Location (city or town anywhere in the practice location)
        if (!locationFilter.isEmpty()
                && !d.location.toLowerCase(Locale.getDefault()).contains(locationFilter)) {
            return false;
        }
        return true;
    }

    private void sort(List<DentistSearchAdapter.DentistItem> list) {
        switch (sortMode) {
            case COST_ASC:
                Collections.sort(list, (a, b) -> Double.compare(a.cost, b.cost));
                break;
            case COST_DESC:
                Collections.sort(list, (a, b) -> Double.compare(b.cost, a.cost));
                break;
            case NAME_ASC:
                Collections.sort(list, (a, b) -> a.name.compareToIgnoreCase(b.name));
                break;
            case NAME_DESC:
                Collections.sort(list, (a, b) -> b.name.compareToIgnoreCase(a.name));
                break;
            default:
                break;   // keep the order PocketBase returned
        }
    }

    // dropdown panels / UI

    private void togglePanel(View toToggle, View other) {
        TransitionManager.beginDelayedTransition(filterSortSection);   // smooth dropdown
        other.setVisibility(View.GONE);
        toToggle.setVisibility(
                toToggle.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void closePanels() {
        TransitionManager.beginDelayedTransition(filterSortSection);
        cardFilterPanel.setVisibility(View.GONE);
        cardSortPanel.setVisibility(View.GONE);

        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(filterSortSection.getWindowToken(), 0);
    }

    // Filled buttons show when a filter or sort is active
    private void updateButtonStates() {
        boolean filterActive = minBudget != null || maxBudget != null
                || !locationFilter.isEmpty();
        styleToggleButton(btnFilter, filterActive);
        styleToggleButton(btnSort, sortMode != SortMode.NONE);
    }

    private void styleToggleButton(MaterialButton button, boolean active) {
        int background = active ? R.color.dental_primary : R.color.dental_primary_container;
        int text = active ? R.color.white : R.color.dental_primary;
        button.setBackgroundTintList(
                ColorStateList.valueOf(ContextCompat.getColor(this, background)));
        button.setTextColor(ContextCompat.getColor(this, text));
    }

    // navigation

    private void openDentist(DentistSearchAdapter.DentistItem item) {
        Intent intent = new Intent(this, ProfileViewActivity.class);
        intent.putExtra(ProfileViewActivity.EXTRA_USER_ID, item.id);
        startActivity(intent);
    }

    private void goToLogin() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}