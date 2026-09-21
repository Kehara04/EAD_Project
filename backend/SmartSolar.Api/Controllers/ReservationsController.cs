using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolar.Api.Constants;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Services;

namespace SmartSolar.Api.Controllers;

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


    /* =========================================
       AVAILABLE SLOTS
    ========================================= */

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


    /* =========================================
       CREATE
    ========================================= */

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


    /* =========================================
       CURRENT PROSUMER RESERVATIONS
    ========================================= */

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


    /* =========================================
       GET SINGLE
    ========================================= */

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


        /*
         * A Prosumer may only view his/her
         * own reservation.
         */
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


    /* =========================================
       UPDATE
    ========================================= */

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


    /* =========================================
       CANCEL
    ========================================= */

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


    /* =========================================
       BACKOFFICE - ALL RESERVATIONS
    ========================================= */

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


    /* =========================================
       BACKOFFICE - APPROVE
    ========================================= */

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


    /* =========================================
       GET PROSUMER ID FROM JWT
    ========================================= */

    private string? GetProsumerId()
    {
        /*
         * Your current project stores
         * reference_id/referenceId in the
         * authenticated session.
         *
         * Supporting both forms makes the
         * endpoint tolerant of the JWT naming
         * already used in your project.
         */

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