package com.smartsolar.ui.prosumer;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.smartsolar.R;
import com.smartsolar.data.local.SessionManager;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.maps.StationMapActivity;
import com.smartsolar.model.Prosumer;
import com.smartsolar.ui.auth.LoginActivity;
import com.smartsolar.ui.reservation.MyReservationsActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Represents the main dashboard for authenticated prosumers.
 * Displays account information and provides navigation to profile
 * management, solar stations, and energy reservations.
 * Handles location permission requests and user sign-out.
 */
public class ProsumerDashboardActivity
        extends AppCompatActivity {

    private SessionManager sessionManager;
    private ApiService apiService;

    private TextView welcomeText;
    private TextView statusText;
    private TextView emailText;

    private ProgressBar progressBar;

    // Request foreground location after login; Android remembers the user's decision.
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (hasLocationPermission() && sessionManager.isLoggedIn()
                        && "Prosumer".equals(sessionManager.getRole())) {
                    // The map obtains the device location and centres its blue user marker.
                    startActivity(new Intent(this, StationMapActivity.class));
                } else if (!hasLocationPermission()) {
                    Toast.makeText(this, R.string.location_denied, Toast.LENGTH_LONG).show();
                }
            });

    // Checks whether fine or coarse location permission has been granted.
    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    // Requests location permission once for a logged-in prosumer when permission is not yet granted.
    private void requestLocationAfterLogin() {
        if (!sessionManager.isLoggedIn() || !"Prosumer".equals(sessionManager.getRole())
                || hasLocationPermission()) return;

        // Share prompt history with the map to avoid automatically asking twice after denial.
        var preferences = getSharedPreferences("station_location_permissions", MODE_PRIVATE);
        if (preferences.getBoolean("locationAsked", false)) return;
        preferences.edit().putBoolean("locationAsked", true).apply();
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }


    // Initializes the dashboard, session manager, API service, navigation buttons, and location permission request.
    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_prosumer_dashboard
        );


        sessionManager =
                new SessionManager(this);

        apiService =
                ApiClient.create(this);

        welcomeText =
                findViewById(
                        R.id.textProsumerWelcome
                );

        statusText =
                findViewById(
                        R.id.textProsumerStatus
                );

        emailText =
                findViewById(
                        R.id.textProsumerEmail
                );

        progressBar =
                findViewById(
                        R.id.dashboardProgress
                );


        MaterialButton profileButton =
                findViewById(
                        R.id.buttonViewProfile
                );

        MaterialButton stationsButton =
                findViewById(
                        R.id.buttonViewStations
                );

        MaterialButton reservationsButton =
                findViewById(
                        R.id.buttonMyReservations
                );

        MaterialButton logoutButton =
                findViewById(
                        R.id.buttonLogout
                );

        welcomeText.setText(
                "Hello, "
                        +
                        safe(
                                sessionManager
                                        .getName()
                        )
        );


        emailText.setText(
                safe(
                        sessionManager
                                .getEmail()
                )
        );

        profileButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    ProsumerDashboardActivity.this,
                                    ProfileActivity.class
                            );

                    startActivity(intent);
                }
        );

        stationsButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    ProsumerDashboardActivity.this,
                                    StationMapActivity.class
                            );

                    startActivity(intent);
                }
        );

        reservationsButton.setOnClickListener(
                v -> {

                    Intent intent =
                            new Intent(
                                    ProsumerDashboardActivity.this,
                                    MyReservationsActivity.class
                            );

                    startActivity(intent);
                }
        );

        logoutButton.setOnClickListener(
                v -> logout()
        );

        // Avoid repeating the prompt during rotation or when returning from the map.
        if (savedInstanceState == null) {
            requestLocationAfterLogin();
        }
    }

    // Refreshes the prosumer's account status whenever the dashboard becomes active.
    @Override
    protected void onResume() {

        super.onResume();

        loadProfileSummary();
    }

    // Retrieves the current prosumer profile and updates the account status displayed on the dashboard.
    private void loadProfileSummary() {

        progressBar.setVisibility(
                View.VISIBLE
        );


        apiService
                .getProfile()
                .enqueue(
                        new Callback<Prosumer>() {

                            // Processes the profile response and logs out the user if the session is invalid.
                            @Override
                            public void onResponse(
                                    Call<Prosumer> call,
                                    Response<Prosumer> response) {

                                progressBar.setVisibility(
                                        View.GONE
                                );


                                if (
                                        response.isSuccessful()
                                                &&
                                        response.body() != null
                                ) {

                                    Prosumer prosumer =
                                            response.body();


                                    statusText.setText(
                                            prosumer.getStatus()
                                    );


                                    return;
                                }


                                if (
                                        response.code() == 401
                                                ||
                                        response.code() == 403
                                ) {

                                    Toast.makeText(
                                            ProsumerDashboardActivity.this,

                                            "Your session is no longer valid. Please sign in again.",

                                            Toast.LENGTH_LONG
                                    ).show();


                                    logout();
                                }
                            }

                            // Handles network failures by hiding the progress indicator and displaying offline status.
                            @Override
                            public void onFailure(
                                    Call<Prosumer> call,
                                    Throwable throwable) {

                                progressBar.setVisibility(
                                        View.GONE
                                );


                                statusText.setText(
                                        "Offline"
                                );
                            }
                        }
                );
    }

    // Clears the current session and redirects the prosumer to the login screen.
    private void logout() {

        sessionManager.logout();


        Intent intent =
                new Intent(
                        this,
                        LoginActivity.class
                );


        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );


        startActivity(intent);

        finish();
    }
    
    // Returns a default display name when the provided text is null or empty.
    private String safe(
            String value) {

        return value == null
                || value.trim().isEmpty()

                ? "Prosumer"

                : value;
    }
}
