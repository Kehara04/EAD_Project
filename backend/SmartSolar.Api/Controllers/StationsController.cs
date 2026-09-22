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