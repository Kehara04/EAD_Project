package com.smartsolar.ui.reservation;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.smartsolar.R;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.QrPayload;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Prosumer screen that fetches a signed QR payload from the server
 * and renders it as a scannable QR code bitmap.
 * Shown only for reservations in the "Approved" state.
 */
public class QrDisplayActivity extends AppCompatActivity {

    private static final String EXTRA_RESERVATION_ID = "reservationId";

    // QR image size in pixels.
    private static final int QR_SIZE = 600;

    private ApiService apiService;
    private String     reservationId;

    private ImageView   qrImageView;
    private TextView    textStation;
    private TextView    textSlot;
    private TextView    textSchedule;
    private ProgressBar progressBar;
    private View        qrCard;


    /**
     * Convenience launcher so callers do not need to know the Intent extra key.
     */
    public static void open(
            Context context,
            String  reservationId) {

        Intent intent = new Intent(context, QrDisplayActivity.class);
        intent.putExtra(EXTRA_RESERVATION_ID, reservationId);
        context.startActivity(intent);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_display);

        apiService    = ApiClient.create(this);
        reservationId = getIntent().getStringExtra(EXTRA_RESERVATION_ID);

        if (reservationId == null || reservationId.trim().isEmpty()) {
            Toast.makeText(this,
                    "Reservation ID is missing.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        qrImageView  = findViewById(R.id.imageQrCode);
        textStation  = findViewById(R.id.textQrStation);
        textSlot     = findViewById(R.id.textQrSlot);
        textSchedule = findViewById(R.id.textQrSchedule);
        progressBar  = findViewById(R.id.progressQrDisplay);
        qrCard       = findViewById(R.id.cardQrContent);

        // Back button.
        findViewById(R.id.buttonQrBack)
                .setOnClickListener(v -> finish());

        loadQrPayload();
    }


    /**
     * Fetches the signed QR payload from the API then renders the bitmap.
     */
    private void loadQrPayload() {

        setLoading(true);

        apiService.getQrPayload(reservationId)
                .enqueue(new Callback<QrPayload>() {

                    @Override
                    public void onResponse(
                            Call<QrPayload> call,
                            Response<QrPayload> response) {

                        setLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            renderQr(response.body());
                            return;
                        }

                        Toast.makeText(QrDisplayActivity.this,
                                "Could not load QR code. The reservation may not be approved.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }

                    @Override
                    public void onFailure(
                            Call<QrPayload> call,
                            Throwable throwable) {

                        setLoading(false);
                        Toast.makeText(QrDisplayActivity.this,
                                "Unable to reach the server.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                });
    }


    /**
     * Serialises the QrPayload to a compact JSON string and encodes it
     * into a QR code bitmap using the ZXing BarcodeEncoder.
     */
    private void renderQr(QrPayload payload) {

        // Build the JSON string that the operator's scanner will read back.
        String json = buildJson(payload);

        try {
            BarcodeEncoder encoder = new BarcodeEncoder();
            Bitmap bitmap = encoder.encodeBitmap(
                    json,
                    BarcodeFormat.QR_CODE,
                    QR_SIZE,
                    QR_SIZE
            );

            qrImageView.setImageBitmap(bitmap);

            // Populate info labels.
            textStation.setText(safe(payload.getStationId(), "—"));
            textSlot.setText("Slot " + payload.getSlotNumber());
            textSchedule.setText(formatDateTime(payload.getScheduledAt()));

            qrCard.setVisibility(View.VISIBLE);

        } catch (WriterException e) {
            Toast.makeText(this,
                    "Failed to generate QR image.",
                    Toast.LENGTH_LONG).show();
            finish();
        }
    }


    /**
     * Produces a minimal JSON string that the operator endpoint can deserialise.
     * Using manual JSON construction avoids adding a JSON library dependency.
     */
    private String buildJson(QrPayload p) {
        return "{"
                + "\"reservationId\":\"" + p.getReservationId() + "\","
                + "\"prosumerId\":\""    + p.getProsumerId()    + "\","
                + "\"stationId\":\""     + p.getStationId()     + "\","
                + "\"slotNumber\":"      + p.getSlotNumber()    + ","
                + "\"scheduledAt\":\""   + p.getScheduledAt()   + "\","
                + "\"issuedAt\":\""      + p.getIssuedAt()      + "\","
                + "\"signature\":\""     + p.getSignature()     + "\""
                + "}";
    }


    // ─── helpers ────────────────────────────────────────────────────────────

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        if (qrCard != null) {
            qrCard.setVisibility(loading ? View.GONE : View.VISIBLE);
        }
    }


    private String safe(String value, String fallback) {
        return (value == null || value.trim().isEmpty()) ? fallback : value;
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
                SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date date = sdf.parse(value);
                if (date != null) {
                    return new SimpleDateFormat(
                            "dd MMM yyyy • hh:mm a", Locale.getDefault()
                    ).format(date);
                }
            } catch (ParseException ignored) { }
        }
        return value;
    }
}
