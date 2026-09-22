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
    public string Token { get; set; } = string.Empty;

    public string UserId { get; set; } = string.Empty;

    public string Name { get; set; } = string.Empty;

    public string Email { get; set; } = string.Empty;

    public string Role { get; set; } = string.Empty;

    public string? ReferenceId { get; set; }
}