using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class ChangePasswordRequest
{
    [Required]
    public string CurrentPassword { get; set; } = string.Empty;

    [Required]
    [StringLength(64, MinimumLength = 8)]
    [RegularExpression(
        @"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,64}$",
        ErrorMessage =
            "Password must contain uppercase, lowercase, number and special character with no spaces."
    )]
    public string NewPassword { get; set; } = string.Empty;

    [Required]
    public string ConfirmPassword { get; set; } = string.Empty;
}