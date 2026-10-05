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

    private readonly PasswordResetService _passwordResetService;

    // Constructor: Stores the authentication service used for login requests.
    public AuthController(
        AuthService authService, 
        PasswordResetService passwordResetService)
    {
        // Store authentication service.
        _authService = authService;
        _passwordResetService = passwordResetService;
    }

    [AllowAnonymous]
    [HttpPost("login")]
    // Login: Authenticates a user and returns a JWT when valid credentials are supplied.
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

    [AllowAnonymous]
    [HttpPost("forgot-password")]
    // ForgotPassword: Sends a reset link to the supplied email when the account exists.
    public async Task<IActionResult> ForgotPassword(
        ForgotPasswordRequest request)
    {
        try
        {
            await _passwordResetService.RequestResetAsync(
                request.Email
            );
        }
        catch
        {
            // Do not reveal whether the email is registered to prevent enumeration attacks.
        }

        return Ok(new
        {
            message =
                "If this email is registered, password reset instructions will be sent."
        });
    }

    [AllowAnonymous]
    [HttpPost("reset-password")]
    // ResetPassword: Validates the reset token and updates the password when the request is valid.
    public async Task<IActionResult> ResetPassword(
        ResetPasswordRequest request)
    {
        try
        {
            var success = await _passwordResetService
                .ResetPasswordAsync(
                    request.Token,
                    request.NewPassword,
                    request.ConfirmPassword
                );

            if (!success)
            {
                return BadRequest(new
                {
                    message =
                        "Reset link is invalid, expired, or already used."
                });
            }

            return Ok(new
            {
                message = "Password reset successfully."
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