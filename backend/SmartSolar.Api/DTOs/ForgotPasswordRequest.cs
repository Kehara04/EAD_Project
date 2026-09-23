using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class ForgotPasswordRequest
{
    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;
}