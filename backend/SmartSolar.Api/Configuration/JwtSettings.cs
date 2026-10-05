/*
 * File: JwtSettings.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Authentication and Account Management
 *
 * Description:
 * Defines configuration properties used for JWT creation and validation,
 * including issuer, audience, secret key and token expiry settings.
 *
 * References:
 * YouTube Tutorial – ASP.NET Core JWT Authentication
 * https://youtu.be/w8I32UPEvj8?si=SvUaaO_twbKSWQGn
 *
 * YouTube Tutorial
 * https://youtu.be/gfkTfcpWqAY?si=sv6JJ4g-tkBL1Bi7
 *
 * ASP.NET Core 8 Web API Authentication with JWT
 * https://www.youtube.com/watch?v=rOg3wnsiGRE
 */

namespace SmartSolar.Api.Configuration;

public class JwtSettings
{
    public string Key { get; set; } = string.Empty;
    public string Issuer { get; set; } = string.Empty;
    public string Audience { get; set; } = string.Empty;
    public int ExpiryMinutes { get; set; }
}