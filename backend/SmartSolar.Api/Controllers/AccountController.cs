/*
 * File: AccountController.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Exposes authenticated account endpoints for profile viewing,
 * profile updates, and password management for logged-in users.
 */

using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Services;

namespace SmartSolar.Api.Controllers;

[ApiController]
[Route("api/account")]
[Authorize]
public class AccountController : ControllerBase
{
    private readonly AccountService _accountService;

    // Constructor: Initializes the account service used by the authenticated-user endpoints.
    public AccountController(AccountService accountService)
    {
        _accountService = accountService;
    }

    private string? CurrentUserId =>
        User.FindFirstValue(ClaimTypes.NameIdentifier);

    // GetMyProfile: Returns the currently authenticated user's profile details.
    [HttpGet("me")]
    public async Task<IActionResult> GetMyProfile()
    {
        if (string.IsNullOrEmpty(CurrentUserId))
            return Unauthorized();

        var profile = await _accountService
            .GetProfileAsync(CurrentUserId);

        return profile == null
            ? NotFound(new { message = "Account not found." })
            : Ok(profile);
    }

    // UpdateMyProfile: Updates the currently authenticated user's basic profile information.
    [HttpPut("me")]
    public async Task<IActionResult> UpdateMyProfile(
        UpdateAccountRequest request)
    {
        if (string.IsNullOrEmpty(CurrentUserId))
            return Unauthorized();

        try
        {
            var profile = await _accountService
                .UpdateProfileAsync(CurrentUserId, request);

            return profile == null
                ? NotFound(new { message = "Account not found." })
                : Ok(profile);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }

    // ChangePassword: Updates the signed-in user's password after validating the current password.
    [HttpPost("change-password")]
    public async Task<IActionResult> ChangePassword(
        ChangePasswordRequest request)
    {
        if (string.IsNullOrEmpty(CurrentUserId))
            return Unauthorized();

        try
        {
            await _accountService.ChangePasswordAsync(
                CurrentUserId,
                request
            );

            return Ok(new
            {
                message = "Password changed successfully."
            });
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }
}