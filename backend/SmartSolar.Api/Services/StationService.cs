/*
 * File: StationService.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Manages station persistence, location selection, capacity rules,
 * reservation checks and distance-based station discovery.
 */

using MongoDB.Bson;
using MongoDB.Driver;
using SmartSolar.Api.Constants;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class StationService
{
    private readonly MongoDbContext _context;
    private readonly StationGeocodingService _geocoding;

    public StationService(
        MongoDbContext context, StationGeocodingService geocoding)
    {
        _context = context;
        _geocoding = geocoding;
    }


    /* =========================================
       GET ALL STATIONS
    ========================================= */

    public async Task<List<SolarStation>>
        GetAllAsync(
            string? status = null)
    {
        // An empty filter includes both Active and Inactive stations for management.
        FilterDefinition<SolarStation> filter =
            Builders<SolarStation>
                .Filter.Empty;


        if (!string.IsNullOrWhiteSpace(status))
        {
            filter =
                Builders<SolarStation>
                    .Filter.Eq(
                        x => x.Status,
                        status.Trim()
                    );
        }


        return await _context.Stations
            .Find(filter)
            .SortBy(x => x.Name)
            .ToListAsync();
    }


    /* =========================================
       GET STATION BY ID
    ========================================= */

    public async Task<SolarStation?>
        GetByIdAsync(
            string id)
    {
        // Invalid ObjectIds are treated as missing stations instead of query errors.
        if (
            string.IsNullOrWhiteSpace(id) ||
            !ObjectId.TryParse(
                id,
                out _
            )
        )
        {
            return null;
        }


        return await _context.Stations
            .Find(
                x => x.Id == id
            )
            .FirstOrDefaultAsync();
    }


    /* =========================================
       CREATE STATION
    ========================================= */

    public async Task<SolarStation>
        CreateAsync(
            StationRequest request)
    {
        var stationName =
            request.Name.Trim();


        var existing =
            await _context.Stations
                .Find(x =>
                    x.Name.ToLower() ==
                    stationName.ToLower()
                )
                .FirstOrDefaultAsync();


        if (existing != null)
        {
            throw new InvalidOperationException(
                "A station with this name already exists."
            );
        }


        ValidateStationRequest(
            request
        );


        // Read coordinates from the backend-issued selection, never from client input.
        var location = _geocoding.ResolveSelection(request.Address, request.LocationToken);

        var now =
            DateTime.UtcNow;


        var station =
            new SolarStation
            {
                Name =
                    stationName,

                Address =
                    request.Address.Trim(),

                Latitude =
                    location.Latitude,

                Longitude =
                    location.Longitude,

                CapacityKw =
                    request.CapacityKw,

                TotalSlots =
                    request.TotalSlots,

                // A newly created station starts with all physical slots available.
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
            .InsertOneAsync(
                station
            );


        return station;
    }


    /* =========================================
       UPDATE STATION
    ========================================= */

    public async Task<SolarStation?>
        UpdateAsync(
            string id,
            StationRequest request)
    {
        var existing =
            await GetByIdAsync(id);


        if (existing == null)
            return null;


        ValidateStationRequest(
            request
        );


        // Unchanged addresses keep existing map coordinates, including legacy stations.
        // A selected suggestion always replaces the location, even for the same address.
        var location = string.IsNullOrWhiteSpace(request.LocationToken)
            && request.Address.Trim() == existing.Address
            ? new ResolvedStationLocation(existing.Address, existing.Latitude, existing.Longitude)
            : _geocoding.ResolveSelection(request.Address, request.LocationToken);

        var stationName =
            request.Name.Trim();


        var duplicate =
            await _context.Stations
                .Find(x =>
                    x.Id != id &&
                    x.Name.ToLower() ==
                    stationName.ToLower()
                )
                .FirstOrDefaultAsync();


        if (duplicate != null)
        {
            throw new InvalidOperationException(
                "A station with this name already exists."
            );
        }


        /*
         * Count current active/future reservations
         * for this station.
         *
         * These reservations represent occupied
         * booking slots.
         */
        var activeReservations =
            await GetActiveReservationCountAsync(
                id
            );


        /*
         * Do not allow Backoffice to reduce the
         * physical slot count below the number of
         * currently reserved slots.
         */
        if (
            request.TotalSlots <
            activeReservations
        )
        {
            throw new InvalidOperationException(
                "Total slots cannot be reduced below the number of active reservations."
            );
        }


        // Retain the larger count so legacy occupied slots are not lost during an edit.
        var bookedSlots = Math.Max(activeReservations,
            Math.Max(0, existing.TotalSlots - existing.AvailableSlots));
        if (request.TotalSlots < bookedSlots)
        {
            throw new InvalidOperationException(
                "Total slots cannot be reduced below the number of booked slots.");
        }

        var newAvailableSlots =
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
                    stationName
                )

                .Set(
                    x => x.Address,
                    request.Address.Trim()
                )

                .Set(
                    x => x.Latitude,
                    location.Latitude
                )

                .Set(
                    x => x.Longitude,
                    location.Longitude
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
                    newAvailableSlots
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


        return await GetByIdAsync(
            id
        );
    }


    /* =========================================
       UPDATE STATION STATUS
    ========================================= */

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
            NormalizeStatus(
                status
            );


        if (
            normalized != "Active" &&
            normalized != "Inactive"
        )
        {
            throw new InvalidOperationException(
                "Invalid station status."
            );
        }


        /*
         * Important Member 3 integration:
         *
         * Backoffice must not deactivate a station
         * when it still has future Pending or
         * Approved reservations.
         */
        if (
            normalized == "Inactive"
        )
        {
            var hasActiveReservations =
                await HasActiveReservationsAsync(
                    id
                );


            if (hasActiveReservations)
            {
                throw new InvalidOperationException(
                    "Cannot deactivate this station while it has active reservations. " +
                    "Complete or cancel the active reservations first."
                );
            }
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


        var filter = Builders<SolarStation>.Filter.Eq(x => x.Id, id);
        if (normalized == "Inactive")
        {
            // Preserve the atomic booked-slot guard in addition to the
            // reservation lookup above. Cross-collection coordination remains
            // necessary to prevent concurrent reservation creation.
            filter &= new BsonDocument("$expr", new BsonDocument("$gte",
                new BsonArray { "$AvailableSlots", "$TotalSlots" }));
        }

        var result = await _context.Stations.UpdateOneAsync(filter, update);
        // Distinguish a removed station from a station blocked by the slot guard.
        if (result.MatchedCount == 0)
        {
            if (await GetByIdAsync(id) == null)
                return null;

            throw new InvalidOperationException(
                "Cannot deactivate this station while it has booked slots. " +
                "Complete or cancel its active reservations first.");
        }


        return await GetByIdAsync(
            id
        );
    }


    /* =========================================
       GET NEARBY ACTIVE STATIONS
    ========================================= */

    public async Task<List<SolarStation>>
        GetNearbyAsync(
            double latitude,
            double longitude,
            double radiusKm)
    {
        if (
            latitude < -90 ||
            latitude > 90
        )
        {
            throw new InvalidOperationException(
                "Latitude must be between -90 and 90."
            );
        }


        if (
            longitude < -180 ||
            longitude > 180
        )
        {
            throw new InvalidOperationException(
                "Longitude must be between -180 and 180."
            );
        }


        if (
            radiusKm <= 0
        )
        {
            throw new InvalidOperationException(
                "Radius must be greater than zero."
            );
        }


        var stations =
            await _context.Stations
                .Find(x =>
                    x.Status == "Active"
                )
                .ToListAsync();


        // Apply the radius to straight-line distance, then show the closest stations first.
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


    /* =========================================
       CHECK IF STATION HAS ACTIVE RESERVATIONS
    ========================================= */

    public async Task<bool>
        HasActiveReservationsAsync(
            string stationId)
    {
        /*
         * A station is considered to have active
         * reservations when:
         *
         * - reservation belongs to this station
         * - reservation is Pending or Approved
         * - reservation is scheduled in the future
         */

        return await _context.Reservations
            .Find(x =>
                x.StationId ==
                    stationId
                &&
                (
                    x.Status ==
                        ReservationStatuses.Pending
                    ||
                    x.Status ==
                        ReservationStatuses.Approved
                )
                &&
                x.ScheduledAt >
                    DateTime.UtcNow
            )
            .AnyAsync();
    }


    /* =========================================
       COUNT ACTIVE RESERVATIONS
    ========================================= */

    public async Task<int>
        GetActiveReservationCountAsync(
            string stationId)
    {
        var count =
            await _context.Reservations
                .CountDocumentsAsync(
                    x =>
                        x.StationId ==
                            stationId
                        &&
                        (
                            x.Status ==
                                ReservationStatuses.Pending
                            ||
                            x.Status ==
                                ReservationStatuses.Approved
                        )
                        &&
                        x.ScheduledAt >
                            DateTime.UtcNow
                );


        return checked(
            (int)count
        );
    }


    /* =========================================
       REFRESH AVAILABLE SLOT COUNT
    ========================================= */

    public async Task<SolarStation?>
        RefreshAvailableSlotsAsync(
            string stationId)
    {
        var station =
            await GetByIdAsync(
                stationId
            );


        if (station == null)
            return null;


        var reservedCount =
            await GetActiveReservationCountAsync(
                stationId
            );


        var available =
            Math.Max(
                0,
                station.TotalSlots -
                reservedCount
            );


        var update =
            Builders<SolarStation>
                .Update

                .Set(
                    x => x.AvailableSlots,
                    available
                )

                .Set(
                    x => x.UpdatedAt,
                    DateTime.UtcNow
                );


        await _context.Stations
            .UpdateOneAsync(
                x => x.Id ==
                    stationId,
                update
            );


        return await GetByIdAsync(
            stationId
        );
    }


    /* =========================================
       STATION REQUEST VALIDATION
    ========================================= */

    private static void
        ValidateStationRequest(
            StationRequest request)
    {
        if (
            string.IsNullOrWhiteSpace(
                request.Name
            )
        )
        {
            throw new InvalidOperationException(
                "Station name is required."
            );
        }


        if (
            string.IsNullOrWhiteSpace(
                request.Address
            )
        )
        {
            throw new InvalidOperationException(
                "Station address is required."
            );
        }


        if (
            request.CapacityKw <= 0
        )
        {
            throw new InvalidOperationException(
                "Station capacity must be greater than zero."
            );
        }


        if (
            request.TotalSlots <= 0
        )
        {
            throw new InvalidOperationException(
                "Total slots must be greater than zero."
            );
        }


        if (
            string.IsNullOrWhiteSpace(
                request.OpeningTime
            )
        )
        {
            throw new InvalidOperationException(
                "Opening time is required."
            );
        }


        if (
            string.IsNullOrWhiteSpace(
                request.ClosingTime
            )
        )
        {
            throw new InvalidOperationException(
                "Closing time is required."
            );
        }


        /*
         * Operating times arrive as strings. Parse them before
         * checking that closing time is later than opening time.
         */
        if (
            !TimeOnly.TryParse(
                request.OpeningTime,
                out var openingTime
            )
        )
        {
            throw new InvalidOperationException(
                "Opening time is invalid."
            );
        }


        if (
            !TimeOnly.TryParse(
                request.ClosingTime,
                out var closingTime
            )
        )
        {
            throw new InvalidOperationException(
                "Closing time is invalid."
            );
        }


        if (
            closingTime <= openingTime
        )
        {
            throw new InvalidOperationException(
                "Closing time must be later than opening time."
            );
        }
    }


    /* =========================================
       NORMALIZE STATUS
    ========================================= */

    private static string NormalizeStatus(
        string status)
    {
        if (
            string.IsNullOrWhiteSpace(
                status
            )
        )
        {
            return string.Empty;
        }


        var value =
            status
                .Trim()
                .ToLowerInvariant();


        return value switch
        {
            "active" =>
                "Active",

            "inactive" =>
                "Inactive",

            _ =>
                status.Trim()
        };
    }


    /* =========================================
       HAVERSINE DISTANCE
    ========================================= */

    private static double
        CalculateDistanceKm(
            double lat1,
            double lon1,
            double lat2,
            double lon2)
    {
        // Haversine calculates great-circle distance; this is not a road travel distance.
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
            )
            *
            Math.Sin(
                latDistance / 2
            )
            +
            Math.Cos(
                DegreesToRadians(
                    lat1
                )
            )
            *
            Math.Cos(
                DegreesToRadians(
                    lat2
                )
            )
            *
            Math.Sin(
                lonDistance / 2
            )
            *
            Math.Sin(
                lonDistance / 2
            );


        var c =
            2
            *
            Math.Atan2(
                Math.Sqrt(a),
                Math.Sqrt(1 - a)
            );


        return earthRadius * c;
    }


    private static double
        DegreesToRadians(
            double degrees)
    {
        return degrees
               *
               Math.PI
               /
               180.0;
    }
}