package com.smartsolar.ui.reservation;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsolar.R;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.data.remote.ApiService;
import com.smartsolar.model.EnergyReservation;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Displays and manages the authenticated prosumer's energy reservations.
 * Retrieves reservation records from the backend API and allows users
 * to search, filter by reservation status, and open reservation details.
 */
public class MyReservationsActivity extends AppCompatActivity {

    private ApiService apiService;

    private RecyclerView recyclerView;
    private ReservationAdapter adapter;

    private ProgressBar progressBar;
    private TextView emptyText;

    private TextInputEditText searchInput;

    private MaterialButton buttonAll;
    private MaterialButton buttonPending;
    private MaterialButton buttonApproved;
    private MaterialButton buttonHistory;

    private final List<EnergyReservation>
            reservations =
            new ArrayList<>();

    private String selectedStatus = null;

    // Initializes the reservation list, search interface, and status filter buttons.
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_my_reservations
        );


        apiService =
                ApiClient.create(this);


        recyclerView =
                findViewById(
                        R.id.recyclerReservations
                );

        progressBar =
                findViewById(
                        R.id.reservationsProgress
                );

        emptyText =
                findViewById(
                        R.id.textReservationsEmpty
                );

        searchInput =
                findViewById(
                        R.id.editReservationSearch
                );


        buttonAll =
                findViewById(
                        R.id.buttonFilterAll
                );

        buttonPending =
                findViewById(
                        R.id.buttonFilterPending
                );

        buttonApproved =
                findViewById(
                        R.id.buttonFilterApproved
                );

        buttonHistory =
                findViewById(
                        R.id.buttonFilterHistory
                );


        adapter =
                new ReservationAdapter(
                        reservations,
                        reservation -> {
                            ReservationDetailsActivity.open(
                                    MyReservationsActivity.this,
                                    reservation.getId()
                            );
                        }
                );


        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(
                adapter
        );


        ((com.google.android.material.appbar.MaterialToolbar) findViewById(
                R.id.buttonReservationsBack
        )).setNavigationOnClickListener(
                v -> finish()
        );


        buttonAll.setOnClickListener(v -> {
            selectedStatus = null;
            loadReservations();
        });


        buttonPending.setOnClickListener(v -> {
            selectedStatus = "Pending";
            loadReservations();
        });


        buttonApproved.setOnClickListener(v -> {
            selectedStatus = "Approved";
            loadReservations();
        });


        buttonHistory.setOnClickListener(v -> {
            selectedStatus = "History";
            loadReservations();
        });


        findViewById(
                R.id.buttonReservationSearch
        ).setOnClickListener(
                v -> loadReservations()
        );


        loadReservations();
    }

    // Refreshes reservation information when the user returns to this screen.
    @Override
    protected void onResume() {
        super.onResume();

        loadReservations();
    }

    // Retrieves the authenticated prosumer's reservations from the backend API.
    private void loadReservations() {

        progressBar.setVisibility(
                View.VISIBLE
        );

        emptyText.setVisibility(
                View.GONE
        );


        String search =
                searchInput.getText() == null
                        ? null
                        : searchInput
                        .getText()
                        .toString()
                        .trim();

        String apiStatus =
                "History".equals(selectedStatus)
                        ? null
                        : selectedStatus;


        apiService
                .getMyReservations(
                        apiStatus,
                        search
                )
                .enqueue(
                        new Callback<
                                List<EnergyReservation>>() {

                            // Processes the API response, filters historical records, and updates the reservation list.
                            @Override
                            public void onResponse(
                                    Call<List<EnergyReservation>> call,
                                    Response<List<EnergyReservation>> response) {

                                progressBar.setVisibility(
                                        View.GONE
                                );


                                reservations.clear();


                                if (
                                        response.isSuccessful()
                                                &&
                                        response.body() != null
                                ) {

                                    for (
                                            EnergyReservation reservation :
                                            response.body()
                                    ) {

                                        if (
                                                "History"
                                                        .equals(
                                                                selectedStatus
                                                        )
                                        ) {

                                            String status =
                                                    reservation
                                                            .getStatus();


                                            if (
                                                    "Cancelled"
                                                            .equalsIgnoreCase(
                                                                    status
                                                            )

                                                            ||

                                                    "Completed"
                                                            .equalsIgnoreCase(
                                                                    status
                                                            )
                                            ) {

                                                reservations.add(
                                                        reservation
                                                );
                                            }

                                        } else {

                                            reservations.add(
                                                    reservation
                                            );
                                        }
                                    }


                                    adapter.notifyDataSetChanged();


                                    emptyText.setVisibility(
                                            reservations.isEmpty()
                                                    ? View.VISIBLE
                                                    : View.GONE
                                    );


                                    return;
                                }


                                Toast.makeText(
                                        MyReservationsActivity.this,
                                        "Could not load reservations.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }

                            // Handles network failures encountered while retrieving reservation records.
                            @Override
                            public void onFailure(
                                    Call<List<EnergyReservation>> call,
                                    Throwable throwable) {

                                progressBar.setVisibility(
                                        View.GONE
                                );


                                Toast.makeText(
                                        MyReservationsActivity.this,
                                        "Unable to reach the server.",
                                        Toast.LENGTH_LONG
                                ).show();
                            }
                        }
                );
    }
}