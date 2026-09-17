package com.smartsolar.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartsolar.R;
import com.smartsolar.data.local.SessionManager;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.LoginRequest;
import com.smartsolar.model.LoginResponse;
import com.smartsolar.ui.operator.OperatorDashboardActivity;
import com.smartsolar.ui.prosumer.ProsumerDashboardActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextEmail;
    private EditText editTextPassword;

    private Button buttonLogin;
    private Button buttonRegister;

    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextEmail =
                findViewById(R.id.editTextEmail);

        editTextPassword =
                findViewById(R.id.editTextPassword);

        buttonLogin =
                findViewById(R.id.buttonLogin);

        buttonRegister =
                findViewById(R.id.buttonRegister);

        apiService =
                ApiClient.create(this);

        sessionManager =
                new SessionManager(this);

        buttonLogin.setOnClickListener(v ->
                loginUser()
        );

        buttonRegister.setOnClickListener(v -> {
            Intent intent =
                    new Intent(
                            LoginActivity.this,
                            RegisterActivity.class
                    );

            startActivity(intent);
        });
    }

    private void loginUser() {

        String email =
                editTextEmail
                        .getText()
                        .toString()
                        .trim();

        String password =
                editTextPassword
                        .getText()
                        .toString();

        if (email.isEmpty() ||
                password.isEmpty()) {

            Toast.makeText(
                    this,
                    "Enter email and password",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        LoginRequest request =
                new LoginRequest(
                        email,
                        password
                );

        apiService.login(request)
                .enqueue(
                        new Callback<LoginResponse>() {

                            @Override
                            public void onResponse(
                                    Call<LoginResponse> call,
                                    Response<LoginResponse> response) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    LoginResponse user =
                                            response.body();

                                    sessionManager
                                            .saveSession(user);

                                    Toast.makeText(
                                            LoginActivity.this,
                                            "Login successful",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    redirectByRole(
                                            user.getRole()
                                    );

                                } else {

                                    Toast.makeText(
                                            LoginActivity.this,
                                            "Invalid login or inactive account",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<LoginResponse> call,
                                    Throwable throwable) {

                                Toast.makeText(
                                        LoginActivity.this,
                                        "Server error: "
                                                + throwable.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

    private void redirectByRole(String role) {

        Intent intent;

        if ("Prosumer".equals(role)) {

            intent =
                    new Intent(
                            this,
                            ProsumerDashboardActivity.class
                    );

        } else if ("GridOperator".equals(role)) {

            intent =
                    new Intent(
                            this,
                            OperatorDashboardActivity.class
                    );

        } else {

            Toast.makeText(
                    this,
                    "This account is not supported on mobile.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        startActivity(intent);

        finish();
    }
}