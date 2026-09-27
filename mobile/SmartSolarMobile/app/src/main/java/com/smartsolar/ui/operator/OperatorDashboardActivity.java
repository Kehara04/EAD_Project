package com.smartsolar.ui.operator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smartsolar.R;
import com.smartsolar.data.local.SessionManager;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.OperatorDashboardStats;
import com.smartsolar.ui.auth.LoginActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Landing screen for Grid Operators.
 * Displays today's reservation counts and per-station completion
 * totals fetched from GET /api/operator/dashboard.
 * The "Scan QR Code" button navigates to QrScanActivity.
 */
public class OperatorDashboardActivity extends AppCompatActivity {

    private ApiService    apiService;
    private SessionManager sessionManager;

    private ProgressBar   progressDashboard;
    private LinearLayout  layoutStats;

    private TextView textTodayTotal;
    private TextView textPendingCount;
    private TextView textApprovedCount;
    private TextView textCompletedCount;
    private TextView textCancelledCount;

    private LinearLayout layoutStationSummaries;

    // Initializes the operator dashboard, UI components, API service, and button listeners.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_dashboard);

        apiService     = ApiClient.create(this);
        sessionManager = new SessionManager(this);

        progressDashboard    = findViewById(R.id.progressDashboard);
        layoutStats          = findViewById(R.id.layoutStats);
        textTodayTotal       = findViewById(R.id.textStatTodayTotal);
        textPendingCount     = findViewById(R.id.textStatPending);
        textApprovedCount    = findViewById(R.id.textStatApproved);
        textCompletedCount   = findViewById(R.id.textStatCompleted);
        textCancelledCount   = findViewById(R.id.textStatCancelled);
        layoutStationSummaries = findViewById(R.id.layoutStationSummaries);

        MaterialButton scanButton =
                findViewById(R.id.buttonScanQr);

        scanButton.setOnClickListener(v ->
                startActivity(
                        new Intent(this, QrScanActivity.class)
                )
        );

        MaterialButton refreshButton =
                findViewById(R.id.buttonRefreshDashboard);
        refreshButton.setOnClickListener(v -> loadDashboard());

        MaterialButton signOutButton =
                findViewById(R.id.buttonSignOut);
        signOutButton.setOnClickListener(v -> logout());

        loadDashboard();
    }

    // Refreshes dashboard statistics whenever the operator returns to this screen.
    @Override
    protected void onResume() {
        super.onResume();
        loadDashboard();
    }


    // Retrieves the latest operator dashboard statistics from the backend API.
    private void loadDashboard() {

        setLoading(true);

        apiService.getOperatorDashboard()
                .enqueue(new Callback<OperatorDashboardStats>() {

                    // Processes the API response and updates the dashboard when statistics are available.
                    @Override
                    public void onResponse(
                            Call<OperatorDashboardStats> call,
                            Response<OperatorDashboardStats> response) {

                        setLoading(false);

                        if (response.isSuccessful() &&
                                response.body() != null) {

                            bindStats(response.body());
                            return;
                        }

                        Toast.makeText(
                                OperatorDashboardActivity.this,
                                "Could not load dashboard stats.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    // Displays an error when the dashboard request fails due to a connection issue.
                    @Override
                    public void onFailure(
                            Call<OperatorDashboardStats> call,
                            Throwable throwable) {

                        setLoading(false);
                        Toast.makeText(
                                OperatorDashboardActivity.this,
                                "Unable to reach the server.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // Displays reservation counts and generates completion summary rows for each station.
    private void bindStats(OperatorDashboardStats stats) {

        textTodayTotal.setText(String.valueOf(stats.getTodayTotal()));
        textPendingCount.setText(String.valueOf(stats.getPendingCount()));
        textApprovedCount.setText(String.valueOf(stats.getApprovedCount()));
        textCompletedCount.setText(String.valueOf(stats.getCompletedCount()));
        textCancelledCount.setText(String.valueOf(stats.getCancelledCount()));

        layoutStationSummaries.removeAllViews();

        if (stats.getStationSummaries() == null ||
                stats.getStationSummaries().isEmpty()) {

            TextView empty = new TextView(this);
            empty.setText("No completed transfers at any station today.");
            empty.setTextColor(0xFFA0B4C8);
            empty.setTextSize(13f);
            layoutStationSummaries.addView(empty);
            return;
        }

        for (OperatorDashboardStats.StationSummary s :
                stats.getStationSummaries()) {

            View row = getLayoutInflater().inflate(
                    R.layout.item_station_summary,
                    layoutStationSummaries,
                    false
            );

            ((TextView) row.findViewById(R.id.textSummaryStationName))
                    .setText(s.getStationName());

            ((TextView) row.findViewById(R.id.textSummaryCompleted))
                    .setText(s.getCompletedToday() +
                            " completed today");

            layoutStationSummaries.addView(row);
        }

        layoutStats.setVisibility(View.VISIBLE);
    }


    // Controls the loading indicator and visibility of dashboard statistics.
    private void setLoading(boolean loading) {
        progressDashboard.setVisibility(loading ? View.VISIBLE : View.GONE);
        layoutStats.setVisibility(loading ? View.GONE : View.VISIBLE);
    }

    // Clears the current session and redirects the operator to the login screen.
    private void logout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                Intent.FLAG_ACTIVITY_CLEAR_TASK
        );
        startActivity(intent);
        finish();
    }
}
