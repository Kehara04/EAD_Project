/*
 * File: UpdateStationStatusRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Defines the allowed station status values accepted by the
 * station activation and deactivation endpoint.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class UpdateStationStatusRequest
{
    // Restricts station status to Active or Inactive.
    [Required]
    [RegularExpression(
        "^(Active|Inactive)$",
        ErrorMessage =
            "Status must be Active or Inactive."
    )]

    // Stores the station's current status (Active or Inactive).
    public string Status { get; set; } =
        string.Empty;
}