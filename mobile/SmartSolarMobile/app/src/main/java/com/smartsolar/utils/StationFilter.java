package com.smartsolar.utils;

import com.smartsolar.model.SolarStation;
import java.util.Locale;

public final class StationFilter {
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
