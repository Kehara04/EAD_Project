/*
 * File: RegisterProsumerRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Validates information required to register a new Prosumer.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class RegisterProsumerRequest
{
    [Required(ErrorMessage = "NIC is required.")]
    [RegularExpression(
        @"^(?:\d{9}[VvXx]|\d{12})$",
        ErrorMessage =
            "Enter a valid Sri Lankan NIC. Use 9 digits followed by V/X or a 12-digit NIC."
    )]
    public string Nic { get; set; } =
        string.Empty;


    [Required(ErrorMessage = "Name is required.")]
    [StringLength(
        100,
        MinimumLength = 2,
        ErrorMessage =
            "Name must contain at least 2 characters."
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


    [Required(ErrorMessage = "Phone number is required.")]
    [RegularExpression(
        @"^0\d{9}$",
        ErrorMessage =
            "Phone number must contain exactly 10 digits and start with 0."
    )]
    public string Phone { get; set; } =
        string.Empty;


    [Required(ErrorMessage = "Address is required.")]
    [StringLength(
        250,
        MinimumLength = 5,
        ErrorMessage =
            "Address must contain at least 5 characters."
    )]
    public string Address { get; set; } =
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
}