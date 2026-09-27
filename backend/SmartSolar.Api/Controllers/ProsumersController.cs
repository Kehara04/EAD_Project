/*
 * File: ProsumersController.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Provides Prosumer registration, self-service account
 * management and Backoffice lifecycle management endpoints.
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolar.Api.Constants;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Services;

namespace SmartSolar.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class ProsumersController : ControllerBase
{
    private readonly ProsumerService _service;

    public ProsumersController(
        ProsumerService service)
    {
        // Store Prosumer service.
        _service = service;
    }

    [AllowAnonymous]
    [HttpPost("register")]
    public async Task<IActionResult> Register(
        RegisterProsumerRequest request)
    {
        // Register a new Pending Prosumer account.
        try
        {
            var prosumer =
                await _service
                    .RegisterAsync(request);

            return Created(
                $"/api/prosumers/{prosumer.Nic}",
                prosumer
            );
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }

    [Authorize(Roles = UserRoles.Backoffice)]
    [HttpGet]
    public async Task<IActionResult> GetAll(
        [FromQuery] string? status = null)
    {
        // List all Prosumers with optional status filtering.
        var prosumers =
            await _service
                .GetAllAsync(status);

        return Ok(prosumers);
    }

    [Authorize(Roles = UserRoles.Backoffice)]
    [HttpGet("pending")]
    public async Task<IActionResult>
        GetPending()
    {
        // Return registrations awaiting Backoffice approval.
        return Ok(
            await _service.GetAllAsync(
                AccountStatuses.Pending
            )
        );
    }

    [Authorize(Roles = UserRoles.Backoffice)]
    [HttpGet("deactivation-requests")]
    public async Task<IActionResult>
        GetDeactivationRequests()
    {
        // Return Prosumer accounts requesting deactivation.
        return Ok(
            await _service.GetAllAsync(
                AccountStatuses
                    .DeactivationRequested
            )
        );
    }

    [Authorize(Roles = UserRoles.Prosumer)]
    [HttpGet("me")]
    public async Task<IActionResult> GetMe()
    {
        // Read Prosumer NIC stored inside JWT.
        var nic =
            User.FindFirst(
                "referenceId"
            )?.Value;

        if (string.IsNullOrWhiteSpace(nic))
        {
            return Unauthorized(new
            {
                message =
                    "Prosumer reference ID was not found in the token."
            });
        }

        var prosumer =
            await _service
                .GetByNicAsync(nic);

        if (prosumer == null)
        {
            return NotFound(new
            {
                message =
                    "Prosumer profile was not found."
            });
        }

        return Ok(prosumer);
    }

    [Authorize(Roles = UserRoles.Prosumer)]
    [HttpPut("me")]
    public async Task<IActionResult> UpdateMe(
        UpdateProsumerRequest request)
    {
        // Update authenticated Prosumer profile.
        var nic =
            User.FindFirst(
                "referenceId"
            )?.Value;

        if (string.IsNullOrWhiteSpace(nic))
        {
            return Unauthorized();
        }

        try
        {
            var prosumer =
                await _service.UpdateAsync(
                    nic,
                    request
                );

            if (prosumer == null)
            {
                return NotFound(new
                {
                    message =
                        "Prosumer profile was not found."
                });
            }

            return Ok(prosumer);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }

    [Authorize(Roles = UserRoles.Prosumer)]
    [HttpPatch("me/request-deactivation")]
    public async Task<IActionResult>
        RequestDeactivation()
    {
        // Submit a Prosumer account deactivation request.
        var nic =
            User.FindFirst(
                "referenceId"
            )?.Value;

        if (string.IsNullOrWhiteSpace(nic))
            return Unauthorized();

        try
        {
            var prosumer =
                await _service
                    .RequestDeactivationAsync(
                        nic
                    );

            if (prosumer == null)
                return NotFound();

            return Ok(new
            {
                message =
                    "Deactivation request submitted successfully.",

                prosumer
            });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }

    [Authorize(Roles = UserRoles.Backoffice)]
    [HttpPatch("{nic}/activate")]
    public async Task<IActionResult> Activate(
        string nic)
    {
        // Approve a Pending Prosumer registration.
        try
        {
            var prosumer =
                await _service
                    .ActivateAsync(nic);

            if (prosumer == null)
                return NotFound();

            return Ok(new
            {
                message =
                    "Prosumer activated successfully.",

                prosumer
            });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }

    [Authorize(Roles = UserRoles.Backoffice)]
    [HttpPatch("{nic}/deactivate")]
    public async Task<IActionResult> Deactivate(
        string nic)
    {
        // Deactivate an Active Prosumer or approve a Deactivation Requested Prosumer.
        try
        {
            var prosumer =
                await _service
                    .DeactivateAsync(nic);

            if (prosumer == null)
                return NotFound();

            return Ok(new
            {
                message =
                    "Prosumer deactivated successfully.",

                prosumer
            });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }

    [Authorize(Roles = UserRoles.Backoffice)]
    [HttpPatch("{nic}/reactivate")]
    public async Task<IActionResult> Reactivate(
        string nic)
    {
        // Reactivate a previously Deactivated Prosumer.
        try
        {
            var prosumer =
                await _service
                    .ReactivateAsync(nic);

            if (prosumer == null)
                return NotFound();

            return Ok(new
            {
                message =
                    "Prosumer reactivated successfully.",

                prosumer
            });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }
}