package com.smartsolar.utils;

import org.json.JSONObject;

import retrofit2.Response;

/**
 * Extracts a user-friendly message from API error responses.
 */
public final class ApiErrorUtil {

    private ApiErrorUtil() {
    }

    public static String getMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() == null) {
                return fallback;
            }

            String body = response.errorBody().string();
            JSONObject jsonObject = new JSONObject(body);
            String message = jsonObject.optString("message");

            return message == null || message.trim().isEmpty()
                    ? fallback
                    : message;
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
