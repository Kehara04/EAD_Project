using MongoDB.Driver;
using SmartSolar.Api.Data;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class SeedService
{
    private readonly MongoDbContext _context;
    private readonly IConfiguration _configuration;

    public SeedService(
        MongoDbContext context,
        IConfiguration configuration)
    {
        _context = context;
        _configuration = configuration;
    }

    public async Task SeedAsync()
    {
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

        email = email.ToLower();

        var existing =
            await _context.Users
                .Find(x => x.Email == email)
                .FirstOrDefaultAsync();

        if (existing != null)
            return;

        await _context.Users.InsertOneAsync(
            new User
            {
                Name = "System Admin",
                Email = email,
                PasswordHash =
                    BCrypt.Net.BCrypt.HashPassword(
                        password
                    ),
                Role = "Backoffice",
                Status = "Active"
            }
        );
    }
}