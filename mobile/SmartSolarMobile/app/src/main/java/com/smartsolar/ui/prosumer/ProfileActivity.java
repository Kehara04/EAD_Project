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

public class ProfileActivity extends AppCompatActivity {

    private ApiService apiService;
    private SessionManager sessionManager;
    private ProgressBar progressBar;

    private TextView nicText;
    private TextView nameText;
    private TextView emailText;
    private TextView phoneText;
    private TextView addressText;
    private TextView statusText;

    private MaterialButton editButton;
    private MaterialButton deactivateButton;

    private Prosumer currentProsumer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        apiService = ApiClient.create(this);
        sessionManager = new SessionManager(this);

        progressBar = findViewById(R.id.profileProgress);
        nicText = findViewById(R.id.textProfileNic);
        nameText = findViewById(R.id.textProfileName);
        emailText = findViewById(R.id.textProfileEmail);
        phoneText = findViewById(R.id.textProfilePhone);
        addressText = findViewById(R.id.textProfileAddress);
        statusText = findViewById(R.id.textProfileStatus);
        editButton = findViewById(R.id.buttonEditProfile);
        deactivateButton = findViewById(R.id.buttonRequestDeactivation);

        findViewById(R.id.buttonProfileBack).setOnClickListener(v -> finish());
        editButton.setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class))
        );
        deactivateButton.setOnClickListener(v -> confirmDeactivation());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    private void loadProfile() {
        setLoading(true);

        apiService.getProfile().enqueue(new Callback<Prosumer>() {
            @Override
            public void onResponse(Call<Prosumer> call, Response<Prosumer> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentProsumer = response.body();
                    bindProfile(currentProsumer);
                    return;
                }

                if (response.code() == 401 || response.code() == 403) {
                    forceLogout();
                    return;
                }

                Toast.makeText(ProfileActivity.this, "Unable to load profile.", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(Call<Prosumer> call, Throwable throwable) {
                setLoading(false);
                Toast.makeText(ProfileActivity.this, "Unable to reach the server.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void bindProfile(Prosumer prosumer) {
        nicText.setText(prosumer.getNic());
        nameText.setText(prosumer.getName());
        emailText.setText(prosumer.getEmail());
        phoneText.setText(prosumer.getPhone());
        addressText.setText(prosumer.getAddress() == null || prosumer.getAddress().isEmpty() ? "Not provided" : prosumer.getAddress());
        statusText.setText(prosumer.getStatus());

        boolean active = "Active".equals(prosumer.getStatus());
        editButton.setEnabled(!"Deactivated".equals(prosumer.getStatus()));
        deactivateButton.setEnabled(active);

        if ("DeactivationRequested".equals(prosumer.getStatus())) {
            deactivateButton.setText("Deactivation requested");
        } else {
            deactivateButton.setText("Request deactivation");
        }
    }

    private void confirmDeactivation() {
        if (currentProsumer == null || !"Active".equals(currentProsumer.getStatus())) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Request account deactivation?")
                .setMessage("Your request will be sent to Backoffice for approval. Your account is not immediately deleted.")
                .setNegativeButton("Cancel", null)
                .setPositiveButton("Request", (dialog, which) -> requestDeactivation())
                .show();
    }

    private void requestDeactivation() {
        setLoading(true);

        apiService.requestDeactivation().enqueue(new Callback<ProsumerActionResponse>() {
            @Override
            public void onResponse(Call<ProsumerActionResponse> call, Response<ProsumerActionResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProfileActivity.this, response.body().getMessage(), Toast.LENGTH_LONG).show();
                    currentProsumer = response.body().getProsumer();
                    if (currentProsumer != null) {
                        bindProfile(currentProsumer);
                    } else {
                        loadProfile();
                    }
                    return;
                }

                Toast.makeText(
                        ProfileActivity.this,
                        ApiErrorUtil.getMessage(response, "Could not submit deactivation request."),
                        Toast.LENGTH_LONG
                ).show();
            }

            @Override
            public void onFailure(Call<ProsumerActionResponse> call, Throwable throwable) {
                setLoading(false);
                Toast.makeText(ProfileActivity.this, "Unable to reach the server.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        editButton.setEnabled(!loading && (currentProsumer == null || !"Deactivated".equals(currentProsumer.getStatus())));
        deactivateButton.setEnabled(!loading && currentProsumer != null && "Active".equals(currentProsumer.getStatus()));
    }

    private void forceLogout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
