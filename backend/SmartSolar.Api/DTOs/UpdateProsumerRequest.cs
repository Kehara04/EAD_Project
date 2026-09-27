/*
 * File: UpdateProsumerRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Contains editable Prosumer profile information.
 * NIC cannot be changed because it is the primary key.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class UpdateProsumerRequest
{
    // Validates the Prosumer's full name.
    [Required]
    [StringLength(100, MinimumLength = 2)]
    public string Name { get; set; } =
        string.Empty;

    // Ensures a valid email address is provided.
    [Required]
    [EmailAddress]
    public string Email { get; set; } =
        string.Empty;

    // Stores the Prosumer's contact phone number.
    [Required]
    [StringLength(20)]
    public string Phone { get; set; } =
        string.Empty;

    // Stores the Prosumer's residential address.
    [StringLength(250)]
    public string Address { get; set; } =
        string.Empty;
}