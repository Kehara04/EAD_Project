/*
 * File: AccountService.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Authentication and Account Management
 *
 * Description:
 * Implements account-management business logic for retrieving and updating
 * user profile information and securely changing account passwords.
 *
 * References:
 * YouTube Tutorial – ASP.NET Core Account Management
 * https://youtu.be/DpyfCWqIdGE?si=DbXGOo7rCJdtruZY
 *
 * ASP.NET Core Web API CRUD – Service Class and Dependency Injection
 * https://www.youtube.com/watch?v=iamBmx-6pCs
 *
 */

using MongoDB.Bson;
using MongoDB.Driver;
using SmartSolar.Api.Constants;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class AccountService
{
    private readonly MongoDbContext _context;

    // Constructor: Initializes the MongoDB context for account operations.
    public AccountService(MongoDbContext context)
    {
        _context = context;
    }

    // GetProfileAsync: Retrieves the profile of the currently authenticated user.
    public async Task<UserResponse?> GetProfileAsync(string userId)
    {
        if (!ObjectId.TryParse(userId, out _))
            return null;

        var user = await _context.Users
            .Find(x => x.Id == userId)
            .FirstOrDefaultAsync();

        return user == null ? null : Map(user);
    }

    // UpdateProfileAsync: Updates the authenticated user's editable profile information.
    public async Task<UserResponse?> UpdateProfileAsync(
        string userId,
        UpdateAccountRequest request)
    {
        if (!ObjectId.TryParse(userId, out _))
            return null;

        var user = await _context.Users
            .Find(x => x.Id == userId)
            .FirstOrDefaultAsync();

        if (user == null)
            return null;

        if (user.Status != AccountStatuses.Active)
            throw new InvalidOperationException(
                "This account is not active."
            );

        // Prosumers already have their dedicated profile endpoint.
        if (user.Role == UserRoles.Prosumer)
            throw new InvalidOperationException(
                "Use the Prosumer profile endpoint to update Prosumer details."
            );

        return await UpdateWebUserAsync(user, request);
    }

    // UpdateUserByAdminAsync: Allows an administrator to update a non-prosumer account profile.
    public async Task<UserResponse?> UpdateUserByAdminAsync(
        string userId,
        UpdateAccountRequest request)
    {
        if (!ObjectId.TryParse(userId, out _))
            return null;

        var user = await _context.Users
            .Find(x => x.Id == userId)
            .FirstOrDefaultAsync();

        if (user == null)
            return null;

        if (user.Role == UserRoles.Prosumer)
            throw new InvalidOperationException(
                "Prosumer accounts must be managed through the Prosumer endpoints."
            );

        return await UpdateWebUserAsync(user, request);
    }

    // UpdateWebUserAsync: Applies the actual profile update to a web user record.
    private async Task<UserResponse> UpdateWebUserAsync(
        User user,
        UpdateAccountRequest request)
    {
        var email = request.Email.Trim().ToLowerInvariant();
        var name = request.Name.Trim();

        var duplicate = await _context.Users
            .Find(x =>
                x.Email == email &&
                x.Id != user.Id
            )
            .FirstOrDefaultAsync();

        if (duplicate != null)
            throw new InvalidOperationException(
                "Email address is already in use."
            );

        var now = DateTime.UtcNow;

        var update = Builders<User>.Update
            .Set(x => x.Name, name)
            .Set(x => x.Email, email)
            .Set(x => x.UpdatedAt, now);

        try
        {
            await _context.Users.UpdateOneAsync(
                x => x.Id == user.Id,
                update
            );
        }
        catch (MongoWriteException ex)
            when (ex.WriteError?.Category ==
                  ServerErrorCategory.DuplicateKey)
        {
            throw new InvalidOperationException(
                "Email address is already in use."
            );
        }

        user.Name = name;
        user.Email = email;
        user.UpdatedAt = now;

        return Map(user);
    }

    // ChangePasswordAsync: Validates the current password and updates it to the new value.
    public async Task ChangePasswordAsync(
        string userId,
        ChangePasswordRequest request)
    {
        if (request.NewPassword != request.ConfirmPassword)
            throw new InvalidOperationException(
                "New password and confirmation do not match."
            );

        if (!ObjectId.TryParse(userId, out _))
            throw new InvalidOperationException(
                "Invalid account."
            );

        var user = await _context.Users
            .Find(x => x.Id == userId)
            .FirstOrDefaultAsync();

        if (user == null ||
            user.Status != AccountStatuses.Active)
        {
            throw new InvalidOperationException(
                "Account is not available."
            );
        }

        if (!BCrypt.Net.BCrypt.Verify(
                request.CurrentPassword,
                user.PasswordHash))
        {
            throw new InvalidOperationException(
                "Current password is incorrect."
            );
        }

        if (BCrypt.Net.BCrypt.Verify(
                request.NewPassword,
                user.PasswordHash))
        {
            throw new InvalidOperationException(
                "New password must be different from the current password."
            );
        }

        var newHash = BCrypt.Net.BCrypt.HashPassword(
            request.NewPassword
        );

        // Include the previous hash in the filter so concurrent
        // password changes cannot silently overwrite each other.
        var result = await _context.Users.UpdateOneAsync(
            x => x.Id == userId &&
                 x.PasswordHash == user.PasswordHash,
            Builders<User>.Update
                .Set(x => x.PasswordHash, newHash)
                .Set(x => x.UpdatedAt, DateTime.UtcNow)
        );

        if (result.ModifiedCount != 1)
            throw new InvalidOperationException(
                "Password was changed by another request. Please try again."
            );

        // Invalidate outstanding password-reset requests.
        await _context.PasswordResetTokens.DeleteManyAsync(
            x => x.UserId == userId
        );
    }

    // Map: Converts the database user model to a safe API response.
    private static UserResponse Map(User user)
    {
        return new UserResponse
        {
            Id = user.Id ?? string.Empty,
            Name = user.Name,
            Email = user.Email,
            Role = user.Role,
            Status = user.Status,
            CreatedAt = user.CreatedAt,
            UpdatedAt = user.UpdatedAt
        };
    }
}