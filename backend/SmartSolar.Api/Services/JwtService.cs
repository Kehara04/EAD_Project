/*
 * File: JwtService.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Generates JWT access tokens for authenticated users and includes
 * identity, role, and reference claims for request authorization.
 */

using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.Extensions.Options;
using Microsoft.IdentityModel.Tokens;
using SmartSolar.Api.Configuration;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class JwtService
{
    private readonly JwtSettings _settings;

    // Initializes JWT settings from configuration.
    public JwtService(IOptions<JwtSettings> settings)
    {
        _settings = settings.Value;
    }

    // Generates a signed JWT containing user identity and role claims.
    public string GenerateToken(User user)
    {
        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, user.Id ?? string.Empty),
            new(ClaimTypes.Name, user.Name),
            new(ClaimTypes.Email, user.Email),
            new(ClaimTypes.Role, user.Role)
        };

        // Includes the associated Prosumer reference when available.
        if (!string.IsNullOrWhiteSpace(user.ReferenceId))
        {
            claims.Add(new Claim("referenceId", user.ReferenceId));
        }

        // Creates the signing key using the configured JWT secret.
        var key = new SymmetricSecurityKey(
            Encoding.UTF8.GetBytes(_settings.Key)
        );

        var credentials =
            new SigningCredentials(
                key,
                SecurityAlgorithms.HmacSha256
            );

        // Creates the token with its issuer, audience and expiration.
        var token = new JwtSecurityToken(
            issuer: _settings.Issuer,
            audience: _settings.Audience,
            claims: claims,
            expires: DateTime.UtcNow.AddMinutes(
                _settings.ExpiryMinutes
            ),
            signingCredentials: credentials
        );

        // Serializes the JWT into a string for the login response.
        return new JwtSecurityTokenHandler()
            .WriteToken(token);
    }
}