/*
 * File: SeedService.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Creates the initial Backoffice administrator account
 * when the system database is first initialized.
 */

using MongoDB.Driver;
using SmartSolar.Api.Constants;
using SmartSolar.Api.Data;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class SeedService
{
    private readonly MongoDbContext _context;
    private readonly IConfiguration _configuration;

    // Initializes the database context and application configuration.
    public SeedService(
        MongoDbContext context,
        IConfiguration configuration)
    {
        // Store required services.
        _context = context;
        _configuration = configuration;
    }

    public async Task SeedAsync()
    {
        // Read administrator credentials from configuration.
        var email =
            _configuration[
                "SeedAdmin:Email"
            ];

        var password =
            _configuration[
                "SeedAdmin:Password"
            ];

        if (string.IsNullOrWhiteSpace(email) ||
            string.IsNullOrWhiteSpace(password))
        {
            return;
        }

        email =
            email
                .Trim()
                .ToLowerInvariant();

        // Do not create duplicate seed administrator.
        var existing =
            await _context.Users
                .Find(x =>
                    x.Email == email)
                .FirstOrDefaultAsync();

        if (existing != null)
            return;

        var now =
            DateTime.UtcNow;

        var admin =
            new User
            {
                Name =
                    "System Admin",

                Email =
                    email,

                PasswordHash =
                    BCrypt.Net.BCrypt
                        .HashPassword(
                            password
                        ),

                Role =
                    UserRoles.Backoffice,

                Status =
                    AccountStatuses.Active,

                CreatedAt =
                    now,

                UpdatedAt =
                    now
            };

        await _context.Users
            .InsertOneAsync(admin);
    }
}