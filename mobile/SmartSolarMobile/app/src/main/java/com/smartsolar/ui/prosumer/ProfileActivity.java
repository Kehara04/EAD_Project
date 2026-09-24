package com.smartsolar.ui.prosumer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smartsolar.R;
import com.smartsolar.data.local.SessionManager;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.Prosumer;
import com.smartsolar.model.ProsumerActionResponse;
import com.smartsolar.ui.auth.LoginActivity;
import com.smartsolar.utils.ApiErrorUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ProfileActivity
 *
 * Displays Prosumer account information and provides:
 * - View profile
 * - Edit profile
 * - Change password
 * - Request account deactivation
 *
 * Profile information is retrieved from the central API.
 */
public class ProfileActivity extends AppCompatActivity {

    private ApiService apiService;
    private SessionManager sessionManager;

    private ProgressBar progressBar;

    // Profile information
    private TextView nicText;
    private TextView nameText;
    private TextView emailText;
    private TextView phoneText;
    private TextView addressText;
    private TextView statusText;

    private MaterialButton editButton;
    private MaterialButton deactivateButton;
    private MaterialButton changePasswordButton;

    private Prosumer currentProsumer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        // Initialize API and local session management
        apiService = ApiClient.create(this);
        sessionManager = new SessionManager(this);

        // ==========================================
        // PROFILE INFORMATION
        // ==========================================

        progressBar = findViewById(R.id.profileProgress);

        nicText = findViewById(R.id.textProfileNic);
        nameText = findViewById(R.id.textProfileName);
        emailText = findViewById(R.id.textProfileEmail);
        phoneText = findViewById(R.id.textProfilePhone);
        addressText = findViewById(R.id.textProfileAddress);
        statusText = findViewById(R.id.textProfileStatus);

        editButton = findViewById(R.id.buttonEditProfile);

        deactivateButton = findViewById(
                R.id.buttonRequestDeactivation
        );

        changePasswordButton = findViewById(
                R.id.buttonChangePassword
        );

        ((com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.buttonProfileBack))
                .setNavigationOnClickListener(v -> finish());


        editButton.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                ProfileActivity.this,
                                EditProfileActivity.class
                        )
                )
        );

        changePasswordButton.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                ProfileActivity.this,
                                ChangePasswordActivity.class
                        )
                )
        );

        deactivateButton.setOnClickListener(
                v -> confirmDeactivation()
        );
    }



     //Reload the profile whenever the user returns from Edit Profile or Change Password.
    @Override
    protected void onResume() {
        super.onResume();

        loadProfile();
    }

     //Retrieve the authenticated Prosumer profile from the backend API.
    private void loadProfile() {

        setLoading(true);

        apiService.getProfile().enqueue(
                new Callback<Prosumer>() {

                    @Override
                    public void onResponse(
                            Call<Prosumer> call,
                            Response<Prosumer> response
                    ) {

                        setLoading(false);

                        if (
                                response.isSuccessful() &&
                                response.body() != null
                        ) {

                            currentProsumer = response.body();

                            bindProfile(currentProsumer);

                            return;
                        }

                        // Invalid or unauthorized session
                        if (
                                response.code() == 401 ||
                                response.code() == 403
                        ) {

                            forceLogout();

                            return;
                        }

                        Toast.makeText(
                                ProfileActivity.this,
                                ApiErrorUtil.getMessage(
                                        response,
                                        "Unable to load profile."
                                ),
                                Toast.LENGTH_LONG
                        ).show();
                    }


                    @Override
                    public void onFailure(
                            Call<Prosumer> call,
                            Throwable throwable
                    ) {

                        setLoading(false);

                        Toast.makeText(
                                ProfileActivity.this,
                                "Unable to reach the server.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

     //Display the retrieved Prosumer information.
    private void bindProfile(Prosumer prosumer) {

        nicText.setText(
                prosumer.getNic()
        );

        nameText.setText(
                prosumer.getName()
        );

        emailText.setText(
                prosumer.getEmail()
        );

        phoneText.setText(
                prosumer.getPhone()
        );

        addressText.setText(
                prosumer.getAddress() == null ||
                        prosumer.getAddress().isEmpty()
                        ? "Not provided"
                        : prosumer.getAddress()
        );

        statusText.setText(
                prosumer.getStatus()
        );

        boolean active =
                "Active".equals(prosumer.getStatus());

        // Existing profile editing behavior
        editButton.setEnabled(
                !"Deactivated".equals(prosumer.getStatus())
        );

        // Change Password requires an active account
        changePasswordButton.setEnabled(active);

        // Only active accounts can request deactivation
        deactivateButton.setEnabled(active);

        if (
                "DeactivationRequested".equals(
                        prosumer.getStatus()
                )
        ) {

            deactivateButton.setText(
                    "Deactivation requested"
            );

        } else {

            deactivateButton.setText(
                    "Request deactivation"
            );
        }
    }


    
     //Show confirmation before requesting account deactivation.
    private void confirmDeactivation() {

        if (
                currentProsumer == null ||
                !"Active".equals(
                        currentProsumer.getStatus()
                )
        ) {
            return;
        }

        new AlertDialog.Builder(this)

                .setTitle(
                        "Request account deactivation?"
                )

                .setMessage(
                        "Your request will be sent to Backoffice for approval. " +
                                "Your account is not immediately deleted."
                )

                .setNegativeButton(
                        "Cancel",
                        null
                )

                .setPositiveButton(
                        "Request",
                        (dialog, which) ->
                                requestDeactivation()
                )

                .show();
    }


    //Send the deactivation request to the API.
    private void requestDeactivation() {

        setLoading(true);

        apiService.requestDeactivation().enqueue(
                new Callback<ProsumerActionResponse>() {

                    @Override
                    public void onResponse(
                            Call<ProsumerActionResponse> call,
                            Response<ProsumerActionResponse> response
                    ) {

                        setLoading(false);

                        if (
                                response.isSuccessful() &&
                                response.body() != null
                        ) {

                            Toast.makeText(
                                    ProfileActivity.this,
                                    response.body().getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                            currentProsumer =
                                    response.body().getProsumer();

                            if (currentProsumer != null) {

                                bindProfile(currentProsumer);

                            } else {

                                loadProfile();
                            }

                            return;
                        }

                        Toast.makeText(
                                ProfileActivity.this,
                                ApiErrorUtil.getMessage(
                                        response,
                                        "Could not submit deactivation request."
                                ),
                                Toast.LENGTH_LONG
                        ).show();
                    }


                    @Override
                    public void onFailure(
                            Call<ProsumerActionResponse> call,
                            Throwable throwable
                    ) {

                        setLoading(false);

                        Toast.makeText(
                                ProfileActivity.this,
                                "Unable to reach the server.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

     //Enable or disable profile actions while an API request is running.
    private void setLoading(boolean loading) {

        progressBar.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        boolean canEdit =
                currentProsumer != null &&
                !"Deactivated".equals(
                        currentProsumer.getStatus()
                );

        boolean active =
                currentProsumer != null &&
                "Active".equals(
                        currentProsumer.getStatus()
                );

        editButton.setEnabled(
                !loading && canEdit
        );

        changePasswordButton.setEnabled(
                !loading && active
        );

        deactivateButton.setEnabled(
                !loading && active
        );
    }

     //Clear the local SQLite session and redirect the user to LoginActivity.
    private void forceLogout() {

        sessionManager.logout();

        Intent intent = new Intent(
                ProfileActivity.this,
                LoginActivity.class
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }
}
