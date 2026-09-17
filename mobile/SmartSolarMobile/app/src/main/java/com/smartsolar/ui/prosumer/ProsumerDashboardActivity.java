package com.smartsolar.ui.prosumer;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.smartsolar.R;

public class ProsumerDashboardActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_prosumer_dashboard
        );
    }
}