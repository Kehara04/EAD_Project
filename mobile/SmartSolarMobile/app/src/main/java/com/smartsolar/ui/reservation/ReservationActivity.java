package com.smartsolar.ui.reservation;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsolar.R;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.AvailableSlot;
import com.smartsolar.model.CreateReservationRequest;
import com.smartsolar.model.EnergyReservation;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReservationActivity
        extends AppCompatActivity {

    private ApiService apiService;

    private String stationId;
    private String stationName;

    private MaterialButton dateButton;
    private MaterialButton reserveButton;

    private Spinner slotSpinner;

    private TextInputEditText notesInput;

    private TextView errorText;

    private ProgressBar progressBar;

    private final Calendar selectedDateTime =
            Calendar.getInstance();

    private boolean dateSelected = false;
    private final List<AvailableSlot>
            availableSlots =
            new ArrayList<>();


    @Override
    protected void onCreate(
            Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_reservation
        );


        apiService =
                ApiClient.create(this);


        stationId =
                getIntent().getStringExtra(
                        "stationId"
                );

        stationName =
                getIntent().getStringExtra(
                        "stationName"
                );


        if (
                stationId == null ||
                stationId.trim().isEmpty()
        ) {
            Toast.makeText(
                    this,
                    "Station information is missing.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        TextView stationTitle =
                findViewById(
                        R.id.textReservationStation
                );

        stationTitle.setText(
                stationName == null
                        ? "Solar Station"
                        : stationName
        );


        dateButton =
                findViewById(
                        R.id.buttonReservationDate
                );

        reserveButton =
                findViewById(
                        R.id.buttonCreateReservation
                );

        slotSpinner =
                findViewById(
                        R.id.spinnerReservationSlot
                );

        notesInput =
                findViewById(
                        R.id.editReservationNotes
                );

        errorText =
                findViewById(
                        R.id.textReservationError
                );

        progressBar =
                findViewById(
                        R.id.reservationProgress
                );


        findViewById(
                R.id.buttonReservationBack
        ).setOnClickListener(
                v -> finish()
        );


        dateButton.setOnClickListener(
                v -> selectDate()
        );



        reserveButton.setOnClickListener(
                v -> createReservation()
        );
    }


    /* =========================================
       DATE
    ========================================= */

    private void selectDate() {

        Calendar now =
                Calendar.getInstance();


        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,
                        (view,
                         year,
                         month,
                         day) -> {

                            selectedDateTime.set(
                                    Calendar.YEAR,
                                    year
                            );

                            selectedDateTime.set(
                                    Calendar.MONTH,
                                    month
                            );

                            selectedDateTime.set(
                                    Calendar.DAY_OF_MONTH,
                                    day
                            );

                            selectedDateTime.set(
                                    Calendar.HOUR_OF_DAY,
                                    0
                            );

                            selectedDateTime.set(
                                    Calendar.MINUTE,
                                    0
                            );

                            selectedDateTime.set(
                                    Calendar.SECOND,
                                    0
                            );

                            selectedDateTime.set(
                                    Calendar.MILLISECOND,
                                    0
                            );


                            dateSelected = true;


                            dateButton.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            day,
                                            month + 1,
                                            year
                                    )
                            );


                            loadSlotsIfReady();
                        },

                        now.get(
                                Calendar.YEAR
                        ),

                        now.get(
                                Calendar.MONTH
                        ),

                        now.get(
                                Calendar.DAY_OF_MONTH
                        )
                );


        /*
         * Cannot choose dates
         * before today.
         */
        dialog.getDatePicker()
                .setMinDate(
                        System.currentTimeMillis()
                );


        /*
         * UI assistance only.
         *
         * Backend still enforces the
         * actual 7-day business rule.
         */
        Calendar maxDate =
                Calendar.getInstance();

        maxDate.add(
                Calendar.DAY_OF_YEAR,
                7
        );


        dialog.getDatePicker()
                .setMaxDate(
                        maxDate.getTimeInMillis()
                );


        dialog.show();
    }


    /* =========================================
       AVAILABLE SLOTS
    ========================================= */

    private void loadSlotsIfReady() {

        if (
                !dateSelected
        ) {
            return;
        }


        hideError();

        setLoading(true);


        String scheduledAt =
                toUtcApiDate();


        apiService
                .getAvailableSlots(
                        stationId,
                        scheduledAt
                )
                .enqueue(
                        new Callback<
                                List<AvailableSlot>>() {

                            @Override
                            public void onResponse(
                                    Call<List<AvailableSlot>> call,
                                    Response<List<AvailableSlot>> response) {

                                setLoading(false);

                                availableSlots.clear();


                                if (
                                        response.isSuccessful() &&
                                        response.body() != null
                                ) {

                                    availableSlots.addAll(
                                            response.body()
                                    );


                                    updateSlotSpinner();

                                    return;
                                }


                                showError(
                                        "Could not load available slots."
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<AvailableSlot>> call,
                                    Throwable throwable) {

                                setLoading(false);

                                showError(
                                        "Unable to connect to the server."
                                );
                            }
                        }
                );
    }


    private void updateSlotSpinner() {

        ArrayAdapter<AvailableSlot> adapter =
                new ArrayAdapter<AvailableSlot>(
                        this,
                        android.R.layout.simple_spinner_item,
                        availableSlots
                ) {
                    @Override
                    public boolean isEnabled(int position) {
                        AvailableSlot slot = getItem(position);
                        return slot != null && slot.isAvailable();
                    }

                    @Override
                    public View getView(
                            int position,
                            View convertView,
                            ViewGroup parent) {
                        return createSlotView(
                                position,
                                convertView,
                                parent,
                                android.R.layout.simple_spinner_item
                        );
                    }

                    @Override
                    public View getDropDownView(
                            int position,
                            View convertView,
                            ViewGroup parent) {
                        return createSlotView(
                                position,
                                convertView,
                                parent,
                                android.R.layout.simple_spinner_dropdown_item
                        );
                    }

                    private View createSlotView(
                            int position,
                            View convertView,
                            ViewGroup parent,
                            int layout) {
                        TextView view = (TextView) getLayoutInflater()
                                .inflate(layout, parent, false);
                        AvailableSlot slot = getItem(position);
                        view.setText(
                                slot.isAvailable()
                                        ? slot.getLabel()
                                        : slot.getLabel() + " (Booked)"
                        );
                        view.setEnabled(slot.isAvailable());
                        view.setAlpha(slot.isAvailable() ? 1f : 0.5f);
                        return view;
                    }
                };


        slotSpinner.setAdapter(
                adapter
        );


        reserveButton.setEnabled(
                hasAvailableSlot()
        );
    }


    /* =========================================
       CREATE RESERVATION
    ========================================= */

    private void createReservation() {

        hideError();


        if (!dateSelected) {
            showError(
                    "Please select a date."
            );

            return;
        }


        if (availableSlots.isEmpty()) {
            showError(
                    "No available booking slot has been selected."
            );

            return;
        }


        int position =
                slotSpinner
                        .getSelectedItemPosition();


        if (
                position < 0 ||
                position >=
                        availableSlots.size()
        ) {
            showError(
                    "Please select an available slot."
            );

            return;
        }


        AvailableSlot selectedSlot =
                availableSlots.get(
                        position
                );

        if (!selectedSlot.isAvailable()) {
            showError(
                    "The selected slot is already booked for this date."
            );

            return;
        }


        String notes =
                notesInput.getText() == null
                        ? ""
                        : notesInput
                        .getText()
                        .toString()
                        .trim();


        CreateReservationRequest request =
                new CreateReservationRequest(
                        stationId,
                        selectedSlot.getSlotId(),
                        toUtcApiDate(),
                        notes
                );


        setLoading(true);


        apiService
                .createReservation(
                        request
                )
                .enqueue(
                        new Callback<
                                EnergyReservation>() {

                            @Override
                            public void onResponse(
                                    Call<EnergyReservation> call,
                                    Response<EnergyReservation> response) {

                                setLoading(false);


                                if (
                                        response.isSuccessful() &&
                                        response.body() != null
                                ) {

                                    Toast.makeText(
                                            ReservationActivity.this,
                                            "Reservation created successfully.",
                                            Toast.LENGTH_LONG
                                    ).show();


                                    finish();

                                    return;
                                }


                                showError(
                                        "Reservation could not be created."
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<EnergyReservation> call,
                                    Throwable throwable) {

                                setLoading(false);

                                showError(
                                        "Unable to connect to the server."
                                );
                            }
                        }
                );
    }


    /* =========================================
       DATE FOR ASP.NET API
    ========================================= */

    private String toUtcApiDate() {

        SimpleDateFormat format =
                new SimpleDateFormat(
                        "yyyy-MM-dd'T'HH:mm:ss'Z'",
                        Locale.US
                );


        format.setTimeZone(
                TimeZone.getTimeZone(
                        "UTC"
                )
        );


        return format.format(
                selectedDateTime.getTime()
        );
    }


    /* =========================================
       UI HELPERS
    ========================================= */

    private void setLoading(
            boolean loading) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );

        reserveButton.setEnabled(
                !loading &&
                hasAvailableSlot()
        );
    }


    private void showError(
            String message) {

        errorText.setText(
                message
        );

        errorText.setVisibility(
                View.VISIBLE
        );
    }


    private void hideError() {

        errorText.setVisibility(
                View.GONE
        );
    }

        private boolean hasAvailableSlot() {
                for (AvailableSlot slot : availableSlots) {
                        if (slot.isAvailable()) {
                                return true;
                        }
                }

                return false;
        }
}