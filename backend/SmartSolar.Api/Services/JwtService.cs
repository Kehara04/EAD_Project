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

    public JwtService(IOptions<JwtSettings> settings)
    {
        _settings = settings.Value;
    }

    public string GenerateToken(User user)
    {
        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, user.Id ?? string.Empty),
            new(ClaimTypes.Name, user.Name),
            new(ClaimTypes.Email, user.Email),
            new(ClaimTypes.Role, user.Role)
        };

        if (!string.IsNullOrWhiteSpace(user.ReferenceId))
        {
            claims.Add(new Claim("referenceId", user.ReferenceId));
        }

        var key = new SymmetricSecurityKey(
            Encoding.UTF8.GetBytes(_settings.Key)
        );

        var credentials =
            new SigningCredentials(
                key,
                SecurityAlgorithms.HmacSha256
            );

        var token = new JwtSecurityToken(
            issuer: _settings.Issuer,
            audience: _settings.Audience,
            claims: claims,
            expires: DateTime.UtcNow.AddMinutes(
                _settings.ExpiryMinutes
            ),
            signingCredentials: credentials
        );

        return new JwtSecurityTokenHandler()
            .WriteToken(token);
    }
}