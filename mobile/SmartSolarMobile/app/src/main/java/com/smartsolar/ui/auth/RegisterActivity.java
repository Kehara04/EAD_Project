package com.smartsolar.ui.auth;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.smartsolar.R;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.Prosumer;
import com.smartsolar.model.RegisterProsumerRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity
        extends AppCompatActivity {

    private EditText nic;
    private EditText name;
    private EditText email;
    private EditText phone;
    private EditText address;
    private EditText password;

    private ApiService apiService;

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_register
        );

        nic =
                findViewById(R.id.editTextNic);

        name =
                findViewById(R.id.editTextName);

        email =
                findViewById(
                        R.id.editTextRegisterEmail
                );

        phone =
                findViewById(R.id.editTextPhone);

        address =
                findViewById(R.id.editTextAddress);

        password =
                findViewById(
                        R.id.editTextRegisterPassword
                );

        Button registerButton =
                findViewById(
                        R.id.buttonCreateAccount
                );

        apiService =
                ApiClient.create(this);

        registerButton.setOnClickListener(v ->
                registerProsumer()
        );
    }

    private void registerProsumer() {

        RegisterProsumerRequest request =
                new RegisterProsumerRequest(
                        nic.getText()
                                .toString()
                                .trim(),

                        name.getText()
                                .toString()
                                .trim(),

                        email.getText()
                                .toString()
                                .trim(),

                        phone.getText()
                                .toString()
                                .trim(),

                        address.getText()
                                .toString()
                                .trim(),

                        password.getText()
                                .toString()
                );

        apiService.register(request)
                .enqueue(
                        new Callback<Prosumer>() {

                            @Override
                            public void onResponse(
                                    Call<Prosumer> call,
                                    Response<Prosumer> response) {

                                if (response.isSuccessful()) {

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Registration successful. Waiting for activation.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    finish();

                                } else {

                                    Toast.makeText(
                                            RegisterActivity.this,
                                            "Registration failed.",
                                            Toast.LENGTH_LONG
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Prosumer> call,
                                    Throwable throwable) {

                                Toast.makeText(
                                        RegisterActivity.this,
                                        "Server error: "
                                                + throwable.getMessage(),
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }
}