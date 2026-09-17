package com.smartsolar.data.remote;

import android.content.Context;

import com.smartsolar.data.local.SessionManager;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final String BASE_URL =
            "http://10.0.2.2:5103/api/";

    public static ApiService create(
            Context context) {

        SessionManager session =
                new SessionManager(context);

        HttpLoggingInterceptor logging =
                new HttpLoggingInterceptor();

        logging.setLevel(
                HttpLoggingInterceptor.Level.BODY
        );

        OkHttpClient client =
                new OkHttpClient.Builder()
                        .addInterceptor(chain -> {

                            okhttp3.Request request =
                                    chain.request();

                            String token =
                                    session.getToken();

                            if (token != null) {
                                request =
                                        request
                                                .newBuilder()
                                                .addHeader(
                                                        "Authorization",
                                                        "Bearer " + token
                                                )
                                                .build();
                            }

                            return chain.proceed(
                                    request
                            );
                        })
                        .addInterceptor(logging)
                        .build();

        Retrofit retrofit =
                new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(
                                GsonConverterFactory
                                        .create()
                        )
                        .client(client)
                        .build();

        return retrofit.create(
                ApiService.class
        );
    }
}