/*
 * File: UpdateAccountRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description: Defines and validates account update requests.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class UpdateAccountRequest
{
    // Validates the user's full name and allowed characters.
    [Required]
    [StringLength(100, MinimumLength = 2)]
    [RegularExpression(
        @"^[A-Za-z][A-Za-z\s.'-]*$",
        ErrorMessage = "Enter a valid full name."
    )]
    public string Name { get; set; } = string.Empty;

    // Ensures a valid email address is provided.
    [Required]
    [EmailAddress]
    [StringLength(150)]
    public string Email { get; set; } = string.Empty;
}