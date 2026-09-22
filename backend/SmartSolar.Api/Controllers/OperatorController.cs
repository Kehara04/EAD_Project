/*
 * File: OperatorController.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Exposes the QR generation, QR verification,
 * energy-transfer completion, and operator dashboard
 * endpoints for the Smart Solar trading system.
 */

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
public class OperatorController
    : ControllerBase
{
    private readonly QrService _qrService;
    private readonly OperatorService _operatorService;


    public OperatorController(
        QrService qrService,
        OperatorService operatorService)
    {
        _qrService = qrService;
        _operatorService = operatorService;
    }


    /* =========================================
       GET QR PAYLOAD – PROSUMER ONLY
    ========================================= */

    /// <summary>
    /// Generates a signed QR payload for an approved reservation.
    /// Only the Prosumer who owns the reservation may call this.
    /// </summary>
    [Authorize(Roles = UserRoles.Prosumer)]
    [HttpGet("reservations/{id}/qr")]
    public async Task<IActionResult>
        GetQrPayload(string id)
    {
        // Resolve the Prosumer NIC from the JWT claims.
        var prosumerId = GetProsumerId();


        if (string.IsNullOrWhiteSpace(prosumerId))
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
            var payload =
                await _qrService
                    .GenerateQrPayloadAsync(
                        id,
                        prosumerId
                    );


            return Ok(payload);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(
                new
                {
                    message = ex.Message
                }
            );
        }
    }


    /* =========================================
       VERIFY QR – GRID OPERATOR ONLY
    ========================================= */

    /// <summary>
    /// Verifies a QR payload scanned by the Grid Operator.
    /// Always returns HTTP 200; isValid in the body carries the result.
    /// </summary>
    [Authorize(Roles = UserRoles.GridOperator)]
    [HttpPost("verify")]
    public async Task<IActionResult>
        VerifyQr(
            [FromBody] VerifyQrRequest request)
    {
        var response =
            await _qrService.VerifyQrAsync(
                request
            );


        return Ok(response);
    }


    /* =========================================
       COMPLETE ENERGY TRANSFER – GRID OPERATOR ONLY
    ========================================= */

    /// <summary>
    /// Finalises an energy transfer for an approved reservation.
    /// Sets the reservation status to Completed and records the timestamp.
    /// </summary>
    [Authorize(Roles = UserRoles.GridOperator)]
    [HttpPost("complete")]
    public async Task<IActionResult>
        CompleteTransfer(
            [FromBody] CompleteTransferRequest request)
    {
        try
        {
            var result =
                await _operatorService
                    .CompleteTransferAsync(request);


            return Ok(result);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(
                new
                {
                    message = ex.Message
                }
            );
        }
    }


    /* =========================================
       OPERATOR DASHBOARD – GRID OPERATOR ONLY
    ========================================= */

    /// <summary>
    /// Returns today's reservation counts and per-station
    /// completion statistics for the operator dashboard.
    /// </summary>
    [Authorize(Roles = UserRoles.GridOperator)]
    [HttpGet("dashboard")]
    public async Task<IActionResult>
        Dashboard()
    {
        var stats =
            await _operatorService
                .GetDashboardAsync();


        return Ok(stats);
    }


    /* =========================================
       HELPER – EXTRACT PROSUMER ID FROM JWT
    ========================================= */

    /// <summary>
    /// Reads the prosumer NIC stored in the JWT reference_id claim.
    /// Supports both camelCase and snake_case claim names for
    /// compatibility with the existing auth implementation.
    /// </summary>
    private string? GetProsumerId()
    {
        return
            User.FindFirst("referenceId")?.Value
            ??
            User.FindFirst("reference_id")?.Value
            ??
            User.FindFirst(ClaimTypes.NameIdentifier)?.Value;
    }
}
