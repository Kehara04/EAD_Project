/*
 * File: ReservationsController.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 * Description: Provides secured REST endpoints for reservation
 *              creation, updates, cancellation, approval, and retrieval.
 */
using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolar.Api.Constants;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Services;

namespace SmartSolar.Api.Controllers;

// Defines the reservation API and requires authentication for all endpoints.
[ApiController]
[Route("api/[controller]")]
[Authorize]
public class ReservationsController
    : ControllerBase
{
    private readonly ReservationService
        _service;


    public ReservationsController(
        ReservationService service)
    {
        _service = service;
    }

    // Retrieves all reservations for authorized Backoffice users.
    [HttpGet("available-slots")]
    public async Task<IActionResult>
        AvailableSlots(
            [FromQuery]
            string stationId,

            [FromQuery]
            DateTime scheduledAt)
    {
        try
        {
            var result =
                await _service
                    .GetAvailableSlotsAsync(
                        stationId,
                        scheduledAt
                    );

            return Ok(result);
        }
        catch (
            InvalidOperationException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                }
            );
        }
    }

    // Creates a new energy reservation for an authenticated Prosumer
    [Authorize(
        Roles = UserRoles.Prosumer
    )]
    [HttpPost]
    public async Task<IActionResult>
        Create(
            CreateReservationRequest request)
    {
        var prosumerId =
            GetProsumerId();


        if (
            string.IsNullOrWhiteSpace(
                prosumerId
            )
        )
        {
            return Unauthorized(
                new
                {
                    message =
                        "Prosumer identity could not be determined."
                }
            );
        }


        try
        {
            var result =
                await _service.CreateAsync(
                    prosumerId,
                    request
                );


            return Created(
                $"/api/reservations/{result.Id}",
                result
            );
        }
        catch (
            InvalidOperationException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                }
            );
        }
    }

    // Retrieves the authenticated Prosumer's reservation history.
    [Authorize(
        Roles = UserRoles.Prosumer
    )]
    [HttpGet("my")]
    public async Task<IActionResult>
        MyReservations(
            [FromQuery]
            string? status = null,

            [FromQuery]
            string? search = null)
    {
        var prosumerId =
            GetProsumerId();


        if (
            string.IsNullOrWhiteSpace(
                prosumerId
            )
        )
        {
            return Unauthorized();
        }


        var result =
            await _service
                .GetForProsumerAsync(
                    prosumerId,
                    status,
                    search
                );


        return Ok(result);
    }

    // Retrieves the details of a reservation using its identifier.
    [HttpGet("{id}")]
    public async Task<IActionResult>
        GetById(
            string id)
    {
        var result =
            await _service
                .GetByIdAsync(id);


        if (result == null)
        {
            return NotFound(
                new
                {
                    message =
                        "Reservation was not found."
                }
            );
        }
        if (
            User.IsInRole(
                UserRoles.Prosumer
            )
        )
        {
            var prosumerId =
                GetProsumerId();


            if (
                result.ProsumerId !=
                prosumerId
            )
            {
                return Forbid();
            }
        }


        return Ok(result);
    }

    // Updates an existing reservation belonging to the authenticated Prosumer.
    [Authorize(
        Roles = UserRoles.Prosumer
    )]
    [HttpPut("{id}")]
    public async Task<IActionResult>
        Update(
            string id,
            UpdateReservationRequest request)
    {
        var prosumerId =
            GetProsumerId();


        if (
            string.IsNullOrWhiteSpace(
                prosumerId
            )
        )
        {
            return Unauthorized();
        }


        try
        {
            var result =
                await _service
                    .UpdateAsync(
                        id,
                        prosumerId,
                        request
                    );


            if (result == null)
            {
                return NotFound(
                    new
                    {
                        message =
                            "Reservation was not found."
                    }
                );
            }


            return Ok(result);
        }
        catch (
            InvalidOperationException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                }
            );
        }
    }

    // Cancels an existing reservation belonging to the authenticated Prosumer.
    [Authorize(
        Roles = UserRoles.Prosumer
    )]
    [HttpPatch("{id}/cancel")]
    public async Task<IActionResult>
        Cancel(
            string id)
    {
        var prosumerId =
            GetProsumerId();


        if (
            string.IsNullOrWhiteSpace(
                prosumerId
            )
        )
        {
            return Unauthorized();
        }


        try
        {
            var result =
                await _service
                    .CancelAsync(
                        id,
                        prosumerId
                    );


            if (result == null)
            {
                return NotFound(
                    new
                    {
                        message =
                            "Reservation was not found."
                    }
                );
            }


            return Ok(result);
        }
        catch (
            InvalidOperationException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                }
            );
        }
    }

    // Retrieves all reservations for authorized Backoffice users.
    [Authorize(
        Roles = UserRoles.Backoffice
    )]
    [HttpGet]
    public async Task<IActionResult>
        GetAll(
            [FromQuery]
            string? status = null,

            [FromQuery]
            string? search = null)
    {
        return Ok(
            await _service
                .GetAllAsync(
                    status,
                    search
                )
        );
    }

    // Allows authorized Backoffice users to approve reservations.
    [Authorize(
        Roles = UserRoles.Backoffice
    )]
    [HttpPatch("{id}/approve")]
    public async Task<IActionResult>
        Approve(
            string id)
    {
        try
        {
            var result =
                await _service
                    .ApproveAsync(id);


            if (result == null)
            {
                return NotFound(
                    new
                    {
                        message =
                            "Reservation was not found."
                    }
                );
            }


            return Ok(result);
        }
        catch (
            InvalidOperationException ex)
        {
            return BadRequest(
                new
                {
                    message =
                        ex.Message
                }
            );
        }
    }

    // Extracts the Prosumer identifier from the authenticated JWT claims.
    private string? GetProsumerId()
    {
        return
            User.FindFirst(
                "referenceId"
            )?.Value
            ??
            User.FindFirst(
                "reference_id"
            )?.Value
            ??
            User.FindFirst(
                ClaimTypes.NameIdentifier
            )?.Value;
    }
}