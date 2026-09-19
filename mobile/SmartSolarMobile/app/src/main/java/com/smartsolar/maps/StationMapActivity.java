package com.smartsolar.maps;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.smartsolar.R;
import com.smartsolar.data.local.SessionManager;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.model.SolarStation;
import com.smartsolar.ui.auth.LoginActivity;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StationMapActivity extends AppCompatActivity {
    // OSM raster tiles use Web Mercator, which cannot display the poles.
    private static final double MAX_MAP_LATITUDE = 85.05112878;
    private MapView map;
    private TextView status;
    private View retryButton;
    private Call<List<SolarStation>> stationsCall;
    private final List<Marker> stationMarkers = new ArrayList<>();
    private Marker userMarker;
    private LocationManager locationManager;
    private TextView locationStatus;
    private boolean locating;
    private boolean centerOnLocation = true;
    private boolean hasCenteredLocation;
    private Location currentLocation;
    private Location searchOrigin;
    private int radiusKm;
    private boolean waitingForNearbyLocation;
    private BottomSheetDialog stationDetails;
    private final Handler locationHandler = new Handler(Looper.getMainLooper());
    private final Runnable locationTimeout = () -> {
        if (locating && userMarker == null) locationStatus.setText(R.string.location_waiting);
    };
    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) { showUserLocation(location); }

        @Override
        public void onProviderDisabled(String provider) {
            if (!locationServicesEnabled()) {
                removeUserMarker();
                locationStatus.setText(R.string.location_disabled);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            locationStatus.setText(R.string.location_locating);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
    };
    private final ActivityResultLauncher<String[]> locationPermissionRequest =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (hasLocationPermission()) startLocationUpdates();
                else locationStatus.setText(R.string.location_denied);
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Identify tile requests and cache in app-private storage; no storage permission needed.
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(new File(getFilesDir(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(getCacheDir(), "osm-tiles"));
        Configuration.getInstance().setExpirationExtendedDuration(7L * 24 * 60 * 60 * 1000);
        setContentView(R.layout.activity_station_map);
        WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView())
                .setAppearanceLightStatusBars(true);

        MaterialToolbar toolbar = findViewById(R.id.stationToolbar);
        toolbar.setTitle(R.string.solar_stations);
        toolbar.setNavigationIcon(androidx.appcompat.R.drawable.abc_ic_ab_back_material);
        toolbar.setNavigationContentDescription(androidx.appcompat.R.string.abc_action_bar_up_description);
        toolbar.setNavigationOnClickListener(v -> finish());
        status = findViewById(R.id.stationStatus);
        retryButton = findViewById(R.id.buttonRetryStations);
        retryButton.setOnClickListener(v -> loadStations());

        TextView attribution = findViewById(R.id.mapAttribution);
        attribution.setMovementMethod(LinkMovementMethod.getInstance());
        map = findViewById(R.id.stationMap);
        map.setTileSource(new XYTileSource("OpenStreetMap", 0, 19, 256, ".png",
                new String[]{"https://tile.openstreetmap.org/"}, "© OpenStreetMap contributors"));
        map.setMultiTouchControls(true);
        map.setVerticalMapRepetitionEnabled(false);
        map.setMaxZoomLevel(19.0);
        map.getController().setZoom(7.0);
        map.getController().setCenter(new GeoPoint(7.8731, 80.7718));
        locationStatus = findViewById(R.id.locationStatus);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        findViewById(R.id.buttonMyLocation).setOnClickListener(v -> requestUserLocation());
        MaterialButtonToggleGroup radiusOptions = findViewById(R.id.stationRadiusOptions);
        radiusKm = savedInstanceState == null ? 0 : savedInstanceState.getInt("radiusKm", 0);
        radiusOptions.check(radiusKm == 5 ? R.id.radius5 : radiusKm == 10 ? R.id.radius10 : R.id.radiusAll);
        radiusOptions.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            radiusKm = checkedId == R.id.radius5 ? 5 : checkedId == R.id.radius10 ? 10 : 0;
            if (radiusKm == 0) hasCenteredLocation = false;
            searchOrigin = null;
            loadStations();
            if (waitingForNearbyLocation) requestUserLocation();
        });
        findViewById(R.id.buttonRefreshStations).setOnClickListener(v -> {
            loadStations();
            if (waitingForNearbyLocation) requestUserLocation();
        });
        loadStations();
        if (!hasLocationPermission() && !getPreferences(MODE_PRIVATE).getBoolean("locationAsked", false)) {
            askLocationPermission();
        } else if (!hasLocationPermission()) {
            locationStatus.setText(R.string.location_denied);
        }
    }

    private boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void askLocationPermission() {
        getPreferences(MODE_PRIVATE).edit().putBoolean("locationAsked", true).apply();
        locationPermissionRequest.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION});
    }

    private void requestUserLocation() {
        centerOnLocation = true;
        if (!hasLocationPermission()) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)
                    || !getPreferences(MODE_PRIVATE).getBoolean("locationAsked", false)) {
                new AlertDialog.Builder(this).setMessage(R.string.location_rationale)
                        .setPositiveButton(R.string.location_continue, (dialog, which) -> askLocationPermission())
                        .setNegativeButton(android.R.string.cancel, null).show();
            } else {
                new AlertDialog.Builder(this).setMessage(R.string.location_settings_message)
                        .setPositiveButton(R.string.location_settings, (dialog, which) ->
                                startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:" + getPackageName()))))
                        .setNegativeButton(android.R.string.cancel, null).show();
            }
        } else if (!locationServicesEnabled()) {
            locationStatus.setText(R.string.location_disabled);
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        } else {
            stopLocationUpdates();
            startLocationUpdates();
        }
    }

    private boolean locationServicesEnabled() {
        return locationManager != null && (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    private void startLocationUpdates() {
        if (!hasLocationPermission() || locating || locationManager == null) return;
        boolean precise = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        locationStatus.setText(locationServicesEnabled()
                ? R.string.location_locating : R.string.location_disabled);
        try {
            Location recent = null;
            for (String provider : locationManager.getAllProviders()) {
                if (!LocationManager.NETWORK_PROVIDER.equals(provider)
                        && !(precise && LocationManager.GPS_PROVIDER.equals(provider))) continue;
                locationManager.requestLocationUpdates(provider, 5000L, 5f, locationListener, Looper.getMainLooper());
                locating = true;
                Location cached = locationManager.getLastKnownLocation(provider);
                if (cached != null && (recent == null || cached.getElapsedRealtimeNanos() > recent.getElapsedRealtimeNanos())) {
                    recent = cached;
                }
            }
            if (locationServicesEnabled() && recent != null
                    && SystemClock.elapsedRealtimeNanos() - recent.getElapsedRealtimeNanos() < 120_000_000_000L) {
                showUserLocation(recent);
            }
            if (locationServicesEnabled()) locationHandler.postDelayed(locationTimeout, 20000);
        } catch (SecurityException e) {
            stopLocationUpdates();
            removeUserMarker();
            locationStatus.setText(R.string.location_denied);
        }
    }

    private void showUserLocation(Location location) {
        if (!locating || isFinishing() || isDestroyed()) return;
        if (Math.abs(location.getLatitude()) > MAX_MAP_LATITUDE) {
            removeUserMarker();
            locationStatus.setText(R.string.location_unsupported);
            return;
        }
        locationHandler.removeCallbacks(locationTimeout);
        GeoPoint position = new GeoPoint(location.getLatitude(), location.getLongitude());
        if (userMarker == null) {
            userMarker = new Marker(map);
            userMarker.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_my_location));
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
            userMarker.setTitle(getString(R.string.location_you));
            map.getOverlays().add(userMarker);
        }
        userMarker.setPosition(position);
        currentLocation = new Location(location);
        boolean precise = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        locationStatus.setText(precise ? R.string.location_shown : R.string.location_approximate);
        userMarker.setSnippet(getString(precise ? R.string.location_shown : R.string.location_approximate));
        if (centerOnLocation) {
            map.getController().setZoom(precise ? 15.0 : 12.0);
            map.getController().setCenter(position);
            centerOnLocation = false;
            hasCenteredLocation = true;
        }
        map.invalidate();
        if (radiusKm > 0 && (waitingForNearbyLocation
                || searchOrigin == null || searchOrigin.distanceTo(currentLocation) >= 250)) {
            loadStations();
        }
    }

    private void removeUserMarker() {
        currentLocation = null;
        searchOrigin = null;
        if (radiusKm > 0) {
            if (stationsCall != null) stationsCall.cancel();
            stationsCall = null;
            clearStationMarkers();
            waitingForNearbyLocation = true;
            status.setText(R.string.nearby_location_required);
        }
        if (userMarker != null) {
            userMarker.closeInfoWindow();
            map.getOverlays().remove(userMarker);
            userMarker = null;
            map.invalidate();
        }
    }

    private void stopLocationUpdates() {
        locationHandler.removeCallbacks(locationTimeout);
        if (locationManager != null) locationManager.removeUpdates(locationListener);
        locating = false;
    }

    private void loadStations() {
        if (map == null) return;
        if (stationsCall != null) stationsCall.cancel();
        stationsCall = null;
        clearStationMarkers();
        waitingForNearbyLocation = radiusKm > 0 && !hasFreshLocation();
        retryButton.setVisibility(View.GONE);
        if (waitingForNearbyLocation) {
            status.setText(R.string.nearby_location_required);
            return;
        }
        status.setText(radiusKm > 0 ? getString(R.string.nearby_loading, radiusKm)
                : getString(R.string.stations_loading));
        if (radiusKm > 0) {
            searchOrigin = new Location(currentLocation);
            stationsCall = ApiClient.create(this).getNearbyStations(
                    searchOrigin.getLatitude(), searchOrigin.getLongitude(), radiusKm);
        } else {
            stationsCall = ApiClient.create(this).getStations("Active");
        }
        stationsCall.enqueue(new Callback<List<SolarStation>>() {
            @Override
            public void onResponse(Call<List<SolarStation>> call, Response<List<SolarStation>> response) {
                if (call != stationsCall || call.isCanceled() || isFinishing() || isDestroyed()) return;
                if (response.code() == 401) {
                    new SessionManager(StationMapActivity.this).logout();
                    Toast.makeText(StationMapActivity.this, R.string.stations_session_expired,
                            Toast.LENGTH_LONG).show();
                    startActivity(new Intent(StationMapActivity.this, LoginActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    finish();
                } else if (response.isSuccessful() && response.body() != null) {
                    showStations(response.body());
                } else {
                    showError();
                }
            }

            @Override
            public void onFailure(Call<List<SolarStation>> call, Throwable error) {
                if (call == stationsCall && !call.isCanceled() && !isFinishing() && !isDestroyed()) showError();
            }
        });
    }

    private void clearStationMarkers() {
        if (stationDetails != null) stationDetails.dismiss();
        for (Marker marker : stationMarkers) {
            marker.closeInfoWindow();
            map.getOverlays().remove(marker);
        }
        stationMarkers.clear();
        map.invalidate();
    }

    private void showStations(List<SolarStation> stations) {
        clearStationMarkers();
        List<GeoPoint> positions = new ArrayList<>();
        int count = 0;
        for (SolarStation station : stations) {
            if (station == null) continue;
            double latitude = station.getLatitude();
            double longitude = station.getLongitude();
            if (!Double.isFinite(latitude) || !Double.isFinite(longitude)
                    || Math.abs(latitude) > MAX_MAP_LATITUDE || Math.abs(longitude) > 180) continue;
            GeoPoint position = new GeoPoint(latitude, longitude);
            Marker marker = new Marker(map);
            marker.setPosition(position);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setTitle(station.getName());
            marker.setSnippet(getString(R.string.station_details, station.getAddress(),
                    station.getAvailableSlots(), station.getStatus()));
            marker.setOnMarkerClickListener((selectedMarker, mapView) -> {
                showStationDetails(station);
                return true;
            });
            map.getOverlays().add(marker);
            stationMarkers.add(marker);
            positions.add(position);
            count++;
        }
        map.invalidate();
        if (count == 0) {
            status.setText(stations.isEmpty()
                    ? radiusKm > 0 ? getString(R.string.nearby_empty, radiusKm) : getString(R.string.stations_empty)
                    : getString(R.string.stations_coordinates_unsupported));
            retryButton.setVisibility(View.VISIBLE);
            if (radiusKm > 0 && currentLocation != null) {
                map.getController().setCenter(new GeoPoint(currentLocation));
                map.getController().setZoom(radiusKm == 5 ? 13.0 : 12.0);
            }
            return;
        }
        status.setText(radiusKm > 0 ? getString(R.string.nearby_count, count, radiusKm)
                : getString(R.string.stations_count, count));
        if (count < stations.size()) {
            status.append(" " + getString(R.string.stations_skipped, stations.size() - count));
        }
        if (radiusKm > 0 && searchOrigin != null) positions.add(new GeoPoint(searchOrigin));
        BoundingBox stationBounds = BoundingBox.fromGeoPoints(positions);
        View mapView = findViewById(R.id.stationMap);
        mapView.post(() -> {
            if (isFinishing() || isDestroyed() || (radiusKm == 0 && hasCenteredLocation)) return;
            if (stationBounds.getLatitudeSpan() == 0 && stationBounds.getLongitudeSpan() == 0) {
                map.getController().setZoom(14.0);
                map.getController().setCenter(positions.get(0));
            } else if (mapView.getWidth() > 0 && mapView.getHeight() > 0) {
                int padding = Math.min((int) (48 * getResources().getDisplayMetrics().density),
                        Math.min(mapView.getWidth(), mapView.getHeight()) / 4);
                map.zoomToBoundingBox(stationBounds, false, padding, 19.0, null);
            }
        });
    }

    private boolean hasFreshLocation() {
        return currentLocation != null && hasLocationPermission() && locationServicesEnabled()
                && SystemClock.elapsedRealtimeNanos() - currentLocation.getElapsedRealtimeNanos()
                < 120_000_000_000L;
    }

    private void showStationDetails(SolarStation station) {
        if (stationDetails != null) stationDetails.dismiss();
        stationDetails = new BottomSheetDialog(this);
        View content = getLayoutInflater().inflate(R.layout.sheet_station_details, null);
        ((TextView) content.findViewById(R.id.stationDetailName)).setText(station.getName());
        ((TextView) content.findViewById(R.id.stationDetailAddress)).setText(station.getAddress());
        NumberFormat number = NumberFormat.getNumberInstance();
        number.setMaximumFractionDigits(1);
        ((TextView) content.findViewById(R.id.stationDetailCapacity)).setText(
                getString(R.string.station_capacity, number.format(station.getCapacityKw())));
        ((TextView) content.findViewById(R.id.stationDetailSlots)).setText(
                getResources().getQuantityString(R.plurals.station_slots,
                        station.getAvailableSlots(), station.getAvailableSlots()));
        TextView distance = content.findViewById(R.id.stationDetailDistance);
        if (hasFreshLocation()) {
            float[] meters = new float[1];
            Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                    station.getLatitude(), station.getLongitude(), meters);
            distance.setText(getString(R.string.station_distance, meters[0] / 1000.0));
        } else {
            distance.setText(R.string.station_distance_unavailable);
        }
        content.findViewById(R.id.buttonCloseStation).setOnClickListener(v -> stationDetails.dismiss());
        stationDetails.setContentView(content);
        stationDetails.show();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("radiusKm", radiusKm);
        super.onSaveInstanceState(outState);
    }

    private void showError() {
        status.setText(R.string.stations_error);
        retryButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
        if (locationStatus != null) {
            if (hasLocationPermission()) startLocationUpdates();
            else {
                removeUserMarker();
                locationStatus.setText(R.string.location_denied);
            }
        }
    }

    @Override
    protected void onPause() {
        stopLocationUpdates();
        if (map != null) map.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (stationsCall != null) stationsCall.cancel();
        if (stationDetails != null) stationDetails.dismiss();
        stopLocationUpdates();
        if (map != null) map.onDetach();
        super.onDestroy();
    }
}
