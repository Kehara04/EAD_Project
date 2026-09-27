package com.smartsolar.ui.operator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanIntentResult;
import com.journeyapps.barcodescanner.ScanOptions;
import com.smartsolar.R;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.QrPayload;
import com.smartsolar.model.VerifyQrResponse;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.widget.ProgressBar;

import androidx.activity.result.ActivityResultLauncher;

/**
 * Operator screen that launches the ZXing camera scanner, parses
 * the scanned QR JSON, sends it to the verification API, and then
 * opens ScanResultActivity with the server's response.
 */
public class QrScanActivity extends AppCompatActivity {

    private ApiService apiService;
    private ProgressBar progressBar;
    private MaterialButton scanButton;


    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(
                    new ScanContract(),
                    this::handleScanResult
            );

    // Initializes the QR scanning screen, API service, and navigation controls.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scan);

        apiService  = ApiClient.create(this);
        progressBar = findViewById(R.id.progressQrScan);
        scanButton  = findViewById(R.id.buttonStartScan);

        // Back button.
        ((com.google.android.material.appbar.MaterialToolbar) findViewById(R.id.buttonQrScanBack)).setNavigationOnClickListener(v -> finish());

        // Scan QR button.
        scanButton.setOnClickListener(v -> launchScanner());
    }


    // Launches the ZXing camera scanner configured to capture QR codes.
    private void launchScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Point the camera at the Prosumer's QR code");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }


    // Processes the scanned QR code and submits a valid payload for server verification.
    private void handleScanResult(ScanIntentResult result) {

        if (result.getContents() == null) {
            return;
        }

        QrPayload payload = parseQrJson(result.getContents());

        if (payload == null) {
            Toast.makeText(this,
                    "Invalid QR code format.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        verifyPayload(payload);
    }


    // Sends the scanned QR payload to the backend verification API.
    private void verifyPayload(QrPayload payload) {

        setLoading(true);

        apiService.verifyQr(payload)
                .enqueue(new Callback<VerifyQrResponse>() {

                    // Processes the verification response and opens the scan result screen on success.
                    @Override
                    public void onResponse(
                            Call<VerifyQrResponse> call,
                            Response<VerifyQrResponse> response) {

                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            ScanResultActivity.open(
                                    QrScanActivity.this,
                                    response.body()
                            );
                            return;
                        }

                        Toast.makeText(QrScanActivity.this,
                                "Verification failed. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }

                    // Handles network failures encountered during QR code verification.
                    @Override
                    public void onFailure(
                            Call<VerifyQrResponse> call,
                            Throwable throwable) {

                        setLoading(false);
                        Toast.makeText(QrScanActivity.this,
                                "Unable to reach the server.",
                                Toast.LENGTH_LONG).show();
                    }
                });
    }


    // Parses the scanned JSON string into a QR payload and returns null if parsing fails.
    private QrPayload parseQrJson(String json) {
        try {
            JSONObject obj = new JSONObject(json);

            // Use Gson to let Retrofit's converter handle deserialization properly.
            // Manual fallback: construct via package-private fields via Gson.
            com.google.gson.Gson gson = new com.google.gson.Gson();
            return gson.fromJson(json, QrPayload.class);

        } catch (Exception e) {
            return null;
        }
    }


    // Updates the loading indicator and scan button while verification is in progress.
    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        scanButton.setEnabled(!loading);
    }
}
