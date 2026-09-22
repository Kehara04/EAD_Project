package com.smartsolar.data.remote;

import android.content.Context;

import com.smartsolar.data.local.SessionManager;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Creates the Retrofit client used by the native Android application.
 */
public final class ApiClient {

    // Development: run connect-backend.sh after connecting a phone or starting an emulator.
    // ADB reverse forwards this device-local port to the backend on the development computer.
    private static final String BASE_URL = "http://10.0.2.2:5103/api/";
    //private static final String BASE_URL = "http://127.0.0.1:5103/api/";
    //private static final String BASE_URL = "http://172.20.10.3:5103/api/";
    private ApiClient() {
    }

    public static ApiService create(Context context) {
        SessionManager sessionManager = new SessionManager(context);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request();
                    String token = sessionManager.getToken();

                    if (token != null && !token.trim().isEmpty()) {
                        request = request.newBuilder()
                                .header("Authorization", "Bearer " + token)
                                .build();
                    }

                    return chain.proceed(request);
                })
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();

        return retrofit.create(ApiService.class);
    }
}
