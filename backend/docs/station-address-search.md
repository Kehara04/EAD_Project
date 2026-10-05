# Sri Lanka station address search

Station Management no longer accepts manual latitude/longitude. The admin types
an address, selects a Sri Lankan suggestion and checks its OpenStreetMap pin.
Suggestions come from Geoapify via the authenticated Backoffice-only endpoint:

`GET /api/stations/address-suggestions?query=Negombo`

## Local setup

1. Create a Geoapify project at https://myprojects.geoapify.com/ and obtain an API key.
2. Add `GEOAPIFY_API_KEY=your-key` to `backend/SmartSolar.Api/.env` (never commit the key).
3. Restart the backend with `dotnet run --launch-profile http` from that directory.
4. Run the web app with `npm run dev` from `web/smart-solar-web`.
5. In Station Management, enter a street/landmark and city, choose a suggestion,
   check its pin and save. Reload stations in the mobile app to see the new pin.

The key stays on the server. No Google Maps key is required and the mobile map
continues using OpenStreetMap. The provider receives the address text to search.
Geoapify quotas and plan limits apply. Public OSM Nominatim is not used because
its usage policy prohibits autocomplete.

## API contract

Create/update requests include `address` and `locationToken` from a suggestion,
plus the existing station fields. The backend resolves that protected token to
provider coordinates; client latitude/longitude fields are not accepted as the
location source. Selection tokens expire after one hour. Changing the address
requires a fresh selection. Unchanged existing addresses preserve their stored
coordinates without contacting the geocoder. Selecting a suggestion for the same
address intentionally updates its coordinates. Existing stations are not migrated.

The station response still contains `latitude` and `longitude`, so the mobile map
requires no code or APK update. Geocoding may return an area/street centroid rather
than an exact building: the UI shows a map-check link and warns about broad matches.
If no suitable match exists, refine the address; do not save a random city pin.

Search is debounced by 450 ms, cancelled on changed input and cached for five
minutes on the backend. Requests restrict results to country code `lk`. Provider
failures produce a retryable 503; missing configuration never silently saves 0,0.
Keep ASP.NET Data Protection keys persistent/shared if deploying multiple instances.
HTTP client URI logging is disabled for this client because provider URLs contain
the API key.

## Verification

Run `dotnet run --project backend/tests/StationGeocodingChecks` from the repo root.
These checks use a fake HTTP provider, require no key/database, and cover filtering,
coordinate order, cache, token validation/expiry, empty results, invalid responses,
rate limits, timeouts and cancellation. A live end-to-end check requires a Geoapify
key: create a test station using a specific address, compare its map pin, edit its
address, then reload the mobile map and verify the updated pin.
