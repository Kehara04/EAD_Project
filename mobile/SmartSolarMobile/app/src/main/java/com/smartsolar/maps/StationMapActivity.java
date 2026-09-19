package com.smartsolar.maps;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.smartsolar.R;
import com.smartsolar.data.local.SessionManager;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.model.SolarStation;
import com.smartsolar.ui.auth.LoginActivity;

import java.io.File;
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
        loadStations();
    }

    private void loadStations() {
        if (map == null) return;
        status.setText(R.string.stations_loading);
        retryButton.setVisibility(View.GONE);
        stationsCall = ApiClient.create(this).getStations();
        stationsCall.enqueue(new Callback<List<SolarStation>>() {
            @Override
            public void onResponse(Call<List<SolarStation>> call, Response<List<SolarStation>> response) {
                if (isFinishing() || isDestroyed()) return;
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
                if (!call.isCanceled() && !isFinishing() && !isDestroyed()) showError();
            }
        });
    }

    private void showStations(List<SolarStation> stations) {
        map.getOverlays().clear();
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
            map.getOverlays().add(marker);
            positions.add(position);
            count++;
        }
        map.invalidate();
        if (count == 0) {
            status.setText(stations.isEmpty()
                    ? R.string.stations_empty : R.string.stations_coordinates_unsupported);
            retryButton.setVisibility(View.VISIBLE);
            return;
        }
        status.setText(getString(R.string.stations_count, count));
        if (count < stations.size()) {
            status.append(" " + getString(R.string.stations_skipped, stations.size() - count));
        }
        BoundingBox stationBounds = BoundingBox.fromGeoPoints(positions);
        View mapView = findViewById(R.id.stationMap);
        mapView.post(() -> {
            if (isFinishing() || isDestroyed()) return;
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

    private void showError() {
        status.setText(R.string.stations_error);
        retryButton.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (map != null) map.onResume();
    }

    @Override
    protected void onPause() {
        if (map != null) map.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (stationsCall != null) stationsCall.cancel();
        if (map != null) map.onDetach();
        super.onDestroy();
    }
}
