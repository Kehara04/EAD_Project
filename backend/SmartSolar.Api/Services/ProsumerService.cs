/*
 * File: ProsumerService.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Authentication and Account Management
 *
 * Description:
 * Implements Prosumer account business logic including registration,
 * profile updates, account activation and account deactivation handling.
 *
 * References:
 *
 * ASP.NET Core Web API CRUD – Service Class and Dependency Injection
 * https://www.youtube.com/watch?v=iamBmx-6pCs
 *
 */

using MongoDB.Driver;
using SmartSolar.Api.Constants;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class ProsumerService
{
    private readonly MongoDbContext _context;

    // Initializes the MongoDB context.
    public ProsumerService(
        MongoDbContext context)
    {
        
        _context = context;
    }

    // Registers a new Prosumer and creates the associated user account.
    public async Task<Prosumer> RegisterAsync(
        RegisterProsumerRequest request)
    {
        // Normalize NIC and email before storage.
        var nic =
            request.Nic
                .Trim()
                .ToUpperInvariant();

        var email =
            request.Email
                .Trim()
                .ToLowerInvariant();

        // NIC is the Prosumer primary key.
        var existingProsumer =
            await _context.Prosumers
                .Find(x =>
                    x.Nic == nic)
                .FirstOrDefaultAsync();

        if (existingProsumer != null)
        {
            throw new InvalidOperationException(
                "NIC already registered."
            );
        }

        // Email must be unique across all users.
        var existingUser =
            await _context.Users
                .Find(x =>
                    x.Email == email)
                .FirstOrDefaultAsync();

        if (existingUser != null)
        {
            throw new InvalidOperationException(
                "Email already registered."
            );
        }

        var now =
            DateTime.UtcNow;

        var prosumer =
            new Prosumer
            {
                Nic =
                    nic,

                Name =
                    request.Name.Trim(),

                Email =
                    email,

                Phone =
                    request.Phone.Trim(),

                Address =
                    request.Address.Trim(),

                Status =
                    AccountStatuses.Pending,

                CreatedAt =
                    now,

                UpdatedAt =
                    now
            };

        var user =
            new User
            {
                Name =
                    prosumer.Name,

                Email =
                    email,

                PasswordHash =
                    BCrypt.Net.BCrypt
                        .HashPassword(
                            request.Password
                        ),

                Role =
                    UserRoles.Prosumer,

                Status =
                    AccountStatuses.Pending,

                ReferenceId =
                    nic,

                CreatedAt =
                    now,

                UpdatedAt =
                    now
            };

        try
        {
            // Insert Prosumer profile first.
            await _context.Prosumers
                .InsertOneAsync(
                    prosumer
                );

            try
            {
                // Create authentication user record.
                await _context.Users
                    .InsertOneAsync(
                        user
                    );
            }
            catch
            {
                // Roll back Prosumer creation if account creation fails.
                await _context.Prosumers
                    .DeleteOneAsync(
                        x => x.Nic == nic
                    );

                throw;
            }
        }
        catch (MongoWriteException ex)
            when (
                ex.WriteError?.Category ==
                ServerErrorCategory.DuplicateKey)
        {
            throw new InvalidOperationException(
                "NIC or email is already registered."
            );
        }

        return prosumer;
    }

    // Retrieves all Prosumers, optionally filtered by account status.
    public async Task<List<Prosumer>>
        GetAllAsync(
            string? status = null)
    {
        // Return all Prosumers or filter by account status.
        FilterDefinition<Prosumer> filter =
            Builders<Prosumer>.Filter.Empty;

        if (!string.IsNullOrWhiteSpace(status))
        {
            filter =
                Builders<Prosumer>.Filter.Eq(
                    x => x.Status,
                    status
                );
        }

        return await _context.Prosumers
            .Find(filter)
            .SortBy(x => x.Name)
            .ToListAsync();
    }

    // Retrieves a Prosumer using their NIC.
    public async Task<Prosumer?> GetByNicAsync(
        string nic)
    {
        // Normalize NIC before searching.
        var normalizedNic =
            nic.Trim().ToUpperInvariant();

        return await _context.Prosumers
            .Find(x =>
                x.Nic == normalizedNic)
            .FirstOrDefaultAsync();
    }

    // Updates the Prosumer profile and associated user details.
    public async Task<Prosumer?> UpdateAsync(
        string nic,
        UpdateProsumerRequest request)
    {
        // Retrieve the Prosumer being updated.
        var prosumer =
            await GetByNicAsync(nic);

        if (prosumer == null)
            return null;

        if (prosumer.Status ==
            AccountStatuses.Deactivated)
        {
            throw new InvalidOperationException(
                "A deactivated Prosumer cannot modify the profile."
            );
        }

        var email =
            request.Email
                .Trim()
                .ToLowerInvariant();

        // Prevent using another user's email address.
        var duplicateUser =
            await _context.Users
                .Find(x =>
                    x.Email == email &&
                    x.ReferenceId != prosumer.Nic)
                .FirstOrDefaultAsync();

        if (duplicateUser != null)
        {
            throw new InvalidOperationException(
                "Email is already in use."
            );
        }

        var now =
            DateTime.UtcNow;

        var prosumerUpdate =
            Builders<Prosumer>.Update
                .Set(
                    x => x.Name,
                    request.Name.Trim()
                )
                .Set(
                    x => x.Email,
                    email
                )
                .Set(
                    x => x.Phone,
                    request.Phone.Trim()
                )
                .Set(
                    x => x.Address,
                    request.Address.Trim()
                )
                .Set(
                    x => x.UpdatedAt,
                    now
                );

        await _context.Prosumers
            .UpdateOneAsync(
                x =>
                    x.Nic ==
                    prosumer.Nic,
                prosumerUpdate
            );

        var userUpdate =
            Builders<User>.Update
                .Set(
                    x => x.Name,
                    request.Name.Trim()
                )
                .Set(
                    x => x.Email,
                    email
                )
                .Set(
                    x => x.UpdatedAt,
                    now
                );

        await _context.Users
            .UpdateOneAsync(
                x =>
                    x.Role ==
                    UserRoles.Prosumer &&
                    x.ReferenceId ==
                    prosumer.Nic,
                userUpdate
            );

        return await GetByNicAsync(
            prosumer.Nic
        );
    }

    // Records a Prosumer's account deactivation request.
    public async Task<Prosumer?>
        RequestDeactivationAsync(
            string nic)
    {
        // Retrieve Prosumer before validating lifecycle state.
        var prosumer =
            await GetByNicAsync(nic);

        if (prosumer == null)
            return null;

        if (prosumer.Status !=
            AccountStatuses.Active)
        {
            throw new InvalidOperationException(
                "Only an active Prosumer can request account deactivation."
            );
        }

        var now =
            DateTime.UtcNow;

        var update =
            Builders<Prosumer>.Update
                .Set(
                    x => x.Status,
                    AccountStatuses
                        .DeactivationRequested
                )
                .Set(
                    x =>
                        x.DeactivationRequestedAt,
                    now
                )
                .Set(
                    x => x.UpdatedAt,
                    now
                );

        await _context.Prosumers
            .UpdateOneAsync(
                x => x.Nic == prosumer.Nic,
                update
            );

        return await GetByNicAsync(
            prosumer.Nic
        );
    }

    // Activates a newly registered Prosumer account.
    public async Task<Prosumer?> ActivateAsync(
        string nic)
    {
        // Only newly registered Pending accounts can be activated.
        var prosumer =
            await GetByNicAsync(nic);

        if (prosumer == null)
            return null;

        if (prosumer.Status !=
            AccountStatuses.Pending)
        {
            throw new InvalidOperationException(
                "Only a pending Prosumer can be activated."
            );
        }

        return await SetAccountStatusAsync(
            prosumer,
            AccountStatuses.Active
        );
    }

    // Deactivates an eligible Prosumer account.
    public async Task<Prosumer?>
        DeactivateAsync(
            string nic)
    {
        
        
        var prosumer =
            await GetByNicAsync(nic);

        if (prosumer == null)
            return null;

        if (prosumer.Status !=
                AccountStatuses.Active &&
            prosumer.Status !=
                AccountStatuses
                    .DeactivationRequested)
        {
            throw new InvalidOperationException(
                "Only an active Prosumer or a Prosumer with a pending deactivation request can be deactivated."
            );
        }

        return await SetAccountStatusAsync(
            prosumer,
            AccountStatuses.Deactivated
        );
    }

     // Reactivates a previously deactivated Prosumer account.
    public async Task<Prosumer?>
        ReactivateAsync(
            string nic)
    {
        
        
        var prosumer =
            await GetByNicAsync(nic);

        if (prosumer == null)
            return null;

        if (prosumer.Status !=
            AccountStatuses.Deactivated)
        {
            throw new InvalidOperationException(
                "Only a deactivated Prosumer can be reactivated."
            );
        }

        return await SetAccountStatusAsync(
            prosumer,
            AccountStatuses.Active
        );
    }

    // Updates the Prosumer and authentication account statuses.
    private async Task<Prosumer>
        SetAccountStatusAsync(
            Prosumer prosumer,
            string status)
    {
        // Update both profile status and authentication status.
        var now =
            DateTime.UtcNow;

        var prosumerUpdate =
            Builders<Prosumer>.Update
                .Set(
                    x => x.Status,
                    status
                )
                .Set(
                    x => x.UpdatedAt,
                    now
                );

        if (status ==
            AccountStatuses.Deactivated)
        {
            prosumerUpdate =
                prosumerUpdate.Set(
                    x => x.DeactivatedAt,
                    now
                );
        }

        if (status ==
            AccountStatuses.Active &&
            prosumer.Status ==
            AccountStatuses.Deactivated)
        {
            prosumerUpdate =
                prosumerUpdate
                    .Set(
                        x => x.ReactivatedAt,
                        now
                    )
                    .Set(
                        x =>
                            x.DeactivationRequestedAt,
                        null
                    )
                    .Set(
                        x => x.DeactivatedAt,
                        null
                    );
        }

        await _context.Prosumers
            .UpdateOneAsync(
                x =>
                    x.Nic ==
                    prosumer.Nic,
                prosumerUpdate
            );

        // Authentication account becomes Active only
        // when Prosumer account is Active.
        var userStatus =
            status ==
            AccountStatuses.Active
                ? AccountStatuses.Active
                : status ==
                  AccountStatuses.Pending
                    ? AccountStatuses.Pending
                    : AccountStatuses.Deactivated;

        await _context.Users
            .UpdateOneAsync(
                x =>
                    x.Role ==
                    UserRoles.Prosumer &&
                    x.ReferenceId ==
                    prosumer.Nic,

                Builders<User>.Update
                    .Set(
                        x => x.Status,
                        userStatus
                    )
                    .Set(
                        x => x.UpdatedAt,
                        now
                    )
            );

        return (
            await GetByNicAsync(
                prosumer.Nic
            )
        )!;
    }
}