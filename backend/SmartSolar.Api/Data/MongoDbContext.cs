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

    public IMongoCollection<User> Users =>
        _database.GetCollection<User>("Users");

    public IMongoCollection<Prosumer> Prosumers =>
        _database.GetCollection<Prosumer>("Prosumers");

    public IMongoCollection<SolarStation> Stations =>
        _database.GetCollection<SolarStation>("Stations");

    public IMongoCollection<EnergyBookingSlot> BookingSlots =>
        _database.GetCollection<EnergyBookingSlot>(
            "EnergyBookingSlots"
        );

    public IMongoCollection<EnergyReservation> Reservations =>
        _database.GetCollection<EnergyReservation>(
            "EnergyReservations"
        );

    // New collection for password recovery.
    public IMongoCollection<PasswordResetToken> PasswordResetTokens =>
        _database.GetCollection<PasswordResetToken>(
            "PasswordResetTokens"
        );
}