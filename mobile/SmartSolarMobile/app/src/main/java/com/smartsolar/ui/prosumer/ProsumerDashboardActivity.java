// package com.smartsolar.ui.prosumer;

// import android.content.Intent;
// import android.os.Bundle;
// import android.view.View;
// import android.widget.ProgressBar;
// import android.widget.TextView;
// import android.widget.Toast;

// import androidx.appcompat.app.AppCompatActivity;

// import com.google.android.material.button.MaterialButton;
// import com.smartsolar.R;
// import com.smartsolar.data.local.SessionManager;
// import com.smartsolar.data.remote.ApiClient;
// import com.smartsolar.data.remote.ApiService;
// import com.smartsolar.model.Prosumer;
// import com.smartsolar.ui.auth.LoginActivity;
// import com.smartsolar.maps.StationMapActivity;

// import retrofit2.Call;
// import retrofit2.Callback;
// import retrofit2.Response;

// public class ProsumerDashboardActivity extends AppCompatActivity {

//     private SessionManager sessionManager;
//     private ApiService apiService;
//     private TextView welcomeText;
//     private TextView statusText;
//     private TextView emailText;
//     private ProgressBar progressBar;


//     @Override
//     protected void onCreate(Bundle savedInstanceState) {
//         super.onCreate(savedInstanceState);
//         setContentView(R.layout.activity_prosumer_dashboard);

//         sessionManager = new SessionManager(this);
//         apiService = ApiClient.create(this);

//         welcomeText = findViewById(R.id.textProsumerWelcome);
//         statusText = findViewById(R.id.textProsumerStatus);
//         emailText = findViewById(R.id.textProsumerEmail);
//         progressBar = findViewById(R.id.dashboardProgress);

//         welcomeText.setText("Hello, " + safe(sessionManager.getName()));
//         emailText.setText(safe(sessionManager.getEmail()));

//         MaterialButton profileButton = findViewById(R.id.buttonViewProfile);
//         MaterialButton logoutButton = findViewById(R.id.buttonLogout);

//         profileButton.setOnClickListener(v ->
//                 startActivity(new Intent(this, ProfileActivity.class))
//         );

//         findViewById(R.id.buttonViewStations).setOnClickListener(v -> {
//             Intent intent = new Intent(
//                     ProsumerDashboardActivity.this,
//                     StationMapActivity.class
//             );
//             startActivity(intent);
//         });

//         logoutButton.setOnClickListener(v -> logout());
//     }

//     @Override
//     protected void onResume() {
//         super.onResume();
//         loadProfileSummary();
//     }

//     private void loadProfileSummary() {
//         progressBar.setVisibility(View.VISIBLE);

//         apiService.getProfile().enqueue(new Callback<Prosumer>() {
//             @Override
//             public void onResponse(Call<Prosumer> call, Response<Prosumer> response) {
//                 progressBar.setVisibility(View.GONE);

//                 if (response.isSuccessful() && response.body() != null) {
//                     Prosumer prosumer = response.body();
//                     statusText.setText(prosumer.getStatus());
//                     return;
//                 }

//                 if (response.code() == 401 || response.code() == 403) {
//                     Toast.makeText(ProsumerDashboardActivity.this, "Your session is no longer valid. Please sign in again.", Toast.LENGTH_LONG).show();
//                     logout();
//                 }
//             }

//             @Override
//             public void onFailure(Call<Prosumer> call, Throwable throwable) {
//                 progressBar.setVisibility(View.GONE);
//                 statusText.setText("Offline");
//             }
//         });
//     }

//     private void logout() {
//         sessionManager.logout();
//         Intent intent = new Intent(this, LoginActivity.class);
//         intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//         startActivity(intent);
//         finish();
//     }

//     private String safe(String value) {
//         return value == null ? "Prosumer" : value;
//     }
// }


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

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

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


        /* =====================================
           VIEW REFERENCES
        ===================================== */

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


        /* =====================================
           SESSION INFORMATION
        ===================================== */

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


        /* =====================================
           PROFILE
        ===================================== */

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


        /* =====================================
           STATIONS
        ===================================== */

        // Open station discovery from the Prosumer dashboard without changing the login session.
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


        /* =====================================
           MEMBER 3 - MY RESERVATIONS
        ===================================== */

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


        /* =====================================
           LOGOUT
        ===================================== */

        logoutButton.setOnClickListener(
                v -> logout()
        );

        // Avoid repeating the prompt during rotation or when returning from the map.
        if (savedInstanceState == null) {
            requestLocationAfterLogin();
        }
    }


    /* =========================================
       REFRESH PROFILE WHEN RETURNING
    ========================================= */

    @Override
    protected void onResume() {

        super.onResume();

        loadProfileSummary();
    }


    /* =========================================
       PROFILE SUMMARY
    ========================================= */

    private void loadProfileSummary() {

        progressBar.setVisibility(
                View.VISIBLE
        );


        apiService
                .getProfile()
                .enqueue(
                        new Callback<Prosumer>() {

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


    /* =========================================
       LOGOUT
    ========================================= */

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


    /* =========================================
       NULL SAFE TEXT
    ========================================= */

    private String safe(
            String value) {

        return value == null
                || value.trim().isEmpty()

                ? "Prosumer"

                : value;
    }
}