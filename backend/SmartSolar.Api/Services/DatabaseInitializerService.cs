/*
 * File: DatabaseInitializerService.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Creates MongoDB indexes required by the authentication
 * and account-management module.
 */

using MongoDB.Driver;
using SmartSolar.Api.Data;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class DatabaseInitializerService
{
    private readonly MongoDbContext _context;

    public DatabaseInitializerService(
        MongoDbContext context)
    {
        // Store MongoDB context.
        _context = context;
    }

    public async Task InitializeAsync()
    {
        // Create a unique index for user email addresses.
        var userEmailIndex =
            new CreateIndexModel<User>(
                Builders<User>.IndexKeys
                    .Ascending(x => x.Email),
                new CreateIndexOptions
                {
                    Unique = true,
                    Name = "UX_Users_Email"
                }
            );

        await _context.Users.Indexes
            .CreateOneAsync(userEmailIndex);

        // Create index to quickly find users by role and status.
        var userRoleStatusIndex =
            new CreateIndexModel<User>(
                Builders<User>.IndexKeys
                    .Ascending(x => x.Role)
                    .Ascending(x => x.Status),
                new CreateIndexOptions
                {
                    Name = "IX_Users_Role_Status"
                }
            );

        await _context.Users.Indexes
            .CreateOneAsync(userRoleStatusIndex);

        // Create a unique Prosumer email index.
        var prosumerEmailIndex =
            new CreateIndexModel<Prosumer>(
                Builders<Prosumer>.IndexKeys
                    .Ascending(x => x.Email),
                new CreateIndexOptions
                {
                    Unique = true,
                    Name = "UX_Prosumers_Email"
                }
            );

        await _context.Prosumers.Indexes
            .CreateOneAsync(
                prosumerEmailIndex
            );

        // Create index for Prosumer account status filtering.
        var prosumerStatusIndex =
            new CreateIndexModel<Prosumer>(
                Builders<Prosumer>.IndexKeys
                    .Ascending(x => x.Status),
                new CreateIndexOptions
                {
                    Name = "IX_Prosumers_Status"
                }
            );

        await _context.Prosumers.Indexes
            .CreateOneAsync(
                prosumerStatusIndex
            );
    }
}