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


/**
 * Handles Android login, registration navigation,
 * password recovery navigation and role-based redirection.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText emailInput;
    private TextInputEditText passwordInput;

    private MaterialButton loginButton;
    private MaterialButton registerButton;
    private MaterialButton forgotPasswordButton;

    private ProgressBar progressBar;
    private TextView errorText;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = new SessionManager(this);

        // Existing SQLite session check.
        if (sessionManager.isLoggedIn()) {
            redirectByRole(sessionManager.getRole());
            return;
        }

        setContentView(R.layout.activity_login);


        // Existing input fields.
        emailInput = findViewById(R.id.editTextEmail);

        passwordInput = findViewById(R.id.editTextPassword);


        // Existing buttons.
        loginButton = findViewById(R.id.buttonLogin);

        registerButton = findViewById(R.id.buttonRegister);


        // NEW: Forgot Password button.
        forgotPasswordButton =
                findViewById(R.id.buttonForgotPassword);


        // Existing progress and error views.
        progressBar = findViewById(R.id.loginProgress);

        errorText = findViewById(R.id.textLoginError);


        // Existing Retrofit API client.
        apiService = ApiClient.create(this);


        // Existing login functionality.
        loginButton.setOnClickListener(
                v -> loginUser()
        );


        // Existing registration navigation.
        registerButton.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                RegisterActivity.class
                        )
                )
        );

        forgotPasswordButton.setOnClickListener(
                v -> startActivity(
                        new Intent(
                                this,
                                ForgotPasswordActivity.class
                        )
                )
        );
    }


    /**
     * Authenticates a user using the backend API.
     */
    private void loginUser() {

        hideError();

        String email = valueOf(emailInput);

        String password = valueOf(passwordInput);


        // Email required.
        if (email.isEmpty()) {

            emailInput.setError("Email is required");

            emailInput.requestFocus();

            return;
        }


        // Email validation.
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {

            emailInput.setError(
                    "Enter a valid email address"
            );

            emailInput.requestFocus();

            return;
        }


        // Password required.
        if (password.isEmpty()) {

            passwordInput.setError(
                    "Password is required"
            );

            passwordInput.requestFocus();

            return;
        }


        setLoading(true);


        // Existing backend login request.
        apiService.login(
                new LoginRequest(email, password)
        ).enqueue(new Callback<LoginResponse>() {

            @Override
            public void onResponse(
                    Call<LoginResponse> call,
                    Response<LoginResponse> response
            ) {

                setLoading(false);


                if (
                        response.isSuccessful() &&
                        response.body() != null
                ) {

                    LoginResponse loginResponse =
                            response.body();


                    // Save session in SQLite.
                    sessionManager.saveSession(
                            loginResponse
                    );


                    Toast.makeText(
                            LoginActivity.this,
                            "Welcome back, " +
                                    loginResponse.getName(),
                            Toast.LENGTH_SHORT
                    ).show();


                    // Redirect according to role.
                    redirectByRole(
                            loginResponse.getRole()
                    );

                    return;
                }


                showError(
                        ApiErrorUtil.getMessage(
                                response,
                                "Invalid credentials or the account is not active."
                        )
                );
            }


            @Override
            public void onFailure(
                    Call<LoginResponse> call,
                    Throwable throwable
            ) {

                setLoading(false);

                showError(
                        "Unable to reach the server. Check that the API is running and try again."
                );
            }
        });
    }


    /**
     * Redirects authenticated users according to role.
     */
    private void redirectByRole(String role) {

        Intent intent;


        if ("Prosumer".equals(role)) {

            intent = new Intent(
                    this,
                    ProsumerDashboardActivity.class
            );

        } else if ("GridOperator".equals(role)) {

            intent = new Intent(
                    this,
                    OperatorDashboardActivity.class
            );

        } else {

            sessionManager.logout();

            Toast.makeText(
                    this,
                    "This account is intended for the web application.",
                    Toast.LENGTH_LONG
            ).show();


            if (emailInput == null) {

                setContentView(R.layout.activity_login);

                recreate();
            }

            return;
        }


        intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }


    /**
     * Reads a text input safely.
     */
    private String valueOf(TextInputEditText input) {

        return input.getText() == null
                ? ""
                : input.getText().toString().trim();
    }


    /**
     * Updates the login loading state.
     */
    private void setLoading(boolean loading) {

        progressBar.setVisibility(
                loading ? View.VISIBLE : View.GONE
        );

        loginButton.setEnabled(!loading);

        registerButton.setEnabled(!loading);

        // NEW: Disable password recovery while logging in.
        forgotPasswordButton.setEnabled(!loading);

        loginButton.setText(
                loading ? "Signing in..." : "Sign in"
        );
    }


    /**
     * Displays an authentication error.
     */
    private void showError(String message) {

        errorText.setText(message);

        errorText.setVisibility(View.VISIBLE);
    }


    /**
     * Hides the previous authentication error.
     */
    private void hideError() {

        errorText.setVisibility(View.GONE);
    }
}
