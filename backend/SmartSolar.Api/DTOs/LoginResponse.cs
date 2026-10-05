/*
 * File: LoginResponse.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Returns the JWT token and safe profile information
 * after a successful login request.
 */

namespace SmartSolar.Api.DTOs;

public class LoginResponse
{
    // Stores the JWT token used for authenticated API requests.
    public string Token { get; set; } = string.Empty;

    // Identifies the authenticated user.
    public string UserId { get; set; } = string.Empty;

    // Stores the user's display name.
    public string Name { get; set; } = string.Empty;

    // Stores the user's email address.
    public string Email { get; set; } = string.Empty;

    // Identifies the user's role for role-based access.
    public string Role { get; set; } = string.Empty;

    // Stores the associated profile ID, if applicable.
    public string? ReferenceId { get; set; }
}