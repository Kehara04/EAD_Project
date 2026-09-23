using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class UpdateAccountRequest
{
    [Required]
    [StringLength(100, MinimumLength = 2)]
    [RegularExpression(
        @"^[A-Za-z][A-Za-z\s.'-]*$",
        ErrorMessage = "Enter a valid full name."
    )]
    public string Name { get; set; } = string.Empty;

    [Required]
    [EmailAddress]
    [StringLength(150)]
    public string Email { get; set; } = string.Empty;
}