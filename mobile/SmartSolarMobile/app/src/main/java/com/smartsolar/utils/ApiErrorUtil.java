package com.smartsolar.utils;

import org.json.JSONObject;

import retrofit2.Response;

/**
 * Utility class for extracting user-friendly error messages
 * from backend API responses.
 * Returns a fallback message when the response does not contain
 * a valid error message or cannot be parsed.
 */
public final class ApiErrorUtil {

    // Prevents instantiation of this utility class.
    private ApiErrorUtil() {
    }

    // Extracts the message field from an API error response or returns the provided fallback.
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
