package com.smartsolar.data.remote;

import com.smartsolar.model.LoginRequest;
import com.smartsolar.model.LoginResponse;
import com.smartsolar.model.Prosumer;
import com.smartsolar.model.RegisterProsumerRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;

public interface ApiService {

    @POST("auth/login")
    Call<LoginResponse> login(
            @Body LoginRequest request
    );

    @POST("prosumers/register")
    Call<Prosumer> register(
            @Body RegisterProsumerRequest request
    );

    @GET("prosumers/me")
    Call<Prosumer> getProfile();

    @PATCH("prosumers/me/request-deactivation")
    Call<Object> requestDeactivation();
}