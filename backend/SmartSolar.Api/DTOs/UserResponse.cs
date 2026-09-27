/*
 * File: UserResponse.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Safe response model returned by user-management endpoints.
 */

namespace SmartSolar.Api.DTOs;

public class UserResponse
{
    // Stores the unique user ID.
    public string Id { get; set; } =
        string.Empty;

     // Stores the user's full name.
    public string Name { get; set; } =
        string.Empty;

    // Stores the user's email address.
    public string Email { get; set; } =
        string.Empty;

    // Identifies the user's role.
    public string Role { get; set; } =
        string.Empty;

    // Indicates the user's current account status.
    public string Status { get; set; } =
        string.Empty;

    // Records when the user account was created.
    public DateTime CreatedAt { get; set; }

    // Records when the user account was last updated.
    public DateTime UpdatedAt { get; set; }
}