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
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    [Required]
    public string Password { get; set; } = string.Empty;
}