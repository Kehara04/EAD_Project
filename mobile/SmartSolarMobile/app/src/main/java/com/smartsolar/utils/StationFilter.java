package com.smartsolar.utils;

import com.smartsolar.model.SolarStation;
import java.util.Locale;

/**
 * Utility class for filtering solar stations based on availability
 * and search text without modifying the original station list.
 */
public final class StationFilter {

    // Prevents instantiation of this utility class.
    private StationFilter() { }

    // Apply availability first, then search station name and address without changing the loaded list.
    public static boolean matches(SolarStation station, String query, boolean availableOnly) {
        if (station == null || (availableOnly && station.getAvailableSlots() <= 0)) return false;
        String search = normalize(query);
        return search.isEmpty() || normalize(station.getName()).contains(search)
                || normalize(station.getAddress()).contains(search);
    }

    // Use locale-independent case folding and tolerate missing optional text fields.
    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
