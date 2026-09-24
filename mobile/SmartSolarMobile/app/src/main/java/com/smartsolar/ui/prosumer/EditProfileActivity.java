package com.smartsolar.ui.prosumer;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsolar.R;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.Prosumer;
import com.smartsolar.model.UpdateProsumerRequest;
import com.smartsolar.utils.ApiErrorUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ApiService apiService;

    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText addressInput;
    private MaterialButton saveButton;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        apiService = ApiClient.create(this);

        nameInput = findViewById(R.id.editProfileName);
        emailInput = findViewById(R.id.editProfileEmail);
        phoneInput = findViewById(R.id.editProfilePhone);
        addressInput = findViewById(R.id.editProfileAddress);
        saveButton = findViewById(R.id.buttonSaveProfile);
        progressBar = findViewById(R.id.editProfileProgress);

        ((com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.buttonEditProfileBack)).setNavigationOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveProfile());

        loadCurrentProfile();
    }

    private void loadCurrentProfile() {
        setLoading(true);

        apiService.getProfile().enqueue(new Callback<Prosumer>() {
            @Override
            public void onResponse(Call<Prosumer> call, Response<Prosumer> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Prosumer p = response.body();
                    nameInput.setText(p.getName());
                    emailInput.setText(p.getEmail());
                    phoneInput.setText(p.getPhone());
                    addressInput.setText(p.getAddress());
                } else {
                    Toast.makeText(EditProfileActivity.this, "Unable to load profile.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Prosumer> call, Throwable throwable) {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, "Unable to reach the server.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void saveProfile() {
        String name = valueOf(nameInput);
        String email = valueOf(emailInput).toLowerCase();
        String phone = valueOf(phoneInput);
        String address = valueOf(addressInput);

        if (name.length() < 2) {
            nameInput.setError("Name must contain at least 2 characters");
            nameInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            phoneInput.setError("Phone number is required");
            phoneInput.requestFocus();
            return;
        }

        setLoading(true);

        apiService.updateProfile(new UpdateProsumerRequest(name, email, phone, address))
                .enqueue(new Callback<Prosumer>() {
                    @Override
                    public void onResponse(Call<Prosumer> call, Response<Prosumer> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            Toast.makeText(EditProfileActivity.this, "Profile updated successfully.", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        Toast.makeText(
                                EditProfileActivity.this,
                                ApiErrorUtil.getMessage(response, "Could not update profile."),
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onFailure(Call<Prosumer> call, Throwable throwable) {
                        setLoading(false);
                        Toast.makeText(EditProfileActivity.this, "Unable to reach the server.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private String valueOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        saveButton.setEnabled(!loading);
        saveButton.setText(loading ? "Saving..." : "Save changes");
    }
}
