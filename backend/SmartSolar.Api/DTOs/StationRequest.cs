/*
 * File: StationRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Defines validated station creation and update input, including the
 * protected address selection used to resolve map coordinates.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class StationRequest
{
    // Model validation rejects missing or oversized station names before persistence.
    [Required]
    [StringLength(100, MinimumLength = 2)]
    public string Name { get; set; } =
        string.Empty;

    // A new address must match the selected geocoding suggestion.
    [Required]
    [StringLength(250, MinimumLength = 3)]
    public string Address { get; set; } =
        string.Empty;

    // Validates location selection; optional if the address is unchanged.
    [StringLength(4096)]
    public string? LocationToken { get; set; }

    // Require positive generating capacity in kilowatts.
    [Range(0.1, double.MaxValue)]
    public double CapacityKw { get; set; }

    // Service-level checks additionally prevent reducing capacity below booked slots.
    [Range(1, 1000)]
    public int TotalSlots { get; set; }

    // Specifies the station's opening time.
    [Required]
    public string OpeningTime { get; set; } =
        string.Empty;

    // Specifies the station's closing time.
    [Required]
    public string ClosingTime { get; set; } =
        string.Empty;
}