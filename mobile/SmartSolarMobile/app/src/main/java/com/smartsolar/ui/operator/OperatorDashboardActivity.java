package com.smartsolar.ui.operator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smartsolar.R;
import com.smartsolar.data.local.SessionManager;
import com.smartsolar.ui.auth.LoginActivity;

/**
 * Basic role landing screen. QR and operational features belong to the operator module.
 */
public class OperatorDashboardActivity extends AppCompatActivity {

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_dashboard);

        sessionManager = new SessionManager(this);

        TextView welcomeText = findViewById(R.id.textOperatorWelcome);
        welcomeText.setText("Hello, " + (sessionManager.getName() == null ? "Operator" : sessionManager.getName()));

        MaterialButton logoutButton = findViewById(R.id.buttonOperatorLogout);
        logoutButton.setOnClickListener(v -> logout());
    }

    private void logout() {
        sessionManager.logout();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
