/*
 * File: UserService.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Authentication and Account Management
 *
 * Description:
 * Contains service-layer logic for creating, retrieving, updating and
 * managing Backoffice and Grid Operator user accounts.
 *
 * References:
 *
 * ASP.NET Core Web API CRUD – Service Class and Dependency Injection
 * https://www.youtube.com/watch?v=iamBmx-6pCs
 *
 * ASP.NET Core Web API Services Tutorial
 * https://dotnettutorials.net/lesson/services-in-asp-net-core-web-api/
 */

using MongoDB.Bson;
using MongoDB.Driver;
using SmartSolar.Api.Constants;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class UserService
{
    private readonly MongoDbContext _context;

    //Initializes the MongoDB context for user operations.
    public UserService(
        MongoDbContext context)
    {
        // Store MongoDB context.
        _context = context;
    }

    //Retrieves all system users in a safe response format.
    public async Task<List<UserResponse>>
        GetAllAsync()
    {
        // Retrieve users without exposing password hashes.
        var users =
            await _context.Users
                .Find(_ => true)
                .SortBy(x => x.Name)
                .ToListAsync();

        return users.Select(
            MapToResponse
        ).ToList();
    }

    //Finds a single user using a valid MongoDB ObjectId.
    public async Task<UserResponse?> GetByIdAsync(
        string id)
    {
        // Validate MongoDB ObjectId before database query.
        if (!ObjectId.TryParse(id, out _))
            return null;

        var user =
            await _context.Users
                .Find(x => x.Id == id)
                .FirstOrDefaultAsync();

        return user == null
            ? null
            : MapToResponse(user);
    }

    //Creates a new Backoffice or Grid Operator account.
    public async Task<UserResponse> CreateAsync(
        CreateUserRequest request)
    {
        // Normalize and validate requested role.
        var role =
            request.Role.Trim();

        if (role != UserRoles.Backoffice &&
            role != UserRoles.GridOperator)
        {
            throw new InvalidOperationException(
                "Only Backoffice and GridOperator users can be created here."
            );
        }

        var email =
            request.Email
                .Trim()
                .ToLowerInvariant();

        // Check for an existing account using this email.
        var existing =
            await _context.Users
                .Find(x =>
                    x.Email == email)
                .FirstOrDefaultAsync();

        if (existing != null)
        {
            throw new InvalidOperationException(
                "Email already exists."
            );
        }

        var user =
            new User
            {
                Name =
                    request.Name.Trim(),

                Email =
                    email,

                PasswordHash =
                    BCrypt.Net.BCrypt
                        .HashPassword(
                            request.Password
                        ),

                Role =
                    role,

                Status =
                    AccountStatuses.Active,

                CreatedAt =
                    DateTime.UtcNow,

                UpdatedAt =
                    DateTime.UtcNow
            };

        try
        {
            // Store user in MongoDB Atlas.
            await _context.Users
                .InsertOneAsync(user);
        }
        catch (MongoWriteException ex)
            when (
                ex.WriteError?.Category ==
                ServerErrorCategory.DuplicateKey)
        {
            throw new InvalidOperationException(
                "Email already exists."
            );
        }

        return MapToResponse(user);
    }

    //Updates the active status of a user account.
    public async Task<UserResponse?>
        UpdateStatusAsync(
            string id,
            string status)
    {

        if (!ObjectId.TryParse(id, out _))
            return null;

     
        if (status != AccountStatuses.Active &&
            status != AccountStatuses.Deactivated)
        {
            throw new InvalidOperationException(
                "Status must be Active or Deactivated."
            );
        }

        var user =
            await _context.Users
                .Find(x => x.Id == id)
                .FirstOrDefaultAsync();

        if (user == null)
            return null;

    
        if (user.Role == UserRoles.Prosumer)
        {
            throw new InvalidOperationException(
                "Prosumer status must be managed using the Prosumer management endpoints."
            );
        }

        var update =
            Builders<User>.Update
                .Set(
                    x => x.Status,
                    status
                )
                .Set(
                    x => x.UpdatedAt,
                    DateTime.UtcNow
                );

        await _context.Users
            .UpdateOneAsync(
                x => x.Id == id,
                update
            );

        user.Status = status;
        user.UpdatedAt = DateTime.UtcNow;

        return MapToResponse(user);
    }

    //Converts the MongoDB entity into a safe API response model.
    private static UserResponse MapToResponse(
        User user)
    {
       
        return new UserResponse
        {
            Id =
                user.Id ?? string.Empty,

            Name =
                user.Name,

            Email =
                user.Email,

            Role =
                user.Role,

            Status =
                user.Status,

            CreatedAt =
                user.CreatedAt,

            UpdatedAt =
                user.UpdatedAt
        };
    }
}