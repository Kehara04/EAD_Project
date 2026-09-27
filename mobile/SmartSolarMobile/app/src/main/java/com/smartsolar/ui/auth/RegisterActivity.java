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

/**
 * Handles prosumer account registration in the Smart Solar mobile application.
 * Validates personal information and credentials, submits registration details
 * to the backend API, and displays registration results to the user.
 */
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

    private static final String NIC_PATTERN =
            "^(?:\\d{9}[VvXx]|\\d{12})$";

    private static final String PHONE_PATTERN =
            "^0\\d{9}$";

    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,64}$";

    // Initializes the registration screen, input fields, API service, and navigation listeners.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        nicInput =
                findViewById(R.id.editTextNic);

        nameInput =
                findViewById(R.id.editTextName);

        emailInput =
                findViewById(R.id.editTextRegisterEmail);

        phoneInput =
                findViewById(R.id.editTextPhone);

        addressInput =
                findViewById(R.id.editTextAddress);

        passwordInput =
                findViewById(R.id.editTextRegisterPassword);

        registerButton =
                findViewById(R.id.buttonCreateAccount);

        progressBar =
                findViewById(R.id.registerProgress);

        errorText =
                findViewById(R.id.textRegisterError);

        apiService =
                ApiClient.create(this);

        registerButton.setOnClickListener(
                v -> registerProsumer()
        );

        ((com.google.android.material.appbar.MaterialToolbar) findViewById(
                R.id.buttonBackToLogin
        )).setNavigationOnClickListener(
                v -> finish()
        );
    }

    // Validates registration details and submits a new prosumer account request to the backend.
    private void registerProsumer() {

        if (!validateInputs()) {
            return;
        }

        errorText.setVisibility(
                View.GONE
        );

        String nic =
                valueOf(nicInput)
                        .toUpperCase();

        String name =
                valueOf(nameInput);

        String email =
                valueOf(emailInput)
                        .toLowerCase();

        String phone =
                valueOf(phoneInput);

        String address =
                valueOf(addressInput);

        String password =
                valueOf(passwordInput);

        RegisterProsumerRequest request =
                new RegisterProsumerRequest(
                        nic,
                        name,
                        email,
                        phone,
                        address,
                        password
                );

        setLoading(true);

        apiService
                .register(request)
                .enqueue(
                        new Callback<Prosumer>() {

                            // Processes the registration response and displays confirmation or validation errors.
                            @Override
                            public void onResponse(
                                    Call<Prosumer> call,
                                    Response<Prosumer> response) {

                                setLoading(false);

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Registration submitted. A Backoffice user must activate your account before you can sign in.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    finish();
                                    return;
                                }

                                showError(
                                        ApiErrorUtil.getMessage(
                                                response,
                                                "Registration failed. Please check your details."
                                        )
                                );
                            }

                            // Handles network failures encountered while submitting the registration request.
                            @Override
                            public void onFailure(
                                    Call<Prosumer> call,
                                    Throwable throwable) {

                                setLoading(false);

                                showError(
                                        "Unable to reach the server. Please try again."
                                );
                            }
                        }
                );
    }

    // Validates the NIC, name, email, phone number, address, and password before registration.
    private boolean validateInputs() {

        String nicValue =
                valueOf(nicInput);

        String nameValue =
                valueOf(nameInput);

        String emailValue =
                valueOf(emailInput);

        String phoneValue =
                valueOf(phoneInput);

        String addressValue =
                valueOf(addressInput);

        String passwordValue =
                valueOf(passwordInput);

        if (nicValue.isEmpty()) {

            nicInput.setError(
                    "NIC is required"
            );

            nicInput.requestFocus();

            return false;
        }

        if (!nicValue.matches(
                NIC_PATTERN)) {

            nicInput.setError(
                    "Use 9 digits followed by V/X or a 12-digit NIC"
            );

            nicInput.requestFocus();

            return false;
        }

        if (nameValue.isEmpty()) {

            nameInput.setError(
                    "Full name is required"
            );

            nameInput.requestFocus();

            return false;
        }

        if (nameValue.length() < 2) {

            nameInput.setError(
                    "Name must contain at least 2 characters"
            );

            nameInput.requestFocus();

            return false;
        }

        if (nameValue.length() > 100) {

            nameInput.setError(
                    "Name cannot exceed 100 characters"
            );

            nameInput.requestFocus();

            return false;
        }

        if (emailValue.isEmpty()) {

            emailInput.setError(
                    "Email address is required"
            );

            emailInput.requestFocus();

            return false;
        }

        if (!Patterns.EMAIL_ADDRESS
                .matcher(emailValue)
                .matches()) {

            emailInput.setError(
                    "Enter a valid email address"
            );

            emailInput.requestFocus();

            return false;
        }

        if (phoneValue.isEmpty()) {

            phoneInput.setError(
                    "Phone number is required"
            );

            phoneInput.requestFocus();

            return false;
        }

        if (!phoneValue.matches(
                PHONE_PATTERN)) {

            phoneInput.setError(
                    "Phone number must contain exactly 10 digits and start with 0"
            );

            phoneInput.requestFocus();

            return false;
        }

        if (addressValue.isEmpty()) {

            addressInput.setError(
                    "Address is required"
            );

            addressInput.requestFocus();

            return false;
        }

        if (addressValue.length() < 5) {

            addressInput.setError(
                    "Address must contain at least 5 characters"
            );

            addressInput.requestFocus();

            return false;
        }

        if (addressValue.length() > 250) {

            addressInput.setError(
                    "Address cannot exceed 250 characters"
            );

            addressInput.requestFocus();

            return false;
        }

        if (passwordValue.isEmpty()) {

            passwordInput.setError(
                    "Password is required"
            );

            passwordInput.requestFocus();

            return false;
        }

        if (passwordValue.length() < 8) {

            passwordInput.setError(
                    "Password must contain at least 8 characters"
            );

            passwordInput.requestFocus();

            return false;
        }

        if (passwordValue.length() > 64) {

            passwordInput.setError(
                    "Password cannot exceed 64 characters"
            );

            passwordInput.requestFocus();

            return false;
        }

        if (!passwordValue.matches(
                PASSWORD_PATTERN)) {

            passwordInput.setError(
                    "Password must contain uppercase, lowercase, number and special character with no spaces"
            );

            passwordInput.requestFocus();

            return false;
        }

        return true;
    }

    // Safely retrieves and trims the text entered in an input field.
    private String valueOf(
            TextInputEditText input) {

        return input.getText() == null
                ? ""
                : input.getText()
                        .toString()
                        .trim();
    }

    // Updates the progress indicator and registration button while the API request is running.
    private void setLoading(
            boolean loading) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        registerButton.setEnabled(
                !loading
        );

        registerButton.setText(
                loading
                        ? "Creating account..."
                        : "Create account"
        );
    }

    // Displays a registration or network error message on the screen.
    private void showError(
            String message) {

        errorText.setText(
                message
        );

        errorText.setVisibility(
                View.VISIBLE
        );
    }
}