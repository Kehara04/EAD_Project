using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
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
        _service = service;
    }

    [HttpPost("register")]
    public async Task<IActionResult> Register(
        RegisterProsumerRequest request)
    {
        try
        {
            var prosumer =
                await _service.RegisterAsync(request);

            return Ok(prosumer);
        }
        catch (Exception ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }

    [Authorize(Roles = "Backoffice")]
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        return Ok(await _service.GetAllAsync());
    }

    [Authorize(Roles = "Prosumer")]
    [HttpGet("me")]
    public async Task<IActionResult> GetMe()
    {
        var nic =
            User.FindFirst("referenceId")?.Value;

        if (string.IsNullOrEmpty(nic))
            return Unauthorized();

        var prosumer =
            await _service.GetByNicAsync(nic);

        return prosumer == null
            ? NotFound()
            : Ok(prosumer);
    }

    [Authorize(Roles = "Prosumer")]
    [HttpPut("me")]
    public async Task<IActionResult> UpdateMe(
        UpdateProsumerRequest request)
    {
        var nic =
            User.FindFirst("referenceId")?.Value;

        if (string.IsNullOrEmpty(nic))
            return Unauthorized();

        try
        {
            var result =
                await _service.UpdateAsync(
                    nic,
                    request
                );

            return result == null
                ? NotFound()
                : Ok(result);
        }
        catch (Exception ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }

    [Authorize(Roles = "Prosumer")]
    [HttpPatch("me/request-deactivation")]
    public async Task<IActionResult>
        RequestDeactivation()
    {
        var nic =
            User.FindFirst("referenceId")?.Value;

        if (string.IsNullOrEmpty(nic))
            return Unauthorized();

        await _service
            .RequestDeactivationAsync(nic);

        return Ok(new
        {
            message =
                "Deactivation request submitted."
        });
    }

    [Authorize(Roles = "Backoffice")]
    [HttpPatch("{nic}/activate")]
    public async Task<IActionResult> Activate(
        string nic)
    {
        await _service.ChangeStatusAsync(
            nic,
            "Active"
        );

        return Ok(new
        {
            message = "Prosumer activated."
        });
    }

    [Authorize(Roles = "Backoffice")]
    [HttpPatch("{nic}/deactivate")]
    public async Task<IActionResult> Deactivate(
        string nic)
    {
        await _service.ChangeStatusAsync(
            nic,
            "Deactivated"
        );

        return Ok(new
        {
            message = "Prosumer deactivated."
        });
    }

    [Authorize(Roles = "Backoffice")]
    [HttpPatch("{nic}/reactivate")]
    public async Task<IActionResult> Reactivate(
        string nic)
    {
        await _service.ChangeStatusAsync(
            nic,
            "Active"
        );

        return Ok(new
        {
            message = "Prosumer reactivated."
        });
    }
}