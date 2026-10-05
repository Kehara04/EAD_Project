/*
 * File: MongoDbSettings.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Stores the MongoDB connection configuration used by the API
 * to establish a connection and determine the target database.
 */

namespace SmartSolar.Api.Configuration;

public class MongoDbSettings
{
    // Connection string used to connect to the MongoDB instance.
    public string ConnectionString { get; set; } = string.Empty;

    // Name of the MongoDB database used by the application.
    public string DatabaseName { get; set; } = string.Empty;
}