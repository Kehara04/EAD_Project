package com.smartsolar.ui.operator;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.smartsolar.R;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.CompleteTransferRequest;
import com.smartsolar.model.EnergyReservation;
import com.smartsolar.model.VerifyQrResponse;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displayed after the operator scans and verifies a Prosumer QR code.
 * Shows verification status, prosumer/station details, and the
 * "Confirm Transfer" button to finalise the energy exchange.
 */
public class ScanResultActivity extends AppCompatActivity {

    // Keys used to pass the VerifyQrResponse fields via Intent extras.
    private static final String EXTRA_IS_VALID        = "isValid";
    private static final String EXTRA_RESERVATION_ID  = "reservationId";
    private static final String EXTRA_PROSUMER_ID     = "prosumerId";
    private static final String EXTRA_PROSUMER_NAME   = "prosumerName";
    private static final String EXTRA_STATION_NAME    = "stationName";
    private static final String EXTRA_SLOT_NUMBER     = "slotNumber";
    private static final String EXTRA_SCHEDULED_AT    = "scheduledAt";
    private static final String EXTRA_STATUS          = "status";
    private static final String EXTRA_MESSAGE         = "message";

    private ApiService apiService;
    private ProgressBar progressBar;
    private MaterialButton confirmButton;
    private MaterialButton backButton;

    // Cached from intent for the confirm call.
    private String reservationId;
    private String prosumerId;


    /**
     * Convenience launcher – serialises VerifyQrResponse into Intent extras.
     */
    public static void open(Context context, VerifyQrResponse response) {
        Intent intent = new Intent(context, ScanResultActivity.class);
        intent.putExtra(EXTRA_IS_VALID,       response.isValid());
        intent.putExtra(EXTRA_RESERVATION_ID, response.getReservationId());
        intent.putExtra(EXTRA_PROSUMER_ID,    response.getProsumerId());
        intent.putExtra(EXTRA_PROSUMER_NAME,  response.getProsumerName());
        intent.putExtra(EXTRA_STATION_NAME,   response.getStationName());
        intent.putExtra(EXTRA_SLOT_NUMBER,    response.getSlotNumber());
        intent.putExtra(EXTRA_SCHEDULED_AT,   response.getScheduledAt());
        intent.putExtra(EXTRA_STATUS,         response.getStatus());
        intent.putExtra(EXTRA_MESSAGE,        response.getMessage());
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);

        apiService = ApiClient.create(this);

        // Read extras.
        boolean isValid     = getIntent().getBooleanExtra(EXTRA_IS_VALID, false);
        reservationId       = getIntent().getStringExtra(EXTRA_RESERVATION_ID);
        prosumerId          = getIntent().getStringExtra(EXTRA_PROSUMER_ID);
        String prosumerName = getIntent().getStringExtra(EXTRA_PROSUMER_NAME);
        String stationName  = getIntent().getStringExtra(EXTRA_STATION_NAME);
        int    slotNumber   = getIntent().getIntExtra(EXTRA_SLOT_NUMBER, 0);
        String scheduledAt  = getIntent().getStringExtra(EXTRA_SCHEDULED_AT);
        String status       = getIntent().getStringExtra(EXTRA_STATUS);
        String message      = getIntent().getStringExtra(EXTRA_MESSAGE);

        // Bind views.
        TextView textResultBadge   = findViewById(R.id.textScanResultBadge);
        TextView textMessage       = findViewById(R.id.textScanResultMessage);
        TextView textProsumerName  = findViewById(R.id.textResultProsumerName);
        TextView textProsumerId    = findViewById(R.id.textResultProsumerId);
        TextView textStationName   = findViewById(R.id.textResultStationName);
        TextView textSlot          = findViewById(R.id.textResultSlot);
        TextView textScheduledAt   = findViewById(R.id.textResultScheduledAt);
        CardView detailCard        = findViewById(R.id.cardScanDetails);

        progressBar   = findViewById(R.id.progressScanResult);
        confirmButton = findViewById(R.id.buttonConfirmTransfer);
        backButton    = findViewById(R.id.buttonScanResultBack);

        // Back button always available.
        backButton.setOnClickListener(v -> finish());

        if (isValid) {
            textResultBadge.setText("✓ QR Verified");
            textResultBadge.setBackgroundResource(
                    R.drawable.bg_status_approved
            );
            textMessage.setText(
                    message != null ? message
                            : "QR code verified. Confirm the energy transfer below."
            );

            // Populate detail fields.
            textProsumerName.setText(
                    prosumerName != null ? prosumerName : prosumerId
            );
            textProsumerId.setText(prosumerId);
            textStationName.setText(
                    stationName != null ? stationName : "—"
            );
            textSlot.setText("Slot " + slotNumber);
            textScheduledAt.setText(formatDateTime(scheduledAt));

            detailCard.setVisibility(View.VISIBLE);
            confirmButton.setVisibility(View.VISIBLE);

            confirmButton.setOnClickListener(v -> completeTransfer());

        } else {
            textResultBadge.setText("✗ Verification Failed");
            textResultBadge.setBackgroundResource(
                    R.drawable.bg_status_cancelled
            );
            textMessage.setText(
                    message != null ? message
                            : "The QR code could not be verified."
            );
            detailCard.setVisibility(View.GONE);
            confirmButton.setVisibility(View.GONE);
        }
    }


    /**
     * Calls POST /api/operator/complete to mark the reservation as Completed.
     */
    private void completeTransfer() {

        setLoading(true);

        CompleteTransferRequest body =
                new CompleteTransferRequest(reservationId, prosumerId);

        apiService.completeTransfer(body)
                .enqueue(new Callback<EnergyReservation>() {

                    @Override
                    public void onResponse(
                            Call<EnergyReservation> call,
                            Response<EnergyReservation> response) {

                        setLoading(false);

                        if (response.isSuccessful()) {
                            Toast.makeText(
                                    ScanResultActivity.this,
                                    "Energy transfer completed successfully!",
                                    Toast.LENGTH_LONG
                            ).show();
                            // Return to OperatorDashboard.
                            Intent i = new Intent(
                                    ScanResultActivity.this,
                                    OperatorDashboardActivity.class
                            );
                            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(i);
                            finish();
                            return;
                        }

                        Toast.makeText(
                                ScanResultActivity.this,
                                "Failed to complete transfer. Please try again.",
                                Toast.LENGTH_LONG
                        ).show();
                    }

                    @Override
                    public void onFailure(
                            Call<EnergyReservation> call,
                            Throwable throwable) {

                        setLoading(false);
                        Toast.makeText(
                                ScanResultActivity.this,
                                "Unable to reach the server.",
                                Toast.LENGTH_LONG
                        ).show();
                    }
                });
    }


    // ─── helpers ────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        confirmButton.setEnabled(!loading);
    }


    private String formatDateTime(String value) {
        if (value == null) return "—";
        String[] patterns = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat sdf =
                        new SimpleDateFormat(pattern, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(value);
                if (date != null) {
                    return new SimpleDateFormat(
                            "dd MMM yyyy • hh:mm a",
                            Locale.getDefault()
                    ).format(date);
                }
            } catch (ParseException ignored) { }
        }
        return value;
    }
}
