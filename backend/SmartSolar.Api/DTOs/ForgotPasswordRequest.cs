/*
 * File: ForgotPasswordRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description: Defines and validates password recovery requests.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

// Defines the email information required to request a password reset.
public class ForgotPasswordRequest
{
    // Ensures a valid email address is provided for password recovery.
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;
}