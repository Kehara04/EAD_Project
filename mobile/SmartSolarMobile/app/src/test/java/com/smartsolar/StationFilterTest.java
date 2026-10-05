package com.smartsolar;

import com.google.gson.Gson;
import com.smartsolar.model.SolarStation;
import com.smartsolar.utils.StationFilter;
import org.junit.Test;
import static org.junit.Assert.*;

public class StationFilterTest {
    // Deserialize realistic API field names so filtering tests use the mobile station model.
    private SolarStation station(int slots) {
        return new Gson().fromJson("{\"name\":\"Negombo Solar Hub\","
                + "\"address\":\"25 Beach Road, Negombo\",\"availableSlots\":" + slots + "}", SolarStation.class);
    }

    @Test public void searchesNameAndAddressIgnoringCaseAndSurroundingWhitespace() {
        assertTrue(StationFilter.matches(station(4), " SOLAR hub ", false));
        assertTrue(StationFilter.matches(station(4), "beach ROAD", false));
        assertFalse(StationFilter.matches(station(4), "Kandy", false));
    }

    // Search and availability must both match when the free-slot filter is enabled.
    @Test public void availabilityCanBeCombinedWithSearch() {
        assertTrue(StationFilter.matches(station(4), "Negombo", true));
        assertFalse(StationFilter.matches(station(0), "Negombo", true));
        assertTrue(StationFilter.matches(station(0), "Negombo", false));
        assertFalse(StationFilter.matches(station(-1), "", true));
    }

    @Test public void clearingSearchRestoresResultsAndMissingFieldsAreSafe() {
        assertTrue(StationFilter.matches(station(4), "   ", false));
        assertTrue(StationFilter.matches(station(4), null, false));
        assertFalse(StationFilter.matches(new SolarStation(), "solar", false));
        assertFalse(StationFilter.matches(null, "", false));
    }
}
