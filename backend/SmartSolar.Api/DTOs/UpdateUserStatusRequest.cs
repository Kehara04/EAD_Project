/*
 * File: UpdateUserStatusRequest.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Request model used by Backoffice users to
 * activate or deactivate web application users.
 */

using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class UpdateUserStatusRequest
{
    [Required]
    public string Status { get; set; } =
        string.Empty;
}