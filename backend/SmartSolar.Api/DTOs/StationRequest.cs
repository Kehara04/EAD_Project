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

    // Protected selection returned by backend address search; required for a new or changed
    // address. An unchanged existing address may omit it to keep its saved coordinates.
    [StringLength(4096)]
    public string? LocationToken { get; set; }

    // Require positive generating capacity in kilowatts.
    [Range(0.1, double.MaxValue)]
    public double CapacityKw { get; set; }

    // Service-level checks additionally prevent reducing capacity below booked slots.
    [Range(1, 1000)]
    public int TotalSlots { get; set; }

    // The service parses both times and requires closing to be later than opening.
    [Required]
    public string OpeningTime { get; set; } =
        string.Empty;

    [Required]
    public string ClosingTime { get; set; } =
        string.Empty;
}