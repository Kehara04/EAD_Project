/*
 * File: Program.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Station Geocoding Checks
 * Description:
 * Tests station address search, coordinate validation, caching,
 * protected location tokens, and geocoding error handling.
 * Uses a fake HTTP provider without making live API or database calls.
 */

using System.Net;
using System.Text.Json;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Configuration;
using SmartSolar.Api.Services;

// Creates a temporary data protection provider for testing.
var provider = new EphemeralDataProtectionProvider();

// Creates a fake Geoapify provider to simulate API responses.
var handler = new FakeProvider();

// Configures a test API key without using real credentials.
var settings = new ConfigurationBuilder().AddInMemoryCollection(new Dictionary<string, string?>
    { ["GEOAPIFY_API_KEY"] = "test-key" }).Build();

// Initializes the cache and HTTP client.
using var cache = new MemoryCache(new MemoryCacheOptions());
using var http = new HttpClient(handler);

// Creates the geocoding service with test dependencies.
var service = new StationGeocodingService(http, settings, cache, provider);

// Tests address searching and filtering of invalid provider results.
var items = await service.SearchAsync("Negombo & beach", default);
Check(items.Count == 1, "Reject foreign results and results missing coordinates");
Check(handler.LastUri!.Query.Contains("filter=countrycode:lk")
    && handler.LastUri.Query.Contains("Negombo%20%26%20beach"), "Sri Lanka filter and URL encoding");

// Resolves the selected address using its protected location token.
var location = service.ResolveSelection(items[0].Address, items[0].LocationToken);
Check(location.Latitude == 7.2083 && location.Longitude == 79.8358, "Preserve latitude/longitude order");

// Serializes the selected location using web JSON settings.
var storedJson = JsonSerializer.Serialize(location, new JsonSerializerOptions(JsonSerializerDefaults.Web));
Check(storedJson.Contains("\"latitude\":7.2083") && storedJson.Contains("\"longitude\":79.8358"), "Mobile coordinate JSON contract");
await service.SearchAsync("negombo & beach", default);
Check(handler.Calls == 1, "Cache repeated searches");
Reject(() => service.ResolveSelection("Different address", items[0].LocationToken), "Reject changed address");
Reject(() => service.ResolveSelection(items[0].Address, "tampered-token"), "Reject tampered token");
Reject(() => service.ResolveSelection(items[0].Address, null), "Reject manual address without selection");

// Creates a time-limited protector using the service's protection purpose.
var protector = provider.CreateProtector("SmartSolar.StationLocation.v1").ToTimeLimitedDataProtector();

// Generates an already-expired location token.
var expired = protector.Protect(JsonSerializer.Serialize(location), DateTimeOffset.UtcNow.AddMinutes(-1));
Reject(() => service.ResolveSelection(location.Address, expired), "Reject expired selection");
await RejectAsync<InvalidOperationException>(() => service.SearchAsync("ab", default), "Reject short query");
await RejectAsync<InvalidOperationException>(() => service.SearchAsync(new string('a', 251), default), "Reject long query");
handler.Body = "{\"results\":[]}";
Check((await service.SearchAsync("No matches", default)).Count == 0, "Handle no results");
handler.Body = "invalid-json";
await RejectAsync<GeocodingUnavailableException>(() => service.SearchAsync("Malformed response", default), "Handle invalid provider JSON");
handler.Status = HttpStatusCode.TooManyRequests;
await RejectAsync<GeocodingUnavailableException>(() => service.SearchAsync("Rate limited", default), "Handle provider rate limit");

// Creates a service without a configured Geoapify API key.
var unconfigured = new StationGeocodingService(http, new ConfigurationBuilder().Build(), cache, provider);
await RejectAsync<GeocodingUnavailableException>(() => unconfigured.SearchAsync("Colombo", default), "Handle missing key");
handler.Timeout = true;
await RejectAsync<GeocodingUnavailableException>(() => service.SearchAsync("Timeout", default), "Handle timeout");
using var cancelled = new CancellationTokenSource();
cancelled.Cancel();
await RejectAsync<OperationCanceledException>(() => service.SearchAsync("Cancelled", cancelled.Token), "Propagate cancelled request");
Console.WriteLine("All geocoding checks passed. No live provider or database calls made.");

// Verifies a test condition and reports its result.
static void Check(bool condition, string name)
{
    if (!condition) throw new Exception(name);
    Console.WriteLine("PASS: " + name);
}

// Verifies that an action throws an InvalidOperationException.
static void Reject(Action action, string name)
{
    try { action(); } catch (InvalidOperationException) { Console.WriteLine("PASS: " + name); return; }
    throw new Exception(name);
}

// Verifies that an asynchronous action throws the expected exception type.
static async Task RejectAsync<T>(Func<Task<List<StationAddressSuggestion>>> action, string name) where T : Exception
{
    try { await action(); } catch (T) { Console.WriteLine("PASS: " + name); return; }
    throw new Exception(name);
}

// Simulates Geoapify HTTP responses without using the live provider.
sealed class FakeProvider : HttpMessageHandler
{
    public int Calls;
    public Uri? LastUri;
    public bool Timeout;
    public HttpStatusCode Status = HttpStatusCode.OK;
    public string Body = """
        {"results":[
          {"formatted":"Negombo, Sri Lanka","lat":7.2083,"lon":79.8358,"country_code":"lk","result_type":"city"},
          {"formatted":"Foreign city","lat":10,"lon":20,"country_code":"in"},
          {"formatted":"Missing coordinates","country_code":"lk"}]}
        """;
    protected override Task<HttpResponseMessage> SendAsync(HttpRequestMessage request, CancellationToken cancellationToken)
    {
        cancellationToken.ThrowIfCancellationRequested();
        if (Timeout) throw new TaskCanceledException();
        Calls++;
        LastUri = request.RequestUri;
        return Task.FromResult(new HttpResponseMessage(Status) { Content = new StringContent(Body) });
    }
}
