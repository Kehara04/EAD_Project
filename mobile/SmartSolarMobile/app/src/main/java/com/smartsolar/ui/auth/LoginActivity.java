package com.smartsolar.ui.auth;

import android.content.Intent;
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
import com.smartsolar.data.local.SessionManager;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.LoginRequest;
import com.smartsolar.model.LoginResponse;
import com.smartsolar.ui.operator.OperatorDashboardActivity;
import com.smartsolar.ui.prosumer.ProsumerDashboardActivity;
import com.smartsolar.utils.ApiErrorUtil;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;
    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private ProgressBar progressBar;
    private TextView errorText;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            redirectByRole(sessionManager.getRole());
            return;
        }

        setContentView(R.layout.activity_login);

        emailInput = findViewById(R.id.editTextEmail);
        passwordInput = findViewById(R.id.editTextPassword);
        loginButton = findViewById(R.id.buttonLogin);
        registerButton = findViewById(R.id.buttonRegister);
        progressBar = findViewById(R.id.loginProgress);
        errorText = findViewById(R.id.textLoginError);

        apiService = ApiClient.create(this);

        loginButton.setOnClickListener(v -> loginUser());
        registerButton.setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class))
        );
    }

    private void loginUser() {
        hideError();

        String email = valueOf(emailInput);
        String password = valueOf(passwordInput);

        if (email.isEmpty()) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address");
            emailInput.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        setLoading(true);

        apiService.login(new LoginRequest(email, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            LoginResponse loginResponse = response.body();
                            sessionManager.saveSession(loginResponse);
                            Toast.makeText(LoginActivity.this, "Welcome back, " + loginResponse.getName(), Toast.LENGTH_SHORT).show();
                            redirectByRole(loginResponse.getRole());
                            return;
                        }

                        showError(ApiErrorUtil.getMessage(
                                response,
                                "Invalid credentials or the account is not active."
                        ));
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable throwable) {
                        setLoading(false);
                        showError("Unable to reach the server. Check that the API is running and try again.");
                    }
                });
    }

    private void redirectByRole(String role) {
        Intent intent;

        if ("Prosumer".equals(role)) {
            intent = new Intent(this, ProsumerDashboardActivity.class);
        } else if ("GridOperator".equals(role)) {
            intent = new Intent(this, OperatorDashboardActivity.class);
        } else {
            sessionManager.logout();
            Toast.makeText(this, "This account is intended for the web application.", Toast.LENGTH_LONG).show();
            if (emailInput == null) {
                setContentView(R.layout.activity_login);
                recreate();
            }
            return;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private String valueOf(TextInputEditText input) {
        return input.getText() == null ? "" : input.getText().toString().trim();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setEnabled(!loading);
        registerButton.setEnabled(!loading);
        loginButton.setText(loading ? "Signing in..." : "Sign in");
    }

    private void showError(String message) {
        errorText.setText(message);
        errorText.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        errorText.setVisibility(View.GONE);
    }
}
