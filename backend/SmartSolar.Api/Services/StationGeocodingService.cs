/*
 * File: StationGeocodingService.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Searches Sri Lankan addresses through Geoapify, caches suggestions,
 * and protects selected coordinates for station creation and updates.
 *
 * References:
 * Geoapify Geocoding API Documentation
 * https://apidocs.geoapify.com/docs/geocoding/forward-geocoding/
 *
 * YouTube Short: caching in ASP.NET Core Web API
 * https://youtube.com/shorts/fRyIwJPZbpM?si=-V0vkp0ICY8-G26w
 *
 * YouTube Video: iimemorycache in ASP.NET Core Web API
 * https://youtu.be/DPpQUuEFo60?si=uYQ74Ru4vcmd2uWr
 */

using System.Security.Cryptography;
using System.Text.Json;
using System.Text.Json.Serialization;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.Extensions.Caching.Memory;

namespace SmartSolar.Api.Services;

// Represents an address suggestion returned to the client.
public record StationAddressSuggestion(string Address, double Latitude, double Longitude,
    string LocationToken, string? ResultType);

// Stores the validated address and coordinates of a selected location.
public record ResolvedStationLocation(string Address, double Latitude, double Longitude);

// Represents an address search service failure.
public class GeocodingUnavailableException(string message) : Exception(message);

// Initializes the HTTP client, configuration, cache, and token protector.
public class StationGeocodingService
{
    private readonly HttpClient _http;
    private readonly IConfiguration _configuration;
    private readonly IMemoryCache _cache;
    private readonly ITimeLimitedDataProtector _protector;

    public StationGeocodingService(HttpClient http, IConfiguration configuration,
        IMemoryCache cache, IDataProtectionProvider protection)
    {
        _http = http;
        _configuration = configuration;
        _cache = cache;

        // Creates a time-limited protector for station location tokens.
        _protector = protection.CreateProtector("SmartSolar.StationLocation.v1")
            .ToTimeLimitedDataProtector();
    }

    // Searches Sri Lankan addresses and returns protected location suggestions.
    public async Task<List<StationAddressSuggestion>> SearchAsync(string query, CancellationToken cancellationToken)
    {
        query = query.Trim();
        if (query.Length < 3 || query.Length > 250)
            throw new InvalidOperationException("Enter between 3 and 250 characters to search for an address.");

        // Configuration is server-side; the browser receives suggestions, not the API key.
        var apiKey = _configuration["GEOAPIFY_API_KEY"];
        if (string.IsNullOrWhiteSpace(apiKey))
            throw new GeocodingUnavailableException("Address search is not configured. Ask the administrator to configure the geocoding service.");

        // Normalize repeated queries to reduce provider calls while an admin types.
        var cacheKey = "station-address:lk:" + query.ToLowerInvariant();
        if (!_cache.TryGetValue(cacheKey, out List<GeoAddress>? addresses))
        {
            try
            {
                // Restrict search to Sri Lanka and escape user input as a query value.
                var url = "https://api.geoapify.com/v1/geocode/autocomplete?format=json&limit=5&lang=en"
                    + "&filter=countrycode:lk&text=" + Uri.EscapeDataString(query)
                    + "&apiKey=" + Uri.EscapeDataString(apiKey);
                using var response = await _http.GetAsync(url, cancellationToken);
                if (!response.IsSuccessStatusCode)
                    throw new GeocodingUnavailableException("Address search is temporarily unavailable. Please try again shortly.");
                var result = await response.Content.ReadFromJsonAsync<GeoResponse>(cancellationToken);
                if (result?.Results == null)
                    throw new GeocodingUnavailableException("Address search returned an invalid response. Please try again.");

                // Validate the provider response before issuing a trusted location token.
                addresses = result.Results.Where(x => x.CountryCode == "lk"
                    && !string.IsNullOrWhiteSpace(x.Formatted) && x.Formatted.Length <= 250
                    && x.Lat is double lat && x.Lon is double lon
                    && double.IsFinite(lat) && double.IsFinite(lon)
                    && lat >= -90 && lat <= 90 && lon >= -180 && lon <= 180).ToList();
                _cache.Set(cacheKey, addresses, TimeSpan.FromMinutes(5));
            }
            // A provider timeout is retryable; client-request cancellation propagates.
            catch (OperationCanceledException) when (!cancellationToken.IsCancellationRequested)
            {
                throw new GeocodingUnavailableException("Address search timed out. Please try again.");
            }
            catch (Exception ex) when (ex is HttpRequestException or JsonException)
            {
                throw new GeocodingUnavailableException("Unable to reach the address search service. Please try again.");
            }
        }

        // Issue a fresh expiring token even when the address came from the search cache.
        return addresses!.Select(x =>
        {
            var location = new ResolvedStationLocation(x.Formatted!, x.Lat!.Value, x.Lon!.Value);

            // Clients cannot replace a suggestion's coordinates or address with arbitrary values.
            var token = _protector.Protect(JsonSerializer.Serialize(location), TimeSpan.FromHours(1));
            return new StationAddressSuggestion(location.Address, location.Latitude, location.Longitude, token, x.ResultType);
        }).ToList();
    }

    // Validates a selected location token and returns its trusted coordinates.
    public ResolvedStationLocation ResolveSelection(string address, string? token)
    {
        if (string.IsNullOrWhiteSpace(token))
            throw new InvalidOperationException("Select a Sri Lankan address suggestion before saving the station.");
        try
        {
            // Unprotect verifies authenticity and expiry; the address must still match.
            var location = JsonSerializer.Deserialize<ResolvedStationLocation>(_protector.Unprotect(token));
            if (location == null || location.Address != address.Trim())
                throw new InvalidOperationException("The address has changed. Select a matching suggestion again.");
            return location;
        }
        catch (Exception ex) when (ex is CryptographicException or JsonException or FormatException)
        {
            throw new InvalidOperationException("The selected address has expired or is invalid. Search and select the address again.");
        }
    }

    // Only deserialize the provider fields needed by station address selection.
    private class GeoResponse
    {
        public List<GeoAddress>? Results { get; set; }
    }

    // Defines the Geoapify fields required for station address selection.
    private class GeoAddress
    {
        public string? Formatted { get; set; }
        public double? Lat { get; set; }
        public double? Lon { get; set; }
        [JsonPropertyName("country_code")]
        public string? CountryCode { get; set; }
        [JsonPropertyName("result_type")]
        public string? ResultType { get; set; }
    }
}
