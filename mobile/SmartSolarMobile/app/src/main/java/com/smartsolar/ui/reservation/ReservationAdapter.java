// package com.smartsolar.ui.reservation;

// import android.view.LayoutInflater;
// import android.view.View;
// import android.view.ViewGroup;
// import android.widget.TextView;

// import androidx.annotation.NonNull;
// import androidx.recyclerview.widget.RecyclerView;

// import com.google.android.material.button.MaterialButton;
// import com.smartsolar.R;
// import com.smartsolar.model.EnergyReservation;

// import java.util.List;

// public class ReservationAdapter
//         extends RecyclerView.Adapter<
//         ReservationAdapter.ViewHolder> {

//     public interface OnReservationClickListener {
//         void onClick(
//                 EnergyReservation reservation
//         );
//     }


//     private final List<EnergyReservation>
//             reservations;

//     private final OnReservationClickListener
//             listener;


//     public ReservationAdapter(
//             List<EnergyReservation> reservations,
//             OnReservationClickListener listener
//     ) {
//         this.reservations = reservations;
//         this.listener = listener;
//     }


//     @NonNull
//     @Override
//     public ViewHolder onCreateViewHolder(
//             @NonNull ViewGroup parent,
//             int viewType
//     ) {

//         View view =
//                 LayoutInflater
//                         .from(
//                                 parent.getContext()
//                         )
//                         .inflate(
//                                 R.layout.item_reservation,
//                                 parent,
//                                 false
//                         );


//         return new ViewHolder(
//                 view
//         );
//     }


//     @Override
//     public void onBindViewHolder(
//             @NonNull ViewHolder holder,
//             int position
//     ) {

//         EnergyReservation reservation =
//                 reservations.get(
//                         position
//                 );


//         holder.stationName.setText(
//                 reservation.getStationName()
//         );


//         holder.address.setText(
//                 reservation.getStationAddress()
//         );


//         holder.schedule.setText(
//                 reservation.getScheduledAt()
//         );


//         holder.slot.setText(
//                 "Slot "
//                         +
//                         reservation.getSlotNumber()
//         );


//         holder.status.setText(
//                 reservation.getStatus()
//         );


//         holder.viewButton
//                 .setOnClickListener(
//                         v ->
//                                 listener.onClick(
//                                         reservation
//                                 )
//                 );


//         holder.itemView
//                 .setOnClickListener(
//                         v ->
//                                 listener.onClick(
//                                         reservation
//                                 )
//                 );
//     }


//     @Override
//     public int getItemCount() {
//         return reservations.size();
//     }


//     static class ViewHolder
//             extends RecyclerView.ViewHolder {

//         TextView stationName;
//         TextView address;
//         TextView schedule;
//         TextView slot;
//         TextView status;

//         MaterialButton viewButton;


//         ViewHolder(
//                 @NonNull View itemView
//         ) {
//             super(itemView);


//             stationName =
//                     itemView.findViewById(
//                             R.id.textReservationStationName
//                     );

//             address =
//                     itemView.findViewById(
//                             R.id.textReservationAddress
//                     );

//             schedule =
//                     itemView.findViewById(
//                             R.id.textReservationSchedule
//                     );

//             slot =
//                     itemView.findViewById(
//                             R.id.textReservationSlot
//                     );

//             status =
//                     itemView.findViewById(
//                             R.id.textReservationStatus
//                     );

//             viewButton =
//                     itemView.findViewById(
//                             R.id.buttonViewReservation
//                     );
//         }
//     }
// }

