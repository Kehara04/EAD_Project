/*
 * File: MongoDbContext.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Provides access to Smart Solar MongoDB Atlas collections.
 */

using Microsoft.Extensions.Options;
using MongoDB.Driver;
using SmartSolar.Api.Configuration;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Data;

public class MongoDbContext
{
    private readonly IMongoDatabase _database;

    // Initializes the MongoDB connection using the configured settings.
    public MongoDbContext(
        IOptions<MongoDbSettings> settings)
    {
        var client = new MongoClient(
            settings.Value.ConnectionString
        );

        _database = client.GetDatabase(
            settings.Value.DatabaseName
        );
    }

    // Provides access to the Users collection.
    public IMongoCollection<User> Users =>
        _database.GetCollection<User>("Users");

    // Provides access to the Prosumers collection.
    public IMongoCollection<Prosumer> Prosumers =>
        _database.GetCollection<Prosumer>("Prosumers");

    // Provides access to the Stations collection.
    public IMongoCollection<SolarStation> Stations =>
        _database.GetCollection<SolarStation>("Stations");

    // Provides access to the energy booking slots collection.
    public IMongoCollection<EnergyBookingSlot> BookingSlots =>
        _database.GetCollection<EnergyBookingSlot>(
            "EnergyBookingSlots"
        );

    // Provides access to the energy reservations collection.
    public IMongoCollection<EnergyReservation> Reservations =>
        _database.GetCollection<EnergyReservation>(
            "EnergyReservations"
        );

    // Provides access to password reset tokens for account recovery.
    public IMongoCollection<PasswordResetToken> PasswordResetTokens =>
        _database.GetCollection<PasswordResetToken>(
            "PasswordResetTokens"
        );
}