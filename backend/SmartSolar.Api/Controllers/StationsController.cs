/*
 * File: StationsController.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Exposes authenticated station endpoints for listing, address search,
 * creation, updates, status changes and nearby station discovery.
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolar.Api.Constants;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Services;

namespace SmartSolar.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class StationsController : ControllerBase
{
    private readonly StationService _service;

    public StationsController(
        StationService service)
    {
        _service = service;
    }


    // Any authenticated client can list stations, optionally restricted by status.
    [Authorize]
    [HttpGet]
    public async Task<IActionResult> GetAll(
        [FromQuery] string? status = null)
    {
        var stations =
            await _service
                .GetAllAsync(status);

        return Ok(stations);
    }


    // Keep the provider key on the server and restrict address lookup to Backoffice.
    [Authorize(Roles = UserRoles.Backoffice)]
    [HttpGet("address-suggestions")]
    public async Task<IActionResult> AddressSuggestions(
        [FromQuery] string? query, [FromServices] StationGeocodingService geocoding,
        CancellationToken cancellationToken)
    {
        try
        {
            return Ok(await geocoding.SearchAsync(query ?? "", cancellationToken));
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
        // Report provider/configuration failures as a retryable service-unavailable response.
        catch (GeocodingUnavailableException ex)
        {
            return StatusCode(StatusCodes.Status503ServiceUnavailable, new { message = ex.Message });
        }
    }

    // Return the saved station details used by the web interface and mobile map.
    [Authorize]
    [HttpGet("{id}")]
    public async Task<IActionResult>
        GetById(
            string id)
    {
        var station =
            await _service
                .GetByIdAsync(id);

        if (station == null)
        {
            return NotFound(new
            {
                message =
                    "Station was not found."
            });
        }

        return Ok(station);
    }


    [Authorize(
        Roles = UserRoles.Backoffice
    )]
    // The service verifies the selected address before saving its coordinates.
    [HttpPost]
    public async Task<IActionResult>
        Create(
            StationRequest request)
    {
        try
        {
            var station =
                await _service
                    .CreateAsync(request);

            return Created(
                $"/api/stations/{station.Id}",
                station
            );
        }
        catch (
            InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message =
                    ex.Message
            });
        }
    }


    [Authorize(
        Roles = UserRoles.Backoffice
    )]
    // Update station details while preserving booking and location safeguards.
    [HttpPut("{id}")]
    public async Task<IActionResult>
        Update(
            string id,
            StationRequest request)
    {
        try
        {
            var station =
                await _service
                    .UpdateAsync(
                        id,
                        request
                    );

            if (station == null)
            {
                return NotFound(new
                {
                    message =
                        "Station was not found."
                });
            }

            return Ok(station);
        }
        catch (
            InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message =
                    ex.Message
            });
        }
    }


    [Authorize(
        Roles = UserRoles.Backoffice
    )]
    // Reservation and booked-slot checks are enforced in the service, not the UI.
    [HttpPatch("{id}/status")]
    public async Task<IActionResult>
        UpdateStatus(
            string id,
            UpdateStationStatusRequest request)
    {
        try
        {
            var station =
                await _service
                    .UpdateStatusAsync(
                        id,
                        request.Status
                    );

            if (station == null)
            {
                return NotFound(new
                {
                    message =
                        "Station was not found."
                });
            }

            return Ok(station);
        }
        catch (
            InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message =
                    ex.Message
            });
        }
    }


    // Radius is measured in kilometres; the service returns only Active stations.
    [Authorize]
    [HttpGet("nearby")]
    public async Task<IActionResult>
        GetNearby(
            [FromQuery] double latitude,
            [FromQuery] double longitude,
            [FromQuery] double radiusKm = 10)
    {
        var stations =
            await _service
                .GetNearbyAsync(
                    latitude,
                    longitude,
                    radiusKm
                );

        return Ok(stations);
    }
}