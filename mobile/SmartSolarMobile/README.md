# Run the Android app locally

The app uses `http://127.0.0.1:5103/api/` through ADB reverse port forwarding. This works on authorized USB-connected phones and Android emulators without exposing the backend on Wi-Fi.

1. Start the backend in its own terminal:

   ```sh
   cd backend/SmartSolar.Api
   dotnet run --launch-profile http
   ```

2. Connect your phone and allow USB debugging, or start an emulator. From `mobile/SmartSolarMobile`, run:

   ```sh
   sh connect-backend.sh
   ./gradlew installDebug
   ```

3. Open Smart Solar, or select your device and press Run in Android Studio.

Run `sh connect-backend.sh` again after reconnecting a device, restarting ADB, or restarting the emulator. To target one device, pass its serial: `sh connect-backend.sh DEVICE_SERIAL`.

Keep the backend and ADB connection running while testing login and API features. This local development setup does not provide API access after the phone disconnects; standalone use requires a reachable deployed backend URL.

See [Android's local-server documentation](https://developer.android.com/develop/ui/views/layout/webapps/access-local-server) for ADB reverse forwarding.


## Google Maps

The station map uses Google Maps SDK for Android. OpenStreetMap/osmdroid is no
longer included. Station data, nearby searches, location permission and reservation
navigation continue using the existing REST API.

Set `MAPS_API_KEY=your-android-maps-key` in this directory's ignored
`local.properties` (or the build environment), then rebuild and install the app.
A key already present in this file is used automatically; do not commit it.

In Google Cloud, enable **Maps SDK for Android** and billing. Restrict the key to
Android package `com.smartsolar` and the signing certificate SHA-1, and restrict its
API access to Maps SDK for Android. Get your development SHA-1 with
`./gradlew :app:signingReport`. Release signing needs its own fingerprint.
Use a Google Play/Google APIs emulator or a phone with Google Play services.

The Google Maps key is separate from the backend's Geoapify address-search key.
Geocoded station coordinates work with Google Maps without changing the database.
Missing configuration or Play services displays a message; the station list remains
available. If tiles stay blank, check internet access, API enablement, billing and
key restrictions. No OpenStreetMap fallback is included.

Reference: https://developers.google.com/maps/documentation/android-sdk/get-api-key
