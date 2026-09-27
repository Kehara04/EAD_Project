/*
 * File: ResetPasswordRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description: Defines and validates password reset requests.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class ResetPasswordRequest
{
    // Stores the password reset token used to verify the request.
    [Required]
    public string Token { get; set; } = string.Empty;

    // Validates the new password against the required security rules.
    [Required]
    [StringLength(64, MinimumLength = 8)]
    [RegularExpression(
        @"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,64}$",
        ErrorMessage =
            "Password must contain uppercase, lowercase, number and special character with no spaces."
    )]
    public string NewPassword { get; set; } = string.Empty;

    // Stores the confirmation password for matching with the new password.
    [Required]
    public string ConfirmPassword { get; set; } = string.Empty;
}