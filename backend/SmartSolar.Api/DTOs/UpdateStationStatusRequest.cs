/*
 * File: UpdateStationStatusRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Defines the allowed station status values accepted by the
 * station activation and deactivation endpoint.
 *
 * References:
 * Microsoft Learn: Model validation in ASP.NET Core MVC and Razor Pages
 * https://learn.microsoft.com/en-us/aspnet/core/mvc/models/validation
 *
 * Microsoft Learn: RequiredAttribute Class
 * https://learn.microsoft.com/dotnet/api/system.componentmodel.dataannotations.requiredattribute
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