/*
 * File: RegisterProsumerRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Contains information required for a Prosumer
 * to create a new mobile account.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class RegisterProsumerRequest
{
    [Required]
    [StringLength(20, MinimumLength = 5)]
    public string Nic { get; set; } =
        string.Empty;

    [Required]
    [StringLength(100, MinimumLength = 2)]
    public string Name { get; set; } =
        string.Empty;

    [Required]
    [EmailAddress]
    public string Email { get; set; } =
        string.Empty;

    [Required]
    [StringLength(20)]
    public string Phone { get; set; } =
        string.Empty;

    [StringLength(250)]
    public string Address { get; set; } =
        string.Empty;

    [Required]
    [MinLength(6)]
    public string Password { get; set; } =
        string.Empty;
}