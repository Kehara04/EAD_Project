/*
 * File: CreateUserRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Contains information required by Backoffice
 * to create Backoffice or Grid Operator accounts.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class CreateUserRequest
{
    [Required]
    [StringLength(100, MinimumLength = 2)]
    public string Name { get; set; } =
        string.Empty;

    [Required]
    [EmailAddress]
    public string Email { get; set; } =
        string.Empty;

    [Required]
    [MinLength(6)]
    public string Password { get; set; } =
        string.Empty;

    [Required]
    public string Role { get; set; } =
        string.Empty;
}