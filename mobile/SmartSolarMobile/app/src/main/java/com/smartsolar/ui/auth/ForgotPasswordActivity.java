package com.smartsolar.ui.auth;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartsolar.R;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;

import java.util.HashMap;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText emailInput;
    private Button submitButton;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_forgot_password);

        emailInput = findViewById(R.id.forgotEmailInput);
        submitButton = findViewById(R.id.forgotSubmitButton);

        apiService = ApiClient.create(this);

        submitButton.setOnClickListener(v -> submitRequest());
    }

    private void submitRequest() {
        String email = emailInput.getText() == null
                ? ""
                : emailInput.getText().toString().trim();

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Enter a valid email address.");
            return;
        }

        submitButton.setEnabled(false);

        Map<String, String> request = new HashMap<>();
        request.put("email", email);

        apiService.forgotPassword(request)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {

                        submitButton.setEnabled(true);

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    "If this email is registered, reset instructions will be sent.",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                        } else {
                            Toast.makeText(
                                    ForgotPasswordActivity.this,
                                    "Unable to process the request.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<ResponseBody> call,
                            Throwable throwable) {

                        submitButton.setEnabled(true);

                        Toast.makeText(
                                ForgotPasswordActivity.this,
                                "Unable to reach the server.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}