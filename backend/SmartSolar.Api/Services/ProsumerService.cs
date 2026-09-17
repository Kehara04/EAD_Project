using MongoDB.Driver;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class ProsumerService
{
    private readonly MongoDbContext _context;

    public ProsumerService(MongoDbContext context)
    {
        _context = context;
    }

    public async Task<Prosumer> RegisterAsync(
        RegisterProsumerRequest request)
    {
        var nic = request.Nic.Trim();
        var email = request.Email.Trim().ToLower();

        var existingProsumer =
            await _context.Prosumers
                .Find(x => x.Nic == nic)
                .FirstOrDefaultAsync();

        if (existingProsumer != null)
            throw new Exception("NIC already registered.");

        var existingUser =
            await _context.Users
                .Find(x => x.Email == email)
                .FirstOrDefaultAsync();

        if (existingUser != null)
            throw new Exception("Email already registered.");

        var prosumer = new Prosumer
        {
            Nic = nic,
            Name = request.Name.Trim(),
            Email = email,
            Phone = request.Phone.Trim(),
            Address = request.Address.Trim(),
            Status = "Pending"
        };

        var user = new User
        {
            Name = prosumer.Name,
            Email = email,
            PasswordHash =
                BCrypt.Net.BCrypt.HashPassword(
                    request.Password
                ),
            Role = "Prosumer",
            Status = "Pending",
            ReferenceId = nic
        };

        await _context.Prosumers.InsertOneAsync(
            prosumer
        );

        try
        {
            await _context.Users.InsertOneAsync(user);
        }
        catch
        {
            await _context.Prosumers.DeleteOneAsync(
                x => x.Nic == nic
            );

            throw;
        }

        return prosumer;
    }

    public async Task<List<Prosumer>> GetAllAsync()
    {
        return await _context.Prosumers
            .Find(_ => true)
            .ToListAsync();
    }

    public async Task<Prosumer?> GetByNicAsync(
        string nic)
    {
        return await _context.Prosumers
            .Find(x => x.Nic == nic)
            .FirstOrDefaultAsync();
    }

    public async Task<Prosumer?> UpdateAsync(
        string nic,
        UpdateProsumerRequest request)
    {
        var prosumer =
            await GetByNicAsync(nic);

        if (prosumer == null)
            return null;

        var email = request.Email.Trim().ToLower();

        var duplicate =
            await _context.Users
                .Find(x =>
                    x.Email == email &&
                    x.ReferenceId != nic)
                .FirstOrDefaultAsync();

        if (duplicate != null)
            throw new Exception(
                "Email is already in use."
            );

        var prosumerUpdate =
            Builders<Prosumer>.Update
                .Set(x => x.Name, request.Name.Trim())
                .Set(x => x.Email, email)
                .Set(x => x.Phone, request.Phone.Trim())
                .Set(x => x.Address, request.Address.Trim());

        await _context.Prosumers.UpdateOneAsync(
            x => x.Nic == nic,
            prosumerUpdate
        );

        var userUpdate =
            Builders<User>.Update
                .Set(x => x.Name, request.Name.Trim())
                .Set(x => x.Email, email);

        await _context.Users.UpdateOneAsync(
            x =>
                x.Role == "Prosumer" &&
                x.ReferenceId == nic,
            userUpdate
        );

        return await GetByNicAsync(nic);
    }

    public async Task RequestDeactivationAsync(
        string nic)
    {
        await _context.Prosumers.UpdateOneAsync(
            x => x.Nic == nic,
            Builders<Prosumer>.Update.Set(
                x => x.Status,
                "DeactivationRequested"
            )
        );
    }

    public async Task ChangeStatusAsync(
        string nic,
        string status)
    {
        await _context.Prosumers.UpdateOneAsync(
            x => x.Nic == nic,
            Builders<Prosumer>.Update.Set(
                x => x.Status,
                status
            )
        );

        var loginStatus =
            status == "Active"
                ? "Active"
                : status == "Pending"
                    ? "Pending"
                    : "Deactivated";

        await _context.Users.UpdateOneAsync(
            x =>
                x.Role == "Prosumer" &&
                x.ReferenceId == nic,
            Builders<User>.Update.Set(
                x => x.Status,
                loginStatus
            )
        );
    }
}