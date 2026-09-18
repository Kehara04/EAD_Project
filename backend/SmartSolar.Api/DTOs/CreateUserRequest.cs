/*
 * File: CreateUserRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Validates information used by Backoffice to create
 * Backoffice and Grid Operator accounts.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class CreateUserRequest
{
    [Required(ErrorMessage = "Full name is required.")]
    [StringLength(
        100,
        MinimumLength = 2,
        ErrorMessage =
            "Name must contain at least 2 characters."
    )]
    [RegularExpression(
        @"^[A-Za-z][A-Za-z\s.'-]*$",
        ErrorMessage =
            "Name contains invalid characters."
    )]
    public string Name { get; set; } =
        string.Empty;


    [Required(ErrorMessage = "Email is required.")]
    [EmailAddress(
        ErrorMessage =
            "Enter a valid email address."
    )]
    public string Email { get; set; } =
        string.Empty;


    [Required(ErrorMessage = "Password is required.")]
    [StringLength(
        64,
        MinimumLength = 8,
        ErrorMessage =
            "Password must contain at least 8 characters."
    )]
    [RegularExpression(
        @"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,64}$",
        ErrorMessage =
            "Password must contain uppercase, lowercase, number and special character with no spaces."
    )]
    public string Password { get; set; } =
        string.Empty;


    [Required(ErrorMessage = "Role is required.")]
    [RegularExpression(
        @"^(Backoffice|GridOperator)$",
        ErrorMessage =
            "Role must be Backoffice or GridOperator."
    )]
    public string Role { get; set; } =
        string.Empty;
}