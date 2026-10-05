/*
 * File: LoginRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Defines the credentials payload accepted by the
 * authentication endpoint for user sign-in.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class LoginRequest
{
    // Ensures the user provides a valid email address.
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    // Ensures the password is provided for authentication.
    [Required]
    public string Password { get; set; } = string.Empty;
}