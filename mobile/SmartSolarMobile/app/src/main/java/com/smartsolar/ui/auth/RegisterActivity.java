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

    /*
     * Supports:
     * Old NIC: 9 digits + V/X
     * New NIC: 12 digits
     */
    private static final String NIC_PATTERN =
            "^(?:\\d{9}[VvXx]|\\d{12})$";

    /*
     * Sri Lankan local mobile/telephone format:
     * exactly 10 digits starting with 0.
     */
    private static final String PHONE_PATTERN =
            "^0\\d{9}$";

    /*
     * Password requires:
     * - 8 to 64 characters
     * - uppercase
     * - lowercase
     * - number
     * - special character
     * - no spaces
     */
    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,64}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Connect Java fields with layout controls.
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

        // Create API service.
        apiService =
                ApiClient.create(this);

        // Register account when button is pressed.
        registerButton.setOnClickListener(
                v -> registerProsumer()
        );

        // Return to login screen.
        ((com.google.android.material.appbar.MaterialToolbar) findViewById(
                R.id.buttonBackToLogin
        )).setNavigationOnClickListener(
                v -> finish()
        );
    }

    private void registerProsumer() {

        // Validate all user-entered information first.
        if (!validateInputs()) {
            return;
        }

        errorText.setVisibility(
                View.GONE
        );

        // Normalize values before sending them to the API.
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

        // Send registration request to central Web API.
        apiService
                .register(request)
                .enqueue(
                        new Callback<Prosumer>() {

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

    private boolean validateInputs() {

        // Read and normalize all input values.
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

        /*
         * NIC validation.
         */
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

        /*
         * Full name validation.
         */
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

        /*
         * Email validation.
         */
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

        /*
         * Phone validation.
         */
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

        /*
         * Address validation.
         */
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

        /*
         * Password validation.
         */
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

    private String valueOf(
            TextInputEditText input) {

        // Safely read EditText value.
        return input.getText() == null
                ? ""
                : input.getText()
                        .toString()
                        .trim();
    }

    private void setLoading(
            boolean loading) {

        // Update UI while network request is running.
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

    private void showError(
            String message) {

        // Display API/network error to user.
        errorText.setText(
                message
        );

        errorText.setVisibility(
                View.VISIBLE
        );
    }
}