using MongoDB.Driver;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class StationService
{
    private readonly MongoDbContext _context;

    public StationService(
        MongoDbContext context)
    {
        _context = context;
    }


    public async Task<List<SolarStation>>
        GetAllAsync(
            string? status = null)
    {
        FilterDefinition<SolarStation> filter =
            Builders<SolarStation>
                .Filter.Empty;

        if (!string.IsNullOrWhiteSpace(status))
        {
            filter =
                Builders<SolarStation>
                    .Filter.Eq(
                        x => x.Status,
                        status
                    );
        }

        return await _context.Stations
            .Find(filter)
            .SortBy(x => x.Name)
            .ToListAsync();
    }


    public async Task<SolarStation?>
        GetByIdAsync(
            string id)
    {
        return await _context.Stations
            .Find(x => x.Id == id)
            .FirstOrDefaultAsync();
    }


    public async Task<SolarStation>
        CreateAsync(
            StationRequest request)
    {
        var existing =
            await _context.Stations
                .Find(x =>
                    x.Name.ToLower() ==
                    request.Name
                        .Trim()
                        .ToLower())
                .FirstOrDefaultAsync();

        if (existing != null)
        {
            throw new InvalidOperationException(
                "A station with this name already exists."
            );
        }

        var now =
            DateTime.UtcNow;

        var station =
            new SolarStation
            {
                Name =
                    request.Name.Trim(),

                Address =
                    request.Address.Trim(),

                Latitude =
                    request.Latitude,

                Longitude =
                    request.Longitude,

                CapacityKw =
                    request.CapacityKw,

                TotalSlots =
                    request.TotalSlots,

                AvailableSlots =
                    request.TotalSlots,

                OpeningTime =
                    request.OpeningTime.Trim(),

                ClosingTime =
                    request.ClosingTime.Trim(),

                Status =
                    "Active",

                CreatedAt =
                    now,

                UpdatedAt =
                    now
            };

        await _context.Stations
            .InsertOneAsync(station);

        return station;
    }


    public async Task<SolarStation?>
        UpdateAsync(
            string id,
            StationRequest request)
    {
        var existing =
            await GetByIdAsync(id);

        if (existing == null)
            return null;

        var duplicate =
            await _context.Stations
                .Find(x =>
                    x.Id != id &&
                    x.Name.ToLower() ==
                    request.Name
                        .Trim()
                        .ToLower())
                .FirstOrDefaultAsync();

        if (duplicate != null)
        {
            throw new InvalidOperationException(
                "A station with this name already exists."
            );
        }

        var bookedSlots =
            Math.Max(
                0,
                existing.TotalSlots -
                existing.AvailableSlots
            );

        var newAvailable =
            Math.Max(
                0,
                request.TotalSlots -
                bookedSlots
            );

        var update =
            Builders<SolarStation>
                .Update
                .Set(
                    x => x.Name,
                    request.Name.Trim()
                )
                .Set(
                    x => x.Address,
                    request.Address.Trim()
                )
                .Set(
                    x => x.Latitude,
                    request.Latitude
                )
                .Set(
                    x => x.Longitude,
                    request.Longitude
                )
                .Set(
                    x => x.CapacityKw,
                    request.CapacityKw
                )
                .Set(
                    x => x.TotalSlots,
                    request.TotalSlots
                )
                .Set(
                    x => x.AvailableSlots,
                    newAvailable
                )
                .Set(
                    x => x.OpeningTime,
                    request.OpeningTime.Trim()
                )
                .Set(
                    x => x.ClosingTime,
                    request.ClosingTime.Trim()
                )
                .Set(
                    x => x.UpdatedAt,
                    DateTime.UtcNow
                );

        await _context.Stations
            .UpdateOneAsync(
                x => x.Id == id,
                update
            );

        return await GetByIdAsync(id);
    }


    public async Task<SolarStation?>
        UpdateStatusAsync(
            string id,
            string status)
    {
        var station =
            await GetByIdAsync(id);

        if (station == null)
            return null;

        var normalized =
            status.Trim();

        if (
            normalized != "Active" &&
            normalized != "Inactive"
        )
        {
            throw new InvalidOperationException(
                "Invalid station status."
            );
        }

        var update =
            Builders<SolarStation>
                .Update
                .Set(
                    x => x.Status,
                    normalized
                )
                .Set(
                    x => x.UpdatedAt,
                    DateTime.UtcNow
                );

        await _context.Stations
            .UpdateOneAsync(
                x => x.Id == id,
                update
            );

        return await GetByIdAsync(id);
    }


    public async Task<List<SolarStation>>
        GetNearbyAsync(
            double latitude,
            double longitude,
            double radiusKm)
    {
        var stations =
            await _context.Stations
                .Find(x =>
                    x.Status == "Active")
                .ToListAsync();

        return stations
            .Where(station =>
                CalculateDistanceKm(
                    latitude,
                    longitude,
                    station.Latitude,
                    station.Longitude
                ) <= radiusKm
            )
            .OrderBy(station =>
                CalculateDistanceKm(
                    latitude,
                    longitude,
                    station.Latitude,
                    station.Longitude
                )
            )
            .ToList();
    }


    private static double CalculateDistanceKm(
        double lat1,
        double lon1,
        double lat2,
        double lon2)
    {
        const double earthRadius =
            6371.0;

        var latDistance =
            DegreesToRadians(
                lat2 - lat1
            );

        var lonDistance =
            DegreesToRadians(
                lon2 - lon1
            );

        var a =
            Math.Sin(
                latDistance / 2
            ) *
            Math.Sin(
                latDistance / 2
            ) +
            Math.Cos(
                DegreesToRadians(lat1)
            ) *
            Math.Cos(
                DegreesToRadians(lat2)
            ) *
            Math.Sin(
                lonDistance / 2
            ) *
            Math.Sin(
                lonDistance / 2
            );

        var c =
            2 *
            Math.Atan2(
                Math.Sqrt(a),
                Math.Sqrt(1 - a)
            );

        return earthRadius * c;
    }


    private static double DegreesToRadians(
        double degrees)
    {
        return degrees *
               Math.PI /
               180.0;
    }
}