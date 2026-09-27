import { useEffect, useState } from "react";
import { getStationAddressSuggestions } from "../services/stationService";
import { getApiError } from "../services/errorService";

// Render an address search field with location suggestions and a selected location preview.
export default function StationAddressField({ value, location, onChange, onSelect }) {
  const [result, setResult] = useState(null);
  const [retry, setRetry] = useState(0);

  // Debounce address searches and cancel outdated requests to prevent stale suggestions.
  useEffect(() => {
    if (location || value.trim().length < 3) return;
    const controller = new AbortController();
    let cancelled = false;
    const timer = setTimeout(async () => {
      setResult({ query: value, loading: true, items: [] });
      try {
        const items = await getStationAddressSuggestions(value.trim(), controller.signal);
        if (!cancelled) setResult({ query: value, loading: false, items });
      } catch (error) {
        if (!cancelled) setResult({ query: value, loading: false, items: [],
          error: getApiError(error, "Unable to search addresses. Please try again.") });
      }
    }, 450);
  
    return () => {
      cancelled = true;
      clearTimeout(timer);
      controller.abort();
    };
  }, [value, location, retry]);

  // Only display results belonging to the current input and hide them after selection.
  const current = !location && result?.query === value ? result : null;
  // Area-level results may be centroids rather than the exact station entrance.
  const approximate = location && ["city", "state", "county", "postcode", "suburb", "district", "country"].includes(location.resultType);

  return (
    <div className="mb-3">
      <label className="form-label" htmlFor="station-address">Station address</label>
      <input id="station-address" className="form-control app-input" name="address"
        value={value} onChange={(event) => onChange(event.target.value)}
        placeholder="Street, landmark or city in Sri Lanka" autoComplete="off"
        aria-describedby="station-address-help" required minLength={3} maxLength={250} />
      <div id="station-address-help" className="form-text">
        Type at least 3 characters, then choose a suggestion. Include the street and city for a closer match.
      </div>
      <div aria-live="polite">
        {current?.loading && <p className="small text-muted mt-2 mb-0">Searching Sri Lankan addresses...</p>}
        {current?.error && (
          <div className="small text-danger mt-2" role="alert">
            {current.error}{" "}
            <button type="button" className="btn btn-link btn-sm p-0" onClick={() => setRetry((x) => x + 1)}>Retry</button>
          </div>
        )}
        {current && !current.loading && !current.error && current.items.length === 0 && (
          <p className="small text-muted mt-2 mb-0">No matching addresses in Sri Lanka. Try a nearby landmark, street or city.</p>
        )}
      </div>
      {current?.items.length > 0 && (
        <ul className="list-group mt-2 station-address-suggestions" aria-label="Sri Lankan address suggestions">
          {current.items.map((item) => (
            <li className="list-group-item p-0" key={item.locationToken}>
              <button type="button" className="list-group-item-action border-0 bg-transparent p-3 w-100 text-start"
                onClick={() => onSelect(item)}>{item.address}</button>
            </li>
          ))}
        </ul>
      )}
      {location && (
        <div className="station-location-preview mt-2">
          <strong>{location.locationToken ? "Address selected" : "Saved station location"}</strong>
          <p className="small mb-1">{approximate ? "This is an area location. Use a more specific address if the station is elsewhere." : "Check the map pin before saving; address matches may be approximate."}</p>
          <a href={`https://www.openstreetmap.org/?mlat=${location.latitude}&mlon=${location.longitude}#map=18/${location.latitude}/${location.longitude}`}
            target="_blank" rel="noopener noreferrer">Check location on map ↗</a>
        </div>
      )}
      <p className="small text-muted mt-2 mb-0">
        Address search by <a href="https://www.geoapify.com/" target="_blank" rel="noopener noreferrer">Geoapify</a>
      </p>
    </div>
  );
}
