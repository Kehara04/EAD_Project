package com.smartsolar.ui.prosumer;

import android.os.Bundle;
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

public class ChangePasswordActivity extends AppCompatActivity {

    private EditText currentInput;
    private EditText newInput;
    private EditText confirmInput;

    private Button submitButton;
    private ApiService apiService;

    private static final String PASSWORD_PATTERN =
            "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9])\\S{8,64}$";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_change_password);

        currentInput = findViewById(R.id.currentPasswordInput);
        newInput = findViewById(R.id.newPasswordInput);
        confirmInput = findViewById(R.id.confirmPasswordInput);

        submitButton = findViewById(R.id.changePasswordButton);

        apiService = ApiClient.create(this);

        submitButton.setOnClickListener(v -> submitChange());
    }

    private String valueOf(EditText input) {
        return input.getText() == null
                ? ""
                : input.getText().toString();
    }

    private void submitChange() {
        String current = valueOf(currentInput);
        String next = valueOf(newInput);
        String confirm = valueOf(confirmInput);

        if (current.isEmpty()) {
            currentInput.setError("Current password is required.");
            return;
        }

        if (!next.matches(PASSWORD_PATTERN)) {
            newInput.setError(
                    "Use 8+ characters with uppercase, lowercase, number and special character."
            );
            return;
        }

        if (!next.equals(confirm)) {
            confirmInput.setError("Passwords do not match.");
            return;
        }

        submitButton.setEnabled(false);

        Map<String, String> request = new HashMap<>();

        request.put("currentPassword", current);
        request.put("newPassword", next);
        request.put("confirmPassword", confirm);

        apiService.changePassword(request)
                .enqueue(new Callback<ResponseBody>() {

                    @Override
                    public void onResponse(
                            Call<ResponseBody> call,
                            Response<ResponseBody> response) {

                        submitButton.setEnabled(true);

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    ChangePasswordActivity.this,
                                    "Password changed successfully.",
                                    Toast.LENGTH_LONG
                            ).show();

                            finish();
                        } else {
                            Toast.makeText(
                                    ChangePasswordActivity.this,
                                    "Unable to change password. Check your current password.",
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
                                ChangePasswordActivity.this,
                                "Unable to reach the server.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }
}