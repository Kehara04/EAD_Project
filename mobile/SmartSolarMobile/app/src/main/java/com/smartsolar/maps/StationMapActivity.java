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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsolar.R;
import com.smartsolar.BuildConfig;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.smartsolar.data.local.SessionManager;
import com.smartsolar.data.remote.ApiClient;
import com.smartsolar.model.SolarStation;
import com.smartsolar.ui.auth.LoginActivity;
import com.smartsolar.ui.reservation.ReservationActivity;
import com.smartsolar.utils.StationFilter;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StationMapActivity extends AppCompatActivity {

    // OSM raster tiles use Web Mercator,
    // which cannot display the poles.
    private static final double MAX_MAP_LATITUDE =
            85.05112878;

    private MapView mapView;
    private GoogleMap map;
    private boolean mapInitialized;
    private boolean mapLoaded;
    private TextView mapStatus;

    private TextView status;

    private View retryButton;

    private Call<List<SolarStation>>
            stationsCall;

    private final List<Marker>
            stationMarkers =
            new ArrayList<>();

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

    private Call<SolarStation> detailCall;

    private final List<SolarStation>
            loadedStations =
            new ArrayList<>();

    private final List<SolarStation>
            filteredStations =
            new ArrayList<>();

    private TextInputEditText stationSearch;

    private MaterialSwitch availableOnly;

    private MaterialButton stationListButton;

    private boolean stationDataLoaded;

    private AlertDialog stationListDialog;

    private final Handler locationHandler =
            new Handler(
                    Looper.getMainLooper()
            );


    private final Runnable locationTimeout =
            () -> {

                if (
                        locating &&
                        userMarker == null
                ) {
                    locationStatus.setText(
                            R.string.location_waiting
                    );
                }
            };


    private final LocationListener
            locationListener =
            new LocationListener() {

                @Override
                public void onLocationChanged(
                        Location location
                ) {
                    showUserLocation(
                            location
                    );
                }


                @Override
                public void onProviderDisabled(
                        String provider
                ) {
                    if (
                            !locationServicesEnabled()
                    ) {
                        removeUserMarker();

                        locationStatus.setText(
                                R.string.location_disabled
                        );
                    }
                }


                @Override
                public void onProviderEnabled(
                        String provider
                ) {
                    locationStatus.setText(
                            R.string.location_locating
                    );
                }


                @Override
                public void onStatusChanged(
                        String provider,
                        int status,
                        Bundle extras
                ) {
                    // Deprecated callback retained
                    // for compatibility.
                }
            };


    private final ActivityResultLauncher<String[]>
            locationPermissionRequest =
            registerForActivityResult(
                    new ActivityResultContracts
                            .RequestMultiplePermissions(),

                    result -> {

                        if (
                                hasLocationPermission()
                        ) {
                            startLocationUpdates();
                        } else {
                            locationStatus.setText(
                                    R.string.location_denied
                            );
                        }
                    }
            );


    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(
                savedInstanceState
        );


        setContentView(
                R.layout.activity_station_map
        );


        WindowCompat
                .getInsetsController(
                        getWindow(),
                        getWindow()
                                .getDecorView()
                )
                .setAppearanceLightStatusBars(
                        true
                );


        MaterialToolbar toolbar =
                findViewById(
                        R.id.stationToolbar
                );


        toolbar.setTitle(
                R.string.solar_stations
        );


        toolbar.setNavigationIcon(
                androidx.appcompat.R.drawable
                        .abc_ic_ab_back_material
        );


        toolbar.setNavigationContentDescription(
                androidx.appcompat.R.string
                        .abc_action_bar_up_description
        );


        toolbar.setNavigationOnClickListener(
                v -> finish()
        );


        status =
                findViewById(
                        R.id.stationStatus
                );


        retryButton =
                findViewById(
                        R.id.buttonRetryStations
                );


        retryButton.setOnClickListener(
                v -> loadStations()
        );


        mapView = findViewById(R.id.stationMap);
        mapStatus = findViewById(R.id.mapStatus);

        locationStatus =
                findViewById(
                        R.id.locationStatus
                );


        locationManager =
                (LocationManager)
                        getSystemService(
                                LOCATION_SERVICE
                        );


        findViewById(
                R.id.buttonMyLocation
        ).setOnClickListener(
                v -> requestUserLocation()
        );


        stationSearch =
                findViewById(
                        R.id.stationSearch
                );


        availableOnly =
                findViewById(
                        R.id.availableStationsOnly
                );


        stationListButton =
                findViewById(
                        R.id.buttonStationList
                );


        if (
                savedInstanceState != null
        ) {
            stationSearch.setText(
                    savedInstanceState
                            .getString(
                                    "stationQuery",
                                    ""
                            )
            );


            availableOnly.setChecked(
                    savedInstanceState
                            .getBoolean(
                                    "availableOnly",
                                    false
                            )
            );
        }


        stationSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {
                    }


                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        applyStationFilters();
                    }


                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );


        availableOnly
                .setOnCheckedChangeListener(
                        (button, checked) ->
                                applyStationFilters()
                );


        stationListButton
                .setOnClickListener(
                        v -> showStationList()
                );


        MaterialButtonToggleGroup
                radiusOptions =
                findViewById(
                        R.id.stationRadiusOptions
                );


        radiusKm =
                savedInstanceState == null
                        ? 0
                        : savedInstanceState
                        .getInt(
                                "radiusKm",
                                0
                        );


        radiusOptions.check(
                radiusKm == 5
                        ? R.id.radius5
                        : radiusKm == 10
                        ? R.id.radius10
                        : R.id.radiusAll
        );


        radiusOptions
                .addOnButtonCheckedListener(
                        (
                                group,
                                checkedId,
                                isChecked
                        ) -> {

                            if (!isChecked) {
                                return;
                            }


                            radiusKm =
                                    checkedId
                                            == R.id.radius5
                                            ? 5

                                            : checkedId
                                            == R.id.radius10
                                            ? 10

                                            : 0;


                            if (
                                    radiusKm == 0
                            ) {
                                hasCenteredLocation =
                                        false;
                            }


                            searchOrigin =
                                    null;


                            loadStations();


                            if (
                                    waitingForNearbyLocation
                            ) {
                                requestUserLocation();
                            }
                        }
                );


        findViewById(
                R.id.buttonRefreshStations
        ).setOnClickListener(
                v -> {

                    loadStations();


                    if (
                            waitingForNearbyLocation
                    ) {
                        requestUserLocation();
                    }
                }
        );


        loadStations();


        initializeGoogleMap(savedInstanceState);

        if (
                !hasLocationPermission()
                        &&
                        !getSharedPreferences("station_location_permissions", MODE_PRIVATE).getBoolean(
                                "locationAsked",
                                false
                        )
        ) {

            askLocationPermission();

        } else if (
                !hasLocationPermission()
        ) {

            locationStatus.setText(
                    R.string.location_denied
            );
        }
    }


    /* =========================================
       LOCATION PERMISSION
    ========================================= */

    // Initialize asynchronously while station search and list results load independently.
    private void initializeGoogleMap(Bundle savedState) {
        if (!BuildConfig.MAPS_KEY_CONFIGURED) {
            mapStatus.setText(R.string.google_map_key_missing);
            mapView.setVisibility(View.GONE);
            return;
        }
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) != ConnectionResult.SUCCESS) {
            mapStatus.setText(R.string.google_map_services_missing);
            mapView.setVisibility(View.GONE);
            return;
        }
        mapView.onCreate(savedState == null ? null : savedState.getBundle("googleMapState"));
        mapInitialized = true;
        mapStatus.setText(R.string.google_map_loading);
        mapView.getMapAsync(googleMap -> {
            if (isFinishing() || isDestroyed()) return;
            map = googleMap;
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setMapToolbarEnabled(false);
            map.setMaxZoomPreference(19f);
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(7.8731, 80.7718), 7f));
            map.setOnMarkerClickListener(marker -> {
                if (marker.getTag() instanceof SolarStation) {
                    showStationDetails((SolarStation) marker.getTag());
                    return true;
                }
                return false;
            });
            map.setOnMapLoadedCallback(() -> {
                mapLoaded = true;
                mapStatus.setVisibility(View.GONE);
            });
            if (currentLocation != null) showUserLocation(currentLocation);
            if (stationDataLoaded) applyStationFilters();
        });
        mapView.postDelayed(() -> {
            if (!isDestroyed() && !mapLoaded) mapStatus.setText(R.string.google_map_unavailable);
        }, 20000);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mapInitialized) mapView.onStart();
    }

    @Override
    protected void onStop() {
        if (mapInitialized) mapView.onStop();
        super.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapInitialized) mapView.onLowMemory();
    }

    private boolean hasLocationPermission() {

        return ContextCompat
                .checkSelfPermission(
                        this,
                        Manifest.permission
                                .ACCESS_FINE_LOCATION
                )
                ==
                PackageManager
                        .PERMISSION_GRANTED

                ||

                ContextCompat
                        .checkSelfPermission(
                                this,
                                Manifest.permission
                                        .ACCESS_COARSE_LOCATION
                        )
                        ==
                        PackageManager
                                .PERMISSION_GRANTED;
    }


    // Request location access only for user positioning and nearby station discovery.
    private void askLocationPermission() {

        getSharedPreferences("station_location_permissions", MODE_PRIVATE)
                .edit()
                .putBoolean(
                        "locationAsked",
                        true
                )
                .apply();


        locationPermissionRequest
                .launch(
                        new String[]{
                                Manifest.permission
                                        .ACCESS_FINE_LOCATION,

                                Manifest.permission
                                        .ACCESS_COARSE_LOCATION
                        }
                );
    }


    // Resolve a usable device location before centering the map or searching nearby.
    private void requestUserLocation() {

        centerOnLocation =
                true;


        if (
                !hasLocationPermission()
        ) {

            if (
                    shouldShowRequestPermissionRationale(
                            Manifest.permission
                                    .ACCESS_COARSE_LOCATION
                    )

                            ||

                            !getSharedPreferences("station_location_permissions", MODE_PRIVATE).getBoolean(
                                    "locationAsked",
                                    false
                            )
            ) {

                new AlertDialog.Builder(
                        this
                )
                        .setMessage(
                                R.string
                                        .location_rationale
                        )

                        .setPositiveButton(
                                R.string
                                        .location_continue,

                                (
                                        dialog,
                                        which
                                ) ->
                                        askLocationPermission()
                        )

                        .setNegativeButton(
                                android.R.string.cancel,
                                null
                        )

                        .show();

            } else {

                new AlertDialog.Builder(
                        this
                )
                        .setMessage(
                                R.string
                                        .location_settings_message
                        )

                        .setPositiveButton(
                                R.string
                                        .location_settings,

                                (
                                        dialog,
                                        which
                                ) ->
                                        startActivity(
                                                new Intent(
                                                        Settings
                                                                .ACTION_APPLICATION_DETAILS_SETTINGS,

                                                        Uri.parse(
                                                                "package:"
                                                                        +
                                                                        getPackageName()
                                                        )
                                                )
                                        )
                        )

                        .setNegativeButton(
                                android.R.string.cancel,
                                null
                        )

                        .show();
            }

        } else if (
                !locationServicesEnabled()
        ) {

            locationStatus.setText(
                    R.string.location_disabled
            );


            startActivity(
                    new Intent(
                            Settings
                                    .ACTION_LOCATION_SOURCE_SETTINGS
                    )
            );

        } else {

            stopLocationUpdates();

            startLocationUpdates();
        }
    }


    private boolean locationServicesEnabled() {

        return locationManager != null

                &&

                (
                        locationManager
                                .isProviderEnabled(
                                        LocationManager
                                                .GPS_PROVIDER
                                )

                                ||

                                locationManager
                                        .isProviderEnabled(
                                                LocationManager
                                                        .NETWORK_PROVIDER
                                        )
                );
    }


    // Listen for fresh foreground fixes to update the user marker and nearby origin.
    private void startLocationUpdates() {

        if (
                !hasLocationPermission()
                        ||
                        locating
                        ||
                        locationManager == null
        ) {
            return;
        }


        boolean precise =
                ContextCompat
                        .checkSelfPermission(
                                this,
                                Manifest.permission
                                        .ACCESS_FINE_LOCATION
                        )
                        ==
                        PackageManager
                                .PERMISSION_GRANTED;


        locationStatus.setText(
                locationServicesEnabled()
                        ? R.string
                        .location_locating

                        : R.string
                        .location_disabled
        );


        try {

            Location recent =
                    null;


            for (
                    String provider :
                    locationManager
                            .getAllProviders()
            ) {

                if (
                        !LocationManager
                                .NETWORK_PROVIDER
                                .equals(
                                        provider
                                )

                                &&

                                !(
                                        precise
                                                &&
                                                LocationManager
                                                        .GPS_PROVIDER
                                                        .equals(
                                                                provider
                                                        )
                                )
                ) {
                    continue;
                }


                locationManager
                        .requestLocationUpdates(
                                provider,
                                5000L,
                                5f,
                                locationListener,
                                Looper
                                        .getMainLooper()
                        );


                locating =
                        true;


                Location cached =
                        locationManager
                                .getLastKnownLocation(
                                        provider
                                );


                if (
                        cached != null

                                &&

                                (
                                        recent == null
                                                ||
                                                cached
                                                        .getElapsedRealtimeNanos()
                                                        >
                                                        recent
                                                                .getElapsedRealtimeNanos()
                                )
                ) {
                    recent =
                            cached;
                }
            }


            if (
                    locationServicesEnabled()
                            &&
                            recent != null

                            &&

                            SystemClock
                                    .elapsedRealtimeNanos()
                                    -
                                    recent
                                            .getElapsedRealtimeNanos()

                                    <
                                    120_000_000_000L
            ) {

                showUserLocation(
                        recent
                );
            }


            if (
                    locationServicesEnabled()
            ) {

                locationHandler
                        .postDelayed(
                                locationTimeout,
                                20000
                        );
            }


        } catch (
                SecurityException e
        ) {

            stopLocationUpdates();

            removeUserMarker();

            locationStatus.setText(
                    R.string.location_denied
            );
        }
    }


    // Display the Prosumer separately from station markers and update nearby discovery when needed.
    private void showUserLocation(Location location) {
        if (!locating || isFinishing() || isDestroyed()) return;
        if (!Double.isFinite(location.getLatitude()) || !Double.isFinite(location.getLongitude())
                || Math.abs(location.getLatitude()) > MAX_MAP_LATITUDE
                || Math.abs(location.getLongitude()) > 180) {
            removeUserMarker();
            locationStatus.setText(R.string.location_unsupported);
            return;
        }
        currentLocation = new Location(location);
        if (map == null) return; // Replay the latest fix once Google Maps is ready.
        locationHandler.removeCallbacks(locationTimeout);
        LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
        boolean precise = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        String description = getString(precise ? R.string.location_shown : R.string.location_approximate);
        if (userMarker == null) {
            userMarker = map.addMarker(new MarkerOptions().position(position)
                    .title(getString(R.string.location_you)).snippet(description)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        } else {
            userMarker.setPosition(position);
            userMarker.setSnippet(description);
        }
        locationStatus.setText(description);
        if (centerOnLocation) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(position, precise ? 15f : 12f));
            centerOnLocation = false;
            hasCenteredLocation = true;
        }
        if (radiusKm > 0 && (waitingForNearbyLocation || searchOrigin == null
                || searchOrigin.distanceTo(currentLocation) >= 250)) loadStations();
    }


    private void removeUserMarker() {
        currentLocation = null;
        searchOrigin = null;
        if (radiusKm > 0) {
            if (stationsCall != null) stationsCall.cancel();
            stationsCall = null;
            resetStationData();
            clearStationMarkers();
            waitingForNearbyLocation = true;
            status.setText(R.string.nearby_location_required);
        }
        if (userMarker != null) {
            userMarker.remove();
            userMarker = null;
        }
    }


    // Release listeners and pending timeout callbacks when location tracking stops.
    private void stopLocationUpdates() {

        locationHandler
                .removeCallbacks(
                        locationTimeout
                );


        if (
                locationManager != null
        ) {
            locationManager
                    .removeUpdates(
                            locationListener
                    );
        }


        locating =
                false;
    }


    /* =========================================
       LOAD STATIONS
    ========================================= */

    // Cancel the earlier request; nearby mode waits for a fresh location before calling the API.
    private void loadStations() {

        if (
                map == null
        ) {
            return;
        }


        if (
                stationsCall != null
        ) {
            stationsCall.cancel();
        }


        stationsCall =
                null;


        resetStationData();

        clearStationMarkers();


        waitingForNearbyLocation =
                radiusKm > 0
                        &&
                        !hasFreshLocation();


        retryButton.setVisibility(
                View.GONE
        );


        if (
                waitingForNearbyLocation
        ) {

            status.setText(
                    R.string
                            .nearby_location_required
            );

            return;
        }


        status.setText(
                radiusKm > 0

                        ? getString(
                        R.string
                                .nearby_loading,
                        radiusKm
                )

                        : getString(
                        R.string
                                .stations_loading
                )
        );


        if (
                radiusKm > 0
        ) {

            searchOrigin =
                    new Location(
                            currentLocation
                    );


            stationsCall =
                    ApiClient
                            .create(
                                    this
                            )
                            .getNearbyStations(
                                    searchOrigin
                                            .getLatitude(),

                                    searchOrigin
                                            .getLongitude(),

                                    radiusKm
                            );

        } else {

            stationsCall =
                    ApiClient
                            .create(
                                    this
                            )
                            .getStations(
                                    "Active"
                            );
        }


        stationsCall.enqueue(
                new Callback<List<SolarStation>>() {

                    @Override
                    public void onResponse(
                            Call<List<SolarStation>> call,
                            Response<List<SolarStation>> response
                    ) {

                        if (
                                call != stationsCall
                                        ||
                                        call.isCanceled()
                                        ||
                                        isFinishing()
                                        ||
                                        isDestroyed()
                        ) {
                            return;
                        }


                        if (
                                response.code()
                                        ==
                                        401
                        ) {

                            new SessionManager(
                                    StationMapActivity.this
                            )
                                    .logout();


                            Toast.makeText(
                                    StationMapActivity.this,

                                    R.string
                                            .stations_session_expired,

                                    Toast.LENGTH_LONG
                            ).show();


                            startActivity(
                                    new Intent(
                                            StationMapActivity.this,
                                            LoginActivity.class
                                    )
                                            .addFlags(
                                                    Intent
                                                            .FLAG_ACTIVITY_NEW_TASK
                                                            |
                                                            Intent
                                                                    .FLAG_ACTIVITY_CLEAR_TASK
                                            )
                            );


                            finish();

                        } else if (
                                response
                                        .isSuccessful()
                                        &&
                                        response.body()
                                                != null
                        ) {

                            loadedStations
                                    .addAll(
                                            response.body()
                                    );


                            stationDataLoaded =
                                    true;


                            applyStationFilters();

                        } else {

                            showError();
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<List<SolarStation>> call,
                            Throwable error
                    ) {

                        if (
                                call == stationsCall
                                        &&
                                        !call.isCanceled()
                                        &&
                                        !isFinishing()
                                        &&
                                        !isDestroyed()
                        ) {
                            showError();
                        }
                    }
                }
        );
    }


    // Remove station overlays independently of the current-user marker.
    private void clearStationMarkers() {
        if (stationDetails != null) stationDetails.dismiss();
        if (stationListDialog != null) stationListDialog.dismiss();
        for (Marker marker : stationMarkers) marker.remove();
        stationMarkers.clear();
    }


    // Clear cached results while a new station request is being prepared.
    private void resetStationData() {

        loadedStations.clear();

        filteredStations.clear();


        stationDataLoaded =
                false;


        stationListButton.setEnabled(
                false
        );


        stationListButton.setText(
                getString(
                        R.string
                                .station_list_count,
                        0
                )
        );
    }


    /* =========================================
       SEARCH / FILTER
    ========================================= */

    // Filter the loaded Active/nearby results and keep the map and list counts consistent.
    private void applyStationFilters() {

        if (
                !stationDataLoaded
        ) {
            return;
        }


        filteredStations.clear();


        String query =
                stationSearch
                        .getText()
                        ==
                        null

                        ? ""

                        : stationSearch
                        .getText()
                        .toString();


        for (
                SolarStation station :
                loadedStations
        ) {

            if (
                    StationFilter.matches(
                            station,
                            query,
                            availableOnly
                                    .isChecked()
                    )
            ) {

                filteredStations
                        .add(
                                station
                        );
            }
        }


        stationListButton.setText(
                getString(
                        R.string
                                .station_list_count,

                        filteredStations
                                .size()
                )
        );


        stationListButton.setEnabled(
                !filteredStations
                        .isEmpty()
        );


        showStations(
                filteredStations
        );


        if (
                filteredStations
                        .isEmpty()

                        &&

                        !loadedStations
                                .isEmpty()
        ) {

            status.setText(
                    R.string
                            .stations_no_matches
            );


            retryButton.setVisibility(
                    View.GONE
            );
        }
    }


    /* =========================================
       STATION LIST DIALOG
    ========================================= */

    // Provide a text-based alternative for browsing the current filtered station results.
    private void showStationList() {

        List<SolarStation> displayed =
                new ArrayList<>(
                        filteredStations
                );


        String[] labels =
                new String[
                        displayed.size()
                        ];


        for (
                int i = 0;
                i < displayed.size();
                i++
        ) {

            SolarStation station =
                    displayed.get(i);


            labels[i] =
                    station.getName()
                            +
                            "\n"
                            +
                            station.getAddress();
        }


        stationListDialog =
                new AlertDialog.Builder(
                        this
                )
                        .setTitle(
                                R.string
                                        .solar_stations
                        )

                        .setItems(
                                labels,

                                (
                                        dialog,
                                        which
                                ) ->
                                        showStationDetails(
                                                displayed
                                                        .get(
                                                                which
                                                        )
                                        )
                        )

                        .setNegativeButton(
                                R.string
                                        .station_close,
                                null
                        )

                        .create();


        stationListDialog.show();
    }


    /* =========================================
       SHOW STATIONS ON MAP
    ========================================= */

    // Build station markers from API coordinates and skip coordinates the map cannot display.
    private void showStations(List<SolarStation> stations) {
        if (map == null) {
            // Keep the list usable while Google Maps is loading or unavailable.
            status.setText(getString(R.string.stations_loaded_without_map, stations.size()));
            return;
        }
        clearStationMarkers();
        List<LatLng> positions = new ArrayList<>();
        for (SolarStation station : stations) {
            double lat = station.getLatitude();
            double lon = station.getLongitude();
            if (!Double.isFinite(lat) || !Double.isFinite(lon)
                    || Math.abs(lat) > MAX_MAP_LATITUDE || Math.abs(lon) > 180) continue;
            LatLng position = new LatLng(lat, lon);
            Marker marker = map.addMarker(new MarkerOptions().position(position).title(station.getName())
                    .snippet(getString(R.string.station_details, station.getAddress(),
                            station.getAvailableSlots(), station.getStatus())));
            if (marker != null) {
                marker.setTag(station);
                stationMarkers.add(marker);
                positions.add(position);
            }
        }
        int count = positions.size();
        if (count == 0) {
            status.setText(stations.isEmpty()
                    ? (radiusKm > 0 ? getString(R.string.nearby_empty, radiusKm) : getString(R.string.stations_empty))
                    : getString(R.string.stations_coordinates_unsupported));
            if (radiusKm > 0 && currentLocation != null) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                        radiusKm == 5 ? 13f : 12f));
            }
            return;
        }
        status.setText(radiusKm > 0 ? getString(R.string.nearby_count, count, radiusKm)
                : getString(R.string.stations_count, count));
        if (count < stations.size()) status.append(" " + getString(R.string.stations_skipped, stations.size() - count));
        if (radiusKm > 0 && searchOrigin != null)
            positions.add(new LatLng(searchOrigin.getLatitude(), searchOrigin.getLongitude()));
        mapView.post(() -> {
            if (isFinishing() || isDestroyed() || map == null || (radiusKm == 0 && hasCenteredLocation)) return;
            LatLngBounds.Builder bounds = new LatLngBounds.Builder();
            for (LatLng position : positions) bounds.include(position);
            LatLngBounds area = bounds.build();
            if (area.southwest.equals(area.northeast)) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(positions.get(0), 14f));
            } else if (mapView.getWidth() > 0 && mapView.getHeight() > 0) {
                int padding = Math.min((int) (48 * getResources().getDisplayMetrics().density),
                        Math.min(mapView.getWidth(), mapView.getHeight()) / 4);
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(area, mapView.getWidth(), mapView.getHeight(), padding));
            }
        });
    }


    // Avoid using an old device fix as the origin for a nearby search.
    private boolean hasFreshLocation() {

        return currentLocation != null

                &&

                hasLocationPermission()

                &&

                locationServicesEnabled()

                &&

                SystemClock
                        .elapsedRealtimeNanos()
                        -
                        currentLocation
                                .getElapsedRealtimeNanos()

                        <
                        120_000_000_000L;
    }


    /* =========================================
       STATION DETAILS BOTTOM SHEET
    ========================================= */

    // Show cached details immediately, then request the latest station data from the API.
    private void showStationDetails(
            SolarStation station
    ) {

        if (
                stationDetails != null
        ) {
            stationDetails.dismiss();
        }


        stationDetails =
                new BottomSheetDialog(
                        this
                );


        View content =
                getLayoutInflater()
                        .inflate(
                                R.layout
                                        .sheet_station_details,
                                null
                        );


        /*
         * Show the station data already loaded
         * from the station list/map.
         */
        bindStationDetails(
                content,
                station
        );


        /*
         * Member 3 reservation integration.
         *
         * Configure the Reserve button using
         * the currently loaded station data.
         */
        bindReservationButton(
                content,
                station
        );


        content
                .findViewById(
                        R.id.buttonCloseStation
                )
                .setOnClickListener(
                        v ->
                                stationDetails
                                        .dismiss()
                );


        stationDetails
                .setContentView(
                        content
                );


        stationDetails
                .setOnDismissListener(
                        dialog -> {

                            if (
                                    detailCall != null
                            ) {
                                detailCall.cancel();
                            }
                        }
                );


        stationDetails.show();


        /*
         * Cannot refresh details if there is
         * no valid station ID.
         */
        if (
                station.getId() == null
                        ||
                        station
                                .getId()
                                .isEmpty()
        ) {

            return;
        }


        TextView refreshStatus =
                content.findViewById(
                        R.id
                                .stationDetailRefreshStatus
                );


        refreshStatus.setText(
                R.string
                        .station_detail_loading
        );


        refreshStatus.setVisibility(
                View.VISIBLE
        );


        /*
         * Refresh station details before the
         * Prosumer reserves it.
         *
         * This is important because available
         * slot counts or station status may have
         * changed since the map was first loaded.
         */
        detailCall =
                ApiClient
                        .create(
                                this
                        )
                        .getStation(
                                station.getId()
                        );


        detailCall.enqueue(
                new Callback<SolarStation>() {

                    @Override
                    public void onResponse(
                            Call<SolarStation> call,
                            Response<SolarStation> response
                    ) {

                        if (
                                call != detailCall
                                        ||
                                        call.isCanceled()
                                        ||
                                        isFinishing()
                                        ||
                                        isDestroyed()
                        ) {

                            return;
                        }


                        if (
                                response
                                        .isSuccessful()
                                        &&
                                        response.body()
                                                != null
                        ) {

                            SolarStation latestStation =
                                    response.body();


                            /*
                             * Refresh visible values.
                             */
                            bindStationDetails(
                                    content,
                                    latestStation
                            );


                            /*
                             * Also refresh reservation
                             * eligibility.
                             *
                             * This prevents the old map
                             * data from leaving the
                             * Reserve button enabled.
                             */
                            bindReservationButton(
                                    content,
                                    latestStation
                            );


                            refreshStatus
                                    .setVisibility(
                                            View.GONE
                                    );

                        } else {

                            refreshStatus.setText(
                                    response.code()
                                            ==
                                            404

                                            ? R.string
                                            .station_detail_removed

                                            : R.string
                                            .station_detail_failed
                            );
                        }
                    }


                    @Override
                    public void onFailure(
                            Call<SolarStation> call,
                            Throwable error
                    ) {

                        if (
                                call == detailCall
                                        &&
                                        !call.isCanceled()
                                        &&
                                        !isFinishing()
                                        &&
                                        !isDestroyed()
                        ) {

                            refreshStatus.setText(
                                    R.string
                                            .station_detail_failed
                            );
                        }
                    }
                }
        );
    }


    /* =========================================
       MEMBER 3:
       RESERVE STATION BUTTON
    ========================================= */

    private void bindReservationButton(
            View content,
            SolarStation station
    ) {

        MaterialButton reserveButton =
                content.findViewById(
                        R.id
                                .buttonReserveStation
                );


        if (
                reserveButton == null
        ) {
            return;
        }


        boolean hasValidStationId =
                station.getId() != null

                        &&

                        !station.getId()
                                .trim()
                                .isEmpty();


        boolean active =
                "Active"
                        .equalsIgnoreCase(
                                station
                                        .getStatus()
                        );


        boolean hasAvailableSlots =
                station.getAvailableSlots()
                        >
                        0;


        boolean canReserve =
                hasValidStationId
                        &&
                        active
                        &&
                        hasAvailableSlots;


        reserveButton.setEnabled(
                canReserve
        );


        if (
                canReserve
        ) {

            reserveButton.setText(
                    "Reserve this station  →"
            );

        } else if (
                !active
        ) {

            reserveButton.setText(
                    "Station unavailable"
            );

        } else if (
                !hasAvailableSlots
        ) {

            reserveButton.setText(
                    "No slots available"
            );

        } else {

            reserveButton.setText(
                    "Reservation unavailable"
            );
        }


        reserveButton.setOnClickListener(
                v -> {

                    /*
                     * Never proceed when the
                     * station cannot currently
                     * accept reservations.
                     */
                    if (
                            !canReserve
                    ) {

                        Toast.makeText(
                                StationMapActivity.this,

                                "This station is not currently available for reservations.",

                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }


                    /*
                     * Open Member 3 booking screen.
                     */
                    Intent intent =
                            new Intent(
                                    StationMapActivity.this,
                                    ReservationActivity.class
                            );


                    intent.putExtra(
                            "stationId",
                            station.getId()
                    );


                    intent.putExtra(
                            "stationName",
                            station.getName()
                    );


                    /*
                     * Optional extra data.
                     *
                     * This can be useful later
                     * when displaying reservation
                     * information.
                     */
                    intent.putExtra(
                            "stationAddress",
                            station.getAddress()
                    );


                    intent.putExtra(
                            "stationOpeningTime",
                            station.getOpeningTime()
                    );


                    intent.putExtra(
                            "stationClosingTime",
                            station.getClosingTime()
                    );


                    stationDetails.dismiss();


                    startActivity(
                            intent
                    );
                }
        );
    }


    /* =========================================
       BIND STATION DETAILS
    ========================================= */

    // Present capacity, free slots, operating hours and location from the station response.
    private void bindStationDetails(
            View content,
            SolarStation station
    ) {

        (
                (TextView)
                        content.findViewById(
                                R.id
                                        .stationDetailName
                        )
        )
                .setText(
                        station.getName()
                );


        (
                (TextView)
                        content.findViewById(
                                R.id
                                        .stationDetailAddress
                        )
        )
                .setText(
                        station.getAddress()
                );


        NumberFormat number =
                NumberFormat
                        .getNumberInstance();


        number.setMaximumFractionDigits(
                1
        );


        (
                (TextView)
                        content.findViewById(
                                R.id
                                        .stationDetailCapacity
                        )
        )
                .setText(
                        getString(
                                R.string
                                        .station_capacity,

                                number.format(
                                        station
                                                .getCapacityKw()
                                )
                        )
                );


        (
                (TextView)
                        content.findViewById(
                                R.id
                                        .stationDetailSlots
                        )
        )
                .setText(
                        getResources()
                                .getQuantityString(
                                        R.plurals
                                                .station_slots,

                                        station
                                                .getAvailableSlots(),

                                        station
                                                .getAvailableSlots()
                                )
                );


        (
                (TextView)
                        content.findViewById(
                                R.id
                                        .stationDetailHours
                        )
        )
                .setText(
                        getString(
                                R.string
                                        .station_hours,

                                detailValue(
                                        station
                                                .getOpeningTime()
                                ),

                                detailValue(
                                        station
                                                .getClosingTime()
                                )
                        )
                );


        (
                (TextView)
                        content.findViewById(
                                R.id
                                        .stationDetailStatus
                        )
        )
                .setText(
                        getString(
                                R.string
                                        .station_status,

                                detailValue(
                                        station
                                                .getStatus()
                                )
                        )
                );


        (
                (TextView)
                        content.findViewById(
                                R.id
                                        .stationDetailLocation
                        )
        )
                .setText(
                        getString(
                                R.string
                                        .station_coordinates,

                                station
                                        .getLatitude(),

                                station
                                        .getLongitude()
                        )
                );


        TextView distance =
                content.findViewById(
                        R.id
                                .stationDetailDistance
                );


        if (
                hasFreshLocation()
        ) {

            float[] meters =
                    new float[1];


            Location.distanceBetween(
                    currentLocation
                            .getLatitude(),

                    currentLocation
                            .getLongitude(),

                    station
                            .getLatitude(),

                    station
                            .getLongitude(),

                    meters
            );


            distance.setText(
                    getString(
                            R.string
                                    .station_distance,

                            meters[0]
                                    /
                                    1000.0
                    )
            );

        } else {

            distance.setText(
                    R.string
                            .station_distance_unavailable
            );
        }
    }


    private String detailValue(
            String value
    ) {

        return value == null
                ||
                value.trim()
                        .isEmpty()

                ? getString(
                R.string
                        .station_not_provided
        )

                : value;
    }


    /* =========================================
       SAVE STATE
    ========================================= */

    @Override
    // Preserve discovery filters and map state across activity recreation.
    protected void onSaveInstanceState(
            Bundle outState
    ) {

        outState.putInt(
                "radiusKm",
                radiusKm
        );


        outState.putString(
                "stationQuery",

                stationSearch.getText()
                        ==
                        null

                        ? ""

                        : stationSearch
                        .getText()
                        .toString()
        );


        outState.putBoolean(
                "availableOnly",
                availableOnly
                        .isChecked()
        );


        if (mapInitialized) {
            Bundle mapState = new Bundle();
            mapView.onSaveInstanceState(mapState);
            outState.putBundle("googleMapState", mapState);
        }
        super.onSaveInstanceState(outState);
    }


    /* =========================================
       ERROR
    ========================================= */

    private void showError() {

        status.setText(
                R.string
                        .stations_error
        );


        retryButton.setVisibility(
                View.VISIBLE
        );
    }


    /* =========================================
       LIFECYCLE
    ========================================= */

    @Override
    protected void onResume() {

        super.onResume();


        if (mapInitialized) mapView.onResume();


        if (
                locationStatus != null
        ) {

            if (
                    hasLocationPermission()
            ) {

                startLocationUpdates();

            } else {

                removeUserMarker();


                locationStatus.setText(
                        R.string
                                .location_denied
                );
            }
        }
    }


    @Override
    // Pause map rendering and foreground location tracking when the screen is not visible.
    protected void onPause() {

        stopLocationUpdates();


        if (mapInitialized) mapView.onPause();


        super.onPause();
    }


    @Override
    // Cancel pending calls and release dialogs/map resources when the activity is destroyed.
    protected void onDestroy() {

        if (
                stationsCall != null
        ) {
            stationsCall.cancel();
        }


        if (
                stationDetails != null
        ) {
            stationDetails.dismiss();
        }


        if (
                stationListDialog != null
        ) {
            stationListDialog.dismiss();
        }


        if (
                detailCall != null
        ) {
            detailCall.cancel();
        }


        stopLocationUpdates();


        if (mapInitialized) mapView.onDestroy();
        map = null;


        super.onDestroy();
    }
}