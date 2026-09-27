package com.smartsolar.data.remote;

import com.smartsolar.model.AvailableSlot;
import com.smartsolar.model.CompleteTransferRequest;
import com.smartsolar.model.CreateReservationRequest;
import com.smartsolar.model.EnergyReservation;
import com.smartsolar.model.LoginRequest;
import com.smartsolar.model.LoginResponse;
import com.smartsolar.model.OperatorDashboardStats;
import com.smartsolar.model.Prosumer;
import com.smartsolar.model.ProsumerActionResponse;
import com.smartsolar.model.QrPayload;
import com.smartsolar.model.RegisterProsumerRequest;
import com.smartsolar.model.SolarStation;
import com.smartsolar.model.UpdateProsumerRequest;
import com.smartsolar.model.UpdateReservationRequest;
import com.smartsolar.model.VerifyQrResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

/*Defines the Retrofit API endpoints used for authentication, prosumer management,
solar stations, energy reservations, and grid operator operations.*/
public interface ApiService {

    // Authenticates a user and retrieves their login details and access token.
    @POST("auth/login")
    Call<LoginResponse> login(
            @Body LoginRequest request
    );

    // Registers a new prosumer account.
    @POST("prosumers/register")
    Call<Prosumer> register(
            @Body RegisterProsumerRequest request
    );

    // Retrieves the authenticated prosumer's profile.
    @GET("prosumers/me")
    Call<Prosumer> getProfile();

    // Updates the authenticated prosumer's profile information.
    @PUT("prosumers/me")
    Call<Prosumer> updateProfile(
            @Body UpdateProsumerRequest request
    );

    // Submits a request to deactivate the authenticated prosumer's account.
    @PATCH("prosumers/me/request-deactivation")
    Call<ProsumerActionResponse>
    requestDeactivation();

    // Retrieves solar stations filtered by their status.
    @GET("stations")
    Call<List<SolarStation>> getStations(
            @Query("status") String status
    );

    // Refresh the selected station before showing its full details.
    @GET("stations/{id}")
    Call<SolarStation> getStation(
            @Path("id") String id
    );

    // Retrieves nearby solar stations based on location and search radius.
    @GET("stations/nearby")
    Call<List<SolarStation>> getNearbyStations(
            @Query("latitude") double latitude,
            @Query("longitude") double longitude,
            @Query("radiusKm") double radiusKm
    );

    // Retrieves available energy reservation slots for a station and scheduled time.
    @GET("reservations/available-slots")
    Call<List<AvailableSlot>> getAvailableSlots(
            @Query("stationId") String stationId,
            @Query("scheduledAt") String scheduledAt
    );

    // Creates a new energy reservation using the provided reservation details.
    @POST("reservations")
    Call<EnergyReservation> createReservation(
            @Body CreateReservationRequest request
    );

    // Retrieves the authenticated user's reservations with optional filtering.
    @GET("reservations/my")
    Call<List<EnergyReservation>> getMyReservations(
            @Query("status") String status,
            @Query("search") String search
    );

    // Retrieves the details of a specific energy reservation.
    @GET("reservations/{id}")
    Call<EnergyReservation> getReservation(
            @Path("id") String id
    );

    // Updates an existing energy reservation.
    @PUT("reservations/{id}")
    Call<EnergyReservation> updateReservation(
            @Path("id") String id,
            @Body UpdateReservationRequest request
    );

    // Cancels an existing energy reservation.
    @PATCH("reservations/{id}/cancel")
    Call<EnergyReservation> cancelReservation(
            @Path("id") String id
    );

    // Retrieves the signed QR payload for an approved reservation.
    @GET("operator/reservations/{id}/qr")
    Call<QrPayload> getQrPayload(
            @Path("id") String reservationId
    );

    // Sends a scanned QR payload to the backend for verification.
    @POST("operator/verify")
    Call<VerifyQrResponse> verifyQr(
            @Body QrPayload payload
    );

    // Completes the energy transfer after successful reservation verification.
    @POST("operator/complete")
    Call<EnergyReservation> completeTransfer(
            @Body CompleteTransferRequest request
    );

    // Retrieves reservation statistics and station completion totals for the operator dashboard.
    @GET("operator/dashboard")
    Call<OperatorDashboardStats> getOperatorDashboard();

    // Sends a password recovery request for the provided email address.
    @POST("auth/forgot-password")
    Call<okhttp3.ResponseBody> forgotPassword(
        @Body java.util.Map<String, String> request
    );

    // Changes the authenticated user's password using the provided credentials.
    @POST("account/change-password")
    Call<okhttp3.ResponseBody> changePassword(
        @Body java.util.Map<String, String> request
    );
}