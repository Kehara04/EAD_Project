package com.smartsolar.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsolar.R;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.Prosumer;
import com.smartsolar.model.RegisterProsumerRequest;
import com.smartsolar.utils.ApiErrorUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText nicInput;
    private TextInputEditText nameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;
    private TextInputEditText addressInput;
    private TextInputEditText passwordInput;
    private MaterialButton registerButton;
    private ProgressBar progressBar;
    private TextView errorText;

    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nicInput = findViewById(R.id.editTextNic);
        nameInput = findViewById(R.id.editTextName);
        emailInput = findViewById(R.id.editTextRegisterEmail);
        phoneInput = findViewById(R.id.editTextPhone);
        addressInput = findViewById(R.id.editTextAddress);
        passwordInput = findViewById(R.id.editTextRegisterPassword);
        registerButton = findViewById(R.id.buttonCreateAccount);
        progressBar = findViewById(R.id.registerProgress);
        errorText = findViewById(R.id.textRegisterError);

        apiService = ApiClient.create(this);

        registerButton.setOnClickListener(v -> registerProsumer());
        findViewById(R.id.buttonBackToLogin).setOnClickListener(v -> finish());
    }

    private void registerProsumer() {
        errorText.setVisibility(View.GONE);

        String nic = valueOf(nicInput).toUpperCase();
        String name = valueOf(nameInput);
        String email = valueOf(emailInput).toLowerCase();
        String phone = valueOf(phoneInput);
        String address = valueOf(addressInput);
        String password = valueOf(passwordInput);

        if (nic.length() < 5) {
            nicInput.setError("Enter a valid NIC");
            nicInput.requestFocus();
            return;
        }

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

        if (password.length() < 6) {
            passwordInput.setError("Password must contain at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        setLoading(true);

        RegisterProsumerRequest request = new RegisterProsumerRequest(
                nic,
                name,
                email,
                phone,
                address,
                password
        );

        apiService.register(request).enqueue(new Callback<Prosumer>() {
            @Override
            public void onResponse(Call<Prosumer> call, Response<Prosumer> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(
                            RegisterActivity.this,
                            "Registration submitted. A Backoffice user must activate your account before you can sign in.",
                            Toast.LENGTH_LONG
                    ).show();
                    finish();
                    return;
                }

                showError(ApiErrorUtil.getMessage(response, "Registration failed. Please check your details."));
            }

            @Override
            public void onFailure(Call<Prosumer> call, Throwable throwable) {
                setLoading(false);
                showError("Unable to reach the server. Please try again.");
            }
        });
    }

    private String valueOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        registerButton.setEnabled(!loading);
        registerButton.setText(loading ? "Creating account..." : "Create account");
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }
}
