using MongoDB.Driver;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class UserService
{
    private readonly MongoDbContext _context;

    public UserService(MongoDbContext context)
    {
        _context = context;
    }

    public async Task<List<User>> GetAllAsync()
    {
        return await _context.Users
            .Find(_ => true)
            .ToListAsync();
    }

    public async Task<User> CreateAsync(
        CreateUserRequest request)
    {
        var role = request.Role.Trim();

        if (role != "Backoffice" &&
            role != "GridOperator")
        {
            throw new Exception(
                "Only Backoffice or GridOperator users can be created here."
            );
        }

        var email = request.Email.Trim().ToLower();

        var existing = await _context.Users
            .Find(x => x.Email == email)
            .FirstOrDefaultAsync();

        if (existing != null)
            throw new Exception("Email already exists.");

        var user = new User
        {
            Name = request.Name.Trim(),
            Email = email,
            PasswordHash =
                BCrypt.Net.BCrypt.HashPassword(
                    request.Password
                ),
            Role = role,
            Status = "Active"
        };

        await _context.Users.InsertOneAsync(user);

        return user;
    }

    public async Task<bool> UpdateStatusAsync(
        string id,
        string status)
    {
        var validStatuses = new[]
        {
            "Active",
            "Deactivated"
        };

        if (!validStatuses.Contains(status))
            return false;

        var result = await _context.Users
            .UpdateOneAsync(
                x => x.Id == id,
                Builders<User>.Update.Set(
                    x => x.Status,
                    status
                )
            );

        return result.ModifiedCount > 0;
    }
}