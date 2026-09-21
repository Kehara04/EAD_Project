/*
 * File: AuthService.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Handles authentication and password verification.
 */

using MongoDB.Driver;
using SmartSolar.Api.Constants;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;

namespace SmartSolar.Api.Services;

public class AuthService
{
    private readonly MongoDbContext _context;
    private readonly JwtService _jwtService;

    public AuthService(
        MongoDbContext context,
        JwtService jwtService)
    {
        // Store required services.
        _context = context;
        _jwtService = jwtService;
    }

    public async Task<LoginResponse?> LoginAsync(
        LoginRequest request)
    {
        // Normalize email before database lookup.
        var email =
            request.Email
                .Trim()
                .ToLowerInvariant();

        var user =
            await _context.Users
                .Find(x => x.Email == email)
                .FirstOrDefaultAsync();

        // Reject unknown users.
        if (user == null)
            return null;

        // Only active accounts can authenticate.
        if (user.Status !=
            AccountStatuses.Active)
        {
            return null;
        }

        // Verify the submitted password against BCrypt hash.
        var validPassword =
            BCrypt.Net.BCrypt.Verify(
                request.Password,
                user.PasswordHash
            );

        if (!validPassword)
            return null;

        // Return a JWT and safe account information.
        return new LoginResponse
        {
            Token =
                _jwtService.GenerateToken(
                    user
                ),

            UserId =
                user.Id ?? string.Empty,

            Name =
                user.Name,

            Email =
                user.Email,

            Role =
                user.Role,

            ReferenceId =
                user.ReferenceId
        };
    }
}