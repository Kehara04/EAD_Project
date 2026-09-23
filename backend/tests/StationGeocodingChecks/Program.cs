using System.Net;
using System.Text.Json;
using Microsoft.AspNetCore.DataProtection;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Configuration;
using SmartSolar.Api.Services;

var provider = new EphemeralDataProtectionProvider();
var handler = new FakeProvider();
var settings = new ConfigurationBuilder().AddInMemoryCollection(new Dictionary<string, string?>
    { ["GEOAPIFY_API_KEY"] = "test-key" }).Build();
using var cache = new MemoryCache(new MemoryCacheOptions());
using var http = new HttpClient(handler);
var service = new StationGeocodingService(http, settings, cache, provider);
var items = await service.SearchAsync("Negombo & beach", default);
Check(items.Count == 1, "Reject foreign results and results missing coordinates");
Check(handler.LastUri!.Query.Contains("filter=countrycode:lk")
    && handler.LastUri.Query.Contains("Negombo%20%26%20beach"), "Sri Lanka filter and URL encoding");
var location = service.ResolveSelection(items[0].Address, items[0].LocationToken);
Check(location.Latitude == 7.2083 && location.Longitude == 79.8358, "Preserve latitude/longitude order");
var storedJson = JsonSerializer.Serialize(location, new JsonSerializerOptions(JsonSerializerDefaults.Web));
Check(storedJson.Contains("\"latitude\":7.2083") && storedJson.Contains("\"longitude\":79.8358"), "Mobile coordinate JSON contract");
await service.SearchAsync("negombo & beach", default);
Check(handler.Calls == 1, "Cache repeated searches");
Reject(() => service.ResolveSelection("Different address", items[0].LocationToken), "Reject changed address");
Reject(() => service.ResolveSelection(items[0].Address, "tampered-token"), "Reject tampered token");
Reject(() => service.ResolveSelection(items[0].Address, null), "Reject manual address without selection");
var protector = provider.CreateProtector("SmartSolar.StationLocation.v1").ToTimeLimitedDataProtector();
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
var unconfigured = new StationGeocodingService(http, new ConfigurationBuilder().Build(), cache, provider);
await RejectAsync<GeocodingUnavailableException>(() => unconfigured.SearchAsync("Colombo", default), "Handle missing key");
handler.Timeout = true;
await RejectAsync<GeocodingUnavailableException>(() => service.SearchAsync("Timeout", default), "Handle timeout");
using var cancelled = new CancellationTokenSource();
cancelled.Cancel();
await RejectAsync<OperationCanceledException>(() => service.SearchAsync("Cancelled", cancelled.Token), "Propagate cancelled request");
Console.WriteLine("All geocoding checks passed. No live provider or database calls made.");

static void Check(bool condition, string name)
{
    if (!condition) throw new Exception(name);
    Console.WriteLine("PASS: " + name);
}
static void Reject(Action action, string name)
{
    try { action(); } catch (InvalidOperationException) { Console.WriteLine("PASS: " + name); return; }
    throw new Exception(name);
}
static async Task RejectAsync<T>(Func<Task<List<StationAddressSuggestion>>> action, string name) where T : Exception
{
    try { await action(); } catch (T) { Console.WriteLine("PASS: " + name); return; }
    throw new Exception(name);
}
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
