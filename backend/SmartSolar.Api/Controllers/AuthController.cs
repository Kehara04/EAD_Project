/*
 * File: AuthController.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Provides login functionality for Backoffice,
 * Grid Operator and Prosumer accounts.
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Services;

namespace SmartSolar.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
public class AuthController : ControllerBase
{
    private readonly AuthService _authService;

    // ---------------------------------------------------------
    // Constructor: Stores the authentication service used for login requests.
    // ---------------------------------------------------------
    public AuthController(
        AuthService authService)
    {
        // Store authentication service.
        _authService = authService;
    }

    [AllowAnonymous]
    [HttpPost("login")]
    // ---------------------------------------------------------
    // Login: Authenticates a user and returns a JWT when valid credentials are supplied.
    // ---------------------------------------------------------
    public async Task<IActionResult> Login(
        LoginRequest request)
    {
        // Validate credentials and issue JWT.
        var result =
            await _authService
                .LoginAsync(request);

        if (result == null)
        {
            return Unauthorized(new
            {
                message =
                    "Invalid credentials or account is not active."
            });
        }

        return Ok(result);
    }
}