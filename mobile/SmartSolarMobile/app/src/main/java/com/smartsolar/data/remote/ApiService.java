// package com.smartsolar.data.remote;

// import com.smartsolar.model.LoginRequest;
// import com.smartsolar.model.LoginResponse;
// import com.smartsolar.model.Prosumer;
// import com.smartsolar.model.ProsumerActionResponse;
// import com.smartsolar.model.RegisterProsumerRequest;
// import com.smartsolar.model.UpdateProsumerRequest;
// import com.smartsolar.model.SolarStation;
// import java.util.List;



// import retrofit2.Call;
// import retrofit2.http.Body;
// import retrofit2.http.GET;
// import retrofit2.http.PATCH;
// import retrofit2.http.POST;
// import retrofit2.http.PUT;
// import retrofit2.http.Path;
// import retrofit2.http.Query;

// public interface ApiService {

//     @POST("auth/login")
//     Call<LoginResponse> login(@Body LoginRequest request);

//     @POST("prosumers/register")
//     Call<Prosumer> register(@Body RegisterProsumerRequest request);

//     @GET("prosumers/me")
//     Call<Prosumer> getProfile();

//     @PUT("prosumers/me")
//     Call<Prosumer> updateProfile(@Body UpdateProsumerRequest request);

//     @PATCH("prosumers/me/request-deactivation")
//     Call<ProsumerActionResponse> requestDeactivation();

//     @GET("stations")
//     Call<List<SolarStation>> getStations(@Query("status") String status);

//     @GET("stations/{id}")
//     Call<SolarStation> getStation(
//         @Path("id") String id
// );

//     @GET("stations/nearby")
//     Call<List<SolarStation>> getNearbyStations(
//         @Query("latitude") double latitude,
//         @Query("longitude") double longitude,
//         @Query("radiusKm") double radiusKm
// );
// }


package com.smartsolar.data.remote;

import com.smartsolar.model.AvailableSlot;
import com.smartsolar.model.CreateReservationRequest;
import com.smartsolar.model.EnergyReservation;
import com.smartsolar.model.LoginRequest;
import com.smartsolar.model.LoginResponse;
import com.smartsolar.model.Prosumer;
import com.smartsolar.model.ProsumerActionResponse;
import com.smartsolar.model.RegisterProsumerRequest;
import com.smartsolar.model.SolarStation;
import com.smartsolar.model.UpdateProsumerRequest;
import com.smartsolar.model.UpdateReservationRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    /* =========================================
       AUTHENTICATION
    ========================================= */

    @POST("auth/login")
    Call<LoginResponse> login(
            @Body LoginRequest request
    );


    /* =========================================
       PROSUMER
    ========================================= */

    @POST("prosumers/register")
    Call<Prosumer> register(
            @Body RegisterProsumerRequest request
    );

    @GET("prosumers/me")
    Call<Prosumer> getProfile();

    @PUT("prosumers/me")
    Call<Prosumer> updateProfile(
            @Body UpdateProsumerRequest request
    );

    @PATCH("prosumers/me/request-deactivation")
    Call<ProsumerActionResponse>
    requestDeactivation();


    /* =========================================
       STATIONS
    ========================================= */

    @GET("stations")
    Call<List<SolarStation>> getStations(
            @Query("status") String status
    );

    @GET("stations/{id}")
    Call<SolarStation> getStation(
            @Path("id") String id
    );

    @GET("stations/nearby")
    Call<List<SolarStation>> getNearbyStations(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radiusKm") double radiusKm
    );


    /* =========================================
       RESERVATIONS
    ========================================= */

    @GET("reservations/available-slots")
    Call<List<AvailableSlot>> getAvailableSlots(
            @Query("stationId") String stationId,
            @Query("scheduledAt") String scheduledAt
    );


    @POST("reservations")
    Call<EnergyReservation> createReservation(
            @Body CreateReservationRequest request
    );


    @GET("reservations/my")
    Call<List<EnergyReservation>> getMyReservations(
            @Query("status") String status,
            @Query("search") String search
    );


    @GET("reservations/{id}")
    Call<EnergyReservation> getReservation(
            @Path("id") String id
    );


    @PUT("reservations/{id}")
    Call<EnergyReservation> updateReservation(
            @Path("id") String id,
            @Body UpdateReservationRequest request
    );


    @PATCH("reservations/{id}/cancel")
    Call<EnergyReservation> cancelReservation(
            @Path("id") String id
    );
}