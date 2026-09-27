/*
 * File: ChangePasswordRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description: Defines and validates the password change request.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class ChangePasswordRequest
{
    // Stores the user's current password for verification.
    [Required]
    public string CurrentPassword { get; set; } = string.Empty;

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