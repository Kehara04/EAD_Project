/*
 * File: LoginRequest.cs
 * Project: Smart Solar Microgrid Trading System
 *
 * Description:
 * Defines the credentials payload accepted by the
 * authentication endpoint for user sign-in.
 * 
 * References: 
 * YouTube Tutorial – DTO basic explanation
 * https://youtu.be/F9M9bUq-0Z0?si=28-792wMTAQRVxv0
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