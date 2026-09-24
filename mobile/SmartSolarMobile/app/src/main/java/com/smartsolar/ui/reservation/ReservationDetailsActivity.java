/*
 * File: ReservationDetailsActivity.java
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 *
 * Description:
 * Displays the complete details of a selected energy reservation.
 *
 * Retrieves reservation information from the backend, displays
 * the associated station and schedule, and allows eligible
 * reservations to be updated or cancelled.
 *
 * Applies client-side checks for reservation status and the
 * twelve-hour modification restriction.
 */
package com.smartsolar.ui.reservation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.smartsolar.R;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.EnergyReservation;
import com.smartsolar.ui.reservation.QrDisplayActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationDetailsActivity
        extends AppCompatActivity {

    private static final String EXTRA_RESERVATION_ID =
            "reservationId";

    private ApiService apiService;

    private String reservationId;

    private TextView stationNameText;
    private TextView stationAddressText;
    private TextView scheduleText;
    private TextView slotText;
    private TextView statusText;
    private TextView notesText;
    private TextView ruleText;

    private MaterialButton updateButton;
    private MaterialButton cancelButton;
    private MaterialButton showQrButton;

    private ProgressBar progressBar;

    private EnergyReservation reservation;

        // Opens the reservation details screen using the selected reservation identifier.
    public static void open(
            Context context,
            String reservationId
    ) {
        Intent intent =
                new Intent(
                        context,
                        ReservationDetailsActivity.class
                );

        intent.putExtra(
                EXTRA_RESERVATION_ID,
                reservationId
        );

        context.startActivity(intent);
    }

        // Initializes the reservation details screen and its action buttons.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_reservation_details
        );


        apiService =
                ApiClient.create(this);


        reservationId =
                getIntent().getStringExtra(
                        EXTRA_RESERVATION_ID
                );


        if (
                reservationId == null ||
                reservationId.trim().isEmpty()
        ) {
            Toast.makeText(
                    this,
                    "Reservation information is missing.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        stationNameText =
                findViewById(
                        R.id.textDetailStationName
                );

        stationAddressText =
                findViewById(
                        R.id.textDetailStationAddress
                );

        scheduleText =
                findViewById(
                        R.id.textDetailSchedule
                );

        slotText =
                findViewById(
                        R.id.textDetailSlot
                );

        statusText =
                findViewById(
                        R.id.textDetailStatus
                );

        notesText =
                findViewById(
                        R.id.textDetailNotes
                );

        ruleText =
                findViewById(
                        R.id.textDetailRule
                );

        updateButton =
                findViewById(
                        R.id.buttonUpdateReservation
                );

        cancelButton =
                findViewById(
                        R.id.buttonCancelReservation
                );

        showQrButton =
                findViewById(
                        R.id.buttonShowQr
                );

        progressBar =
                findViewById(
                        R.id.reservationDetailProgress
                );


        ((com.google.android.material.appbar.MaterialToolbar) findViewById(
                R.id.buttonReservationDetailBack
        )).setNavigationOnClickListener(
                v -> finish()
        );


        updateButton.setOnClickListener(
                v -> openUpdateScreen()
        );


        cancelButton.setOnClickListener(
                v -> confirmCancellation()
        );


        // Opens QR display for Approved reservations.
        showQrButton.setOnClickListener(
                v -> QrDisplayActivity.open(
                        this,
                        reservationId
                )
        );
    }

        // Refreshes reservation information when the screen becomes active.
    @Override
    protected void onResume() {

        super.onResume();

        loadReservation();
    }

        // Retrieves the selected reservation and its latest status from the backend API.
    private void loadReservation() {

        setLoading(true);


        apiService
                .getReservation(
                        reservationId
                )
                .enqueue(
                        new Callback<EnergyReservation>() {

                            @Override
                            public void onResponse(
                                    Call<EnergyReservation> call,
                                    Response<EnergyReservation> response
                            ) {

                                setLoading(false);


                                if (
                                        response.isSuccessful()
                                                &&
                                        response.body() != null
                                ) {

                                    reservation =
                                            response.body();

                                    bindReservation();

                                    return;
                                }


                                if (
                                        response.code() == 404
                                ) {

                                    Toast.makeText(
                                            ReservationDetailsActivity.this,
                                            "Reservation was not found.",
                                            Toast.LENGTH_LONG
                                    ).show();

                                    finish();

                                    return;
                                }


                                Toast.makeText(
                                        ReservationDetailsActivity.this,
                                        "Could not load reservation.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }


                            @Override
                            public void onFailure(
                                    Call<EnergyReservation> call,
                                    Throwable throwable
                            ) {

                                setLoading(false);

                                Toast.makeText(
                                        ReservationDetailsActivity.this,
                                        "Unable to reach the server.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }

        // Displays station, schedule, slot, status, and notes in the corresponding UI fields.
    private void bindReservation() {

        stationNameText.setText(
                safe(
                        reservation.getStationName(),
                        "Unknown station"
                )
        );


        stationAddressText.setText(
                safe(
                        reservation.getStationAddress(),
                        "Address unavailable"
                )
        );


        scheduleText.setText(
                formatDateTime(
                        reservation.getScheduledAt()
                )
        );


        slotText.setText(
                "Slot "
                        +
                        reservation.getSlotNumber()
        );


        statusText.setText(
                safe(
                        reservation.getStatus(),
                        "-"
                )
        );


        notesText.setText(
                safe(
                        reservation.getNotes(),
                        "No notes added."
                )
        );


        updateActionState();
    }

        // Enables or disables modification actions based on reservation status and time restrictions.
    private void updateActionState() {

        String status =
                reservation.getStatus();


        boolean finalState =
                "Cancelled".equalsIgnoreCase(
                        status
                )
                ||
                "Completed".equalsIgnoreCase(
                        status
                );


        boolean enoughNotice =
                hasTwelveHoursNotice(
                        reservation.getScheduledAt()
                );


        boolean canModify =
                !finalState
                        &&
                        enoughNotice;


        updateButton.setEnabled(
                canModify
        );


        cancelButton.setEnabled(
                canModify
        );


        // Show QR button only for Approved reservations.
        boolean isApproved =
                "Approved".equalsIgnoreCase(status);

        showQrButton.setVisibility(
                isApproved
                        ? View.VISIBLE
                        : View.GONE
        );


        if (finalState) {

            ruleText.setText(
                    "This reservation can no longer be changed."
            );

        } else if (!enoughNotice) {

            ruleText.setText(
                    "Updates and cancellations require at least 12 hours' notice."
            );

        } else {

            ruleText.setText(
                    "You may update or cancel this reservation. The server will verify the 12-hour rule again."
            );
        }
    }

        // Opens the reservation update screen with the current booking information.
    private void openUpdateScreen() {

        if (
                reservation == null
        ) {
            return;
        }


        if (
                !updateButton.isEnabled()
        ) {

            Toast.makeText(
                    this,
                    "This reservation cannot be updated.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        Intent intent =
                new Intent(
                        this,
                        UpdateReservationActivity.class
                );


        intent.putExtra(
                "reservationId",
                reservation.getId()
        );


        intent.putExtra(
                "stationId",
                reservation.getStationId()
        );


        intent.putExtra(
                "stationName",
                reservation.getStationName()
        );


        intent.putExtra(
                "scheduledAt",
                reservation.getScheduledAt()
        );


        intent.putExtra(
                "notes",
                reservation.getNotes()
        );


        startActivity(intent);
    }

        // Requests confirmation from the prosumer before cancelling the reservation.
    private void confirmCancellation() {

        if (
                reservation == null
        ) {
            return;
        }


        if (
                !cancelButton.isEnabled()
        ) {

            Toast.makeText(
                    this,
                    "This reservation cannot be cancelled.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        new AlertDialog.Builder(this)
                .setTitle(
                        "Cancel reservation?"
                )
                .setMessage(
                        "This will release the reserved energy slot."
                )
                .setPositiveButton(
                        "Cancel reservation",
                        (dialog, which) ->
                                cancelReservation()
                )
                .setNegativeButton(
                        "Keep reservation",
                        null
                )
                .show();
    }

        // Sends the cancellation request to the backend and updates the displayed reservation status.
    private void cancelReservation() {

        setLoading(true);


        apiService
                .cancelReservation(
                        reservationId
                )
                .enqueue(
                        new Callback<EnergyReservation>() {

                            @Override
                            public void onResponse(
                                    Call<EnergyReservation> call,
                                    Response<EnergyReservation> response
                            ) {

                                setLoading(false);


                                if (
                                        response.isSuccessful()
                                                &&
                                        response.body() != null
                                ) {

                                    reservation =
                                            response.body();


                                    bindReservation();


                                    Toast.makeText(
                                            ReservationDetailsActivity.this,
                                            "Reservation cancelled successfully.",
                                            Toast.LENGTH_LONG
                                    ).show();


                                    return;
                                }


                                Toast.makeText(
                                        ReservationDetailsActivity.this,
                                        "Reservation could not be cancelled. It may be inside the 12-hour restriction.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }


                            @Override
                            public void onFailure(
                                    Call<EnergyReservation> call,
                                    Throwable throwable
                            ) {

                                setLoading(false);


                                Toast.makeText(
                                        ReservationDetailsActivity.this,
                                        "Unable to reach the server.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }


    private boolean hasTwelveHoursNotice(
            String scheduledAt
    ) {

        Date date =
                parseApiDate(
                        scheduledAt
                );


        if (
                date == null
        ) {
            return false;
        }


        long difference =
                date.getTime()
                        -
                        System.currentTimeMillis();


        return difference
                >=
                12L
                        *
                        60L
                        *
                        60L
                        *
                        1000L;
    }

        // Parses the reservation timestamp received from the backend API.
    private Date parseApiDate(
            String value
    ) {

        if (
                value == null
        ) {
            return null;
        }


        String[] formats = {
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ss'Z'"
        };


        for (
                String pattern :
                formats
        ) {

            try {

                SimpleDateFormat format =
                        new SimpleDateFormat(
                                pattern,
                                Locale.US
                        );


                format.setTimeZone(
                        TimeZone.getTimeZone(
                                "UTC"
                        )
                );


                return format.parse(
                        value
                );


            } catch (
                    ParseException ignored
            ) {
            }
        }


        return null;
    }

        // Converts the reservation timestamp into a human-readable format for display.
    private String formatDateTime(
            String value
    ) {

        Date date =
                parseApiDate(
                        value
                );


        if (
                date == null
        ) {
            return safe(
                    value,
                    "Schedule unavailable"
            );
        }


        SimpleDateFormat output =
                new SimpleDateFormat(
                        "dd MMM yyyy • hh:mm a",
                        Locale.getDefault()
                );


        return output.format(
                date
        );
    }


    private void setLoading(
            boolean loading
    ) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );


        if (loading) {

            updateButton.setEnabled(
                    false
            );

            cancelButton.setEnabled(
                    false
            );
        }
    }


    private String safe(
            String value,
            String fallback
    ) {

        return value == null
                ||
                value.trim().isEmpty()

                ? fallback

                : value;
    }
}