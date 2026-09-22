/*
 * File: JwtSettings.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Stores the token configuration values used to
 * validate JWTs during authentication and authorization.
 */

namespace SmartSolar.Api.Configuration;

public class JwtSettings
{
    public string Key { get; set; } = string.Empty;
    public string Issuer { get; set; } = string.Empty;
    public string Audience { get; set; } = string.Empty;
    public int ExpiryMinutes { get; set; }
}