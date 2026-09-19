package com.smartsolar.utils;

import com.smartsolar.model.SolarStation;
import java.util.Locale;

public final class StationFilter {
    private StationFilter() { }

    public static boolean matches(SolarStation station, String query, boolean availableOnly) {
        if (station == null || (availableOnly && station.getAvailableSlots() <= 0)) return false;
        String search = normalize(query);
        return search.isEmpty() || normalize(station.getName()).contains(search)
                || normalize(station.getAddress()).contains(search);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
