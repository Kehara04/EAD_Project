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
import com.smartsolar.model.EnergyReservation;
import com.smartsolar.model.UpdateReservationRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpdateReservationActivity extends AppCompatActivity {

    private ApiService apiService;

    private String reservationId;
    private String stationId;
    private String stationName;

    private MaterialButton dateButton;
    private MaterialButton updateButton;

    private Spinner slotSpinner;
    private TextInputEditText notesInput;
    private TextView errorText;
    private ProgressBar progressBar;

    private final Calendar selectedDateTime =
            Calendar.getInstance();

    private final List<AvailableSlot> availableSlots =
            new ArrayList<>();

    private boolean dateSelected = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_update_reservation
        );


        apiService =
                ApiClient.create(this);


        reservationId =
                getIntent().getStringExtra(
                        "reservationId"
                );

        stationId =
                getIntent().getStringExtra(
                        "stationId"
                );

        stationName =
                getIntent().getStringExtra(
                        "stationName"
                );


        if (
                reservationId == null
                        ||
                        reservationId.trim().isEmpty()
                        ||
                        stationId == null
                        ||
                        stationId.trim().isEmpty()
        ) {

            Toast.makeText(
                    this,
                    "Reservation information is missing.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        TextView stationTitle =
                findViewById(
                        R.id.textUpdateReservationStation
                );


        stationTitle.setText(
                stationName == null
                        || stationName.trim().isEmpty()

                        ? "Solar Station"

                        : stationName
        );


        dateButton =
                findViewById(
                        R.id.buttonUpdateReservationDate
                );

        updateButton =
                findViewById(
                        R.id.buttonSaveReservationUpdate
                );

        slotSpinner =
                findViewById(
                        R.id.spinnerUpdateReservationSlot
                );

        notesInput =
                findViewById(
                        R.id.editUpdateReservationNotes
                );

        errorText =
                findViewById(
                        R.id.textUpdateReservationError
                );

        progressBar =
                findViewById(
                        R.id.updateReservationProgress
                );


        String currentNotes =
                getIntent().getStringExtra(
                        "notes"
                );


        if (currentNotes != null) {

            notesInput.setText(
                    currentNotes
            );
        }


        findViewById(
                R.id.buttonUpdateReservationBack
        ).setOnClickListener(
                v -> finish()
        );


        dateButton.setOnClickListener(
                v -> selectDate()
        );


        updateButton.setOnClickListener(
                v -> updateReservation()
        );
    }


    private void selectDate() {

        Calendar now =
                Calendar.getInstance();


        DatePickerDialog dialog =
                new DatePickerDialog(
                        this,

                        (
                                view,
                                year,
                                month,
                                day
                        ) -> {

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


                            dateSelected =
                                    true;


                            dateButton.setText(
                                    String.format(
                                            Locale.getDefault(),
                                            "%02d/%02d/%04d",
                                            day,
                                            month + 1,
                                            year
                                    )
                            );


                            hideError();

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


        dialog.getDatePicker()
                .setMinDate(
                        System.currentTimeMillis()
                );


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


    private void loadSlotsIfReady() {

        if (!dateSelected) {

            return;
        }


        setLoading(true);


        apiService
                .getAvailableSlots(
                        stationId,
                        toUtcApiDate()
                )
                .enqueue(
                        new Callback<List<AvailableSlot>>() {

                            @Override
                            public void onResponse(
                                    Call<List<AvailableSlot>> call,
                                    Response<List<AvailableSlot>> response
                            ) {

                                setLoading(false);

                                availableSlots.clear();


                                if (
                                        response.isSuccessful()
                                                &&
                                        response.body() != null
                                ) {

                                    for (
                                            AvailableSlot slot :
                                            response.body()
                                    ) {

                                        if (
                                                slot != null
                                                        &&
                                                        slot.isAvailable()
                                        ) {

                                            availableSlots.add(
                                                    slot
                                            );
                                        }
                                    }


                                    updateSlotSpinner();


                                    if (
                                            availableSlots.isEmpty()
                                    ) {

                                        showError(
                                                "No available slots for the selected date and time."
                                        );
                                    } else {

                                        hideError();
                                    }


                                    return;
                                }


                                updateSlotSpinner();


                                showError(
                                        "Could not load available slots."
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<List<AvailableSlot>> call,
                                    Throwable throwable
                            ) {

                                setLoading(false);

                                availableSlots.clear();

                                updateSlotSpinner();


                                showError(
                                        "Unable to reach the server."
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
                                parent,
                                android.R.layout.simple_spinner_dropdown_item
                        );
                    }

                    private View createSlotView(
                            int position,
                            ViewGroup parent,
                            int layout) {
                        TextView view = (TextView) getLayoutInflater()
                                .inflate(layout, parent, false);
                        AvailableSlot slot = getItem(position);
                        String label = slot.getLabel();

                        if (label == null || label.trim().isEmpty()) {
                            label = "Slot " + slot.getSlotNumber();
                        }

                        view.setText(
                                slot.isAvailable()
                                        ? label
                                        : label + " (Booked)"
                        );
                        view.setEnabled(slot.isAvailable());
                        view.setAlpha(slot.isAvailable() ? 1f : 0.5f);
                        return view;
                    }
                };


        adapter.setDropDownViewResource(
                android.R.layout
                        .simple_spinner_dropdown_item
        );


        slotSpinner.setAdapter(
                adapter
        );


        updateButton.setEnabled(
                hasAvailableSlot()
        );
    }


    private boolean hasAvailableSlot() {

        for (AvailableSlot slot : availableSlots) {
            if (slot != null && slot.isAvailable()) {
                return true;
            }
        }

        return false;
    }


    private void updateReservation() {

        hideError();


        if (!dateSelected) {

            showError(
                    "Please select the new date."
            );

            return;
        }


        if (
                availableSlots.isEmpty()
        ) {

            showError(
                    "No available slot has been selected."
            );

            return;
        }


        int position =
                slotSpinner
                        .getSelectedItemPosition();


        if (
                position < 0
                        ||
                        position >= availableSlots.size()
        ) {

            showError(
                    "Please select a valid slot."
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


        UpdateReservationRequest request =
                new UpdateReservationRequest(
                        stationId,
                        selectedSlot.getSlotId(),
                        toUtcApiDate(),
                        notes
                );


        setLoading(true);


        apiService
                .updateReservation(
                        reservationId,
                        request
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

                                    Toast.makeText(
                                            UpdateReservationActivity.this,
                                            "Reservation updated successfully.",
                                            Toast.LENGTH_LONG
                                    ).show();


                                    finish();

                                    return;
                                }


                                if (
                                        response.code() == 400
                                ) {

                                    showError(
                                            "The reservation could not be updated. Make sure it is more than 12 hours away and the selected slot is still available."
                                    );

                                    return;
                                }


                                if (
                                        response.code() == 404
                                ) {

                                    showError(
                                            "Reservation was not found."
                                    );

                                    return;
                                }


                                showError(
                                        "Reservation could not be updated."
                                );
                            }


                            @Override
                            public void onFailure(
                                    Call<EnergyReservation> call,
                                    Throwable throwable
                            ) {

                                setLoading(false);


                                showError(
                                        "Unable to reach the server."
                                );
                            }
                        }
                );
    }


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


    private void setLoading(
            boolean loading
    ) {

        progressBar.setVisibility(
                loading
                        ? View.VISIBLE
                        : View.GONE
        );


        dateButton.setEnabled(
                !loading
        );


        slotSpinner.setEnabled(
                !loading
        );


        updateButton.setEnabled(
                !loading
                        &&
                        hasAvailableSlot()
        );
    }


    private void showError(
            String message
    ) {

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
}