package com.smartsolar.ui.reservation;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.smartsolar.R;
import com.smartsolar.model.EnergyReservation;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReservationAdapter
        extends RecyclerView.Adapter<ReservationAdapter.ViewHolder> {

    public interface OnReservationClickListener {
        void onClick(EnergyReservation reservation);
    }

    private final List<EnergyReservation> reservations;
    private final OnReservationClickListener listener;


    public ReservationAdapter(
            List<EnergyReservation> reservations,
            OnReservationClickListener listener
    ) {
        this.reservations = reservations;
        this.listener = listener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater
                        .from(parent.getContext())
                        .inflate(
                                R.layout.item_reservation,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        EnergyReservation reservation =
                reservations.get(position);


        /* =====================================
           STATION
        ===================================== */

        holder.stationName.setText(
                safe(
                        reservation.getStationName(),
                        "Unknown station"
                )
        );


        holder.address.setText(
                safe(
                        reservation.getStationAddress(),
                        "Address unavailable"
                )
        );


        /* =====================================
           SCHEDULE
        ===================================== */

        holder.schedule.setText(
                formatDateTime(
                        reservation.getScheduledAt()
                )
        );


        /* =====================================
           SLOT
        ===================================== */

        holder.slot.setText(
                "Energy Slot "
                        +
                        reservation.getSlotNumber()
        );


        /* =====================================
           STATUS
        ===================================== */

        String status =
                safe(
                        reservation.getStatus(),
                        "Unknown"
                );


        holder.status.setText(status);

        applyStatusStyle(
                holder.status,
                status
        );


        /* =====================================
           CLICK
        ===================================== */

        holder.viewButton.setOnClickListener(
                v -> listener.onClick(reservation)
        );


        holder.itemView.setOnClickListener(
                v -> listener.onClick(reservation)
        );
    }


    @Override
    public int getItemCount() {
        return reservations.size();
    }


    /* =========================================
       STATUS DESIGN
    ========================================= */

    private void applyStatusStyle(
            TextView statusView,
            String status
    ) {

        int backgroundColor;
        int textColor;


        if (
                "Pending".equalsIgnoreCase(status)
        ) {

            backgroundColor =
                    Color.parseColor("#FFF3CD");

            textColor =
                    Color.parseColor("#8A6500");

        } else if (
                "Approved".equalsIgnoreCase(status)
        ) {

            backgroundColor =
                    Color.parseColor("#DFF6E8");

            textColor =
                    Color.parseColor("#067647");

        } else if (
                "Cancelled".equalsIgnoreCase(status)
        ) {

            backgroundColor =
                    Color.parseColor("#FDE7E7");

            textColor =
                    Color.parseColor("#B42318");

        } else if (
                "Completed".equalsIgnoreCase(status)
        ) {

            backgroundColor =
                    Color.parseColor("#E8EEF4");

            textColor =
                    Color.parseColor("#475467");

        } else {

            backgroundColor =
                    Color.parseColor("#EEF2F0");

            textColor =
                    Color.parseColor("#344054");
        }


        statusView.setBackgroundTintList(
                ColorStateList.valueOf(
                        backgroundColor
                )
        );


        statusView.setTextColor(
                textColor
        );
    }


    /* =========================================
       FORMAT API DATE
    ========================================= */

    private String formatDateTime(
            String value
    ) {

        Date date =
                parseApiDate(value);


        if (date == null) {

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


        return output.format(date);
    }


    private Date parseApiDate(
            String value
    ) {

        if (value == null) {
            return null;
        }


        String[] formats = {

                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'",

                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",

                "yyyy-MM-dd'T'HH:mm:ss'Z'"
        };


        for (String pattern : formats) {

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


                return format.parse(value);

            } catch (ParseException ignored) {

            }
        }


        return null;
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


    /* =========================================
       VIEW HOLDER
    ========================================= */

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView stationName;
        TextView address;
        TextView schedule;
        TextView slot;
        TextView status;

        MaterialButton viewButton;


        ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);


            stationName =
                    itemView.findViewById(
                            R.id.textReservationStationName
                    );


            address =
                    itemView.findViewById(
                            R.id.textReservationAddress
                    );


            schedule =
                    itemView.findViewById(
                            R.id.textReservationSchedule
                    );


            slot =
                    itemView.findViewById(
                            R.id.textReservationSlot
                    );


            status =
                    itemView.findViewById(
                            R.id.textReservationStatus
                    );


            viewButton =
                    itemView.findViewById(
                            R.id.buttonViewReservation
                    );
        }
    }
}