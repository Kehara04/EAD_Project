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
