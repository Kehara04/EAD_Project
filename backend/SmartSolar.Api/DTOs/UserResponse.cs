/*
 * File: UserResponse.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Safe response model returned by user-management endpoints.
 */

namespace SmartSolar.Api.DTOs;

public class UserResponse
{
    public string Id { get; set; } =
        string.Empty;

    public string Name { get; set; } =
        string.Empty;

    public string Email { get; set; } =
        string.Empty;

    public string Role { get; set; } =
        string.Empty;

    public string Status { get; set; } =
        string.Empty;

    public DateTime CreatedAt { get; set; }

    public DateTime UpdatedAt { get; set; }
}