/*
 * File: Program.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Configures the SmartSolar Web API, MongoDB connection,
 * JWT authentication, CORS, Swagger, dependency injection,
 * and initial Backoffice account seeding.
 */

using System.Text;
using DotNetEnv;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using Microsoft.OpenApi;
using SmartSolar.Api.Configuration;
using SmartSolar.Api.Data;
using SmartSolar.Api.Services;


// ---------------------------------------------------------
// Load environment variables from the .env file
// ---------------------------------------------------------

Env.Load();


// ---------------------------------------------------------
// Create WebApplication builder
// ---------------------------------------------------------

var builder = WebApplication.CreateBuilder(args);


// ---------------------------------------------------------
// Read values from .env and add them to ASP.NET configuration
// ---------------------------------------------------------

var mongoConnectionString =
    Environment.GetEnvironmentVariable(
        "MONGODB_CONNECTION_STRING"
    );

var mongoDatabaseName =
    Environment.GetEnvironmentVariable(
        "MONGODB_DATABASE_NAME"
    );

var jwtKey =
    Environment.GetEnvironmentVariable(
        "JWT_KEY"
    );

var jwtIssuer =
    Environment.GetEnvironmentVariable(
        "JWT_ISSUER"
    );

var jwtAudience =
    Environment.GetEnvironmentVariable(
        "JWT_AUDIENCE"
    );

var jwtExpiryMinutes =
    Environment.GetEnvironmentVariable(
        "JWT_EXPIRY_MINUTES"
    );

var seedAdminEmail =
    Environment.GetEnvironmentVariable(
        "SEED_ADMIN_EMAIL"
    );

var seedAdminPassword =
    Environment.GetEnvironmentVariable(
        "SEED_ADMIN_PASSWORD"
    );


// ---------------------------------------------------------
// Validate required environment variables
// ---------------------------------------------------------

if (string.IsNullOrWhiteSpace(mongoConnectionString))
{
    throw new InvalidOperationException(
        "MONGODB_CONNECTION_STRING is missing from the .env file."
    );
}

if (string.IsNullOrWhiteSpace(mongoDatabaseName))
{
    throw new InvalidOperationException(
        "MONGODB_DATABASE_NAME is missing from the .env file."
    );
}

if (string.IsNullOrWhiteSpace(jwtKey))
{
    throw new InvalidOperationException(
        "JWT_KEY is missing from the .env file."
    );
}

if (string.IsNullOrWhiteSpace(jwtIssuer))
{
    throw new InvalidOperationException(
        "JWT_ISSUER is missing from the .env file."
    );
}

if (string.IsNullOrWhiteSpace(jwtAudience))
{
    throw new InvalidOperationException(
        "JWT_AUDIENCE is missing from the .env file."
    );
}


// ---------------------------------------------------------
// Add .env values into ASP.NET configuration
// ---------------------------------------------------------

builder.Configuration["MongoDb:ConnectionString"] =
    mongoConnectionString;

builder.Configuration["MongoDb:DatabaseName"] =
    mongoDatabaseName;

builder.Configuration["Jwt:Key"] =
    jwtKey;

builder.Configuration["Jwt:Issuer"] =
    jwtIssuer;

builder.Configuration["Jwt:Audience"] =
    jwtAudience;

builder.Configuration["Jwt:ExpiryMinutes"] =
    string.IsNullOrWhiteSpace(jwtExpiryMinutes)
        ? "120"
        : jwtExpiryMinutes;

builder.Configuration["SeedAdmin:Email"] =
    seedAdminEmail ?? "";

builder.Configuration["SeedAdmin:Password"] =
    seedAdminPassword ?? "";


// ---------------------------------------------------------
// Register controllers
// ---------------------------------------------------------

builder.Services.AddControllers();


// ---------------------------------------------------------
// Configure MongoDB settings
// ---------------------------------------------------------

builder.Services.Configure<MongoDbSettings>(
    builder.Configuration
        .GetSection("MongoDb")
);


// ---------------------------------------------------------
// Configure JWT settings
// ---------------------------------------------------------

builder.Services.Configure<JwtSettings>(
    builder.Configuration
        .GetSection("Jwt")
);


// ---------------------------------------------------------
// Register MongoDB database context
// ---------------------------------------------------------

builder.Services.AddSingleton<MongoDbContext>();


// ---------------------------------------------------------
// Register application services
// ---------------------------------------------------------

builder.Services.AddScoped<JwtService>();

builder.Services.AddScoped<AuthService>();

builder.Services.AddScoped<UserService>();

builder.Services.AddScoped<ProsumerService>();

builder.Services.AddScoped<SeedService>();

builder.Services.AddScoped<DatabaseInitializerService>();

builder.Services.AddScoped<StationService>();

builder.Services.AddScoped<ReservationService>();

builder.Services.AddScoped<QrService>();

builder.Services.AddScoped<OperatorService>();

builder.Services.AddScoped<AccountService>();

builder.Services.AddScoped<PasswordResetService>();


// ---------------------------------------------------------
// Read JWT configuration
// ---------------------------------------------------------

var jwtSettings =
    builder.Configuration
        .GetSection("Jwt")
        .Get<JwtSettings>();

if (jwtSettings == null)
{
    throw new InvalidOperationException(
        "JWT configuration could not be loaded."
    );
}

if (string.IsNullOrWhiteSpace(jwtSettings.Key))
{
    throw new InvalidOperationException(
        "JWT key is missing."
    );
}


// ---------------------------------------------------------
// Convert JWT secret key to bytes
// ---------------------------------------------------------

var key =
    Encoding.UTF8.GetBytes(
        jwtSettings.Key
    );

if (key.Length < 32)
{
    throw new InvalidOperationException(
        "JWT_KEY must contain at least 32 UTF-8 bytes (256 bits) for HS256. " +
        "Generate a random secret with 'openssl rand -hex 32', " +
        "set JWT_KEY in the .env file, and restart the API."
    );
}


// ---------------------------------------------------------
// Configure JWT authentication
// ---------------------------------------------------------

builder.Services
    .AddAuthentication(
        JwtBearerDefaults.AuthenticationScheme
    )
    .AddJwtBearer(options =>
    {
        // Validate every JWT received from Web or Android clients.
        options.TokenValidationParameters =
            new TokenValidationParameters
            {
                ValidateIssuer = true,

                ValidateAudience = true,

                ValidateLifetime = true,

                ValidateIssuerSigningKey = true,

                ValidIssuer =
                    jwtSettings.Issuer,

                ValidAudience =
                    jwtSettings.Audience,

                IssuerSigningKey =
                    new SymmetricSecurityKey(
                        key
                    ),

                ClockSkew =
                    TimeSpan.Zero
            };
    });


// ---------------------------------------------------------
// Enable role-based authorization
// ---------------------------------------------------------

builder.Services.AddAuthorization();


// ---------------------------------------------------------
// Configure CORS
//
// During development this allows React and Android clients
// to communicate with the Web API.
// ---------------------------------------------------------

builder.Services.AddCors(options =>
{
    options.AddPolicy(
        "AllowClients",
        policy =>
        {
            policy
                .AllowAnyOrigin()
                .AllowAnyHeader()
                .AllowAnyMethod();
        }
    );
});


// ---------------------------------------------------------
// Configure Swagger / OpenAPI
// ---------------------------------------------------------

builder.Services.AddEndpointsApiExplorer();

builder.Services.AddSwaggerGen(options =>
{
    // Add JWT Bearer authentication to Swagger.
    options.AddSecurityDefinition(
        "bearer",
        new OpenApiSecurityScheme
        {
            Type =
                SecuritySchemeType.Http,

            Scheme =
                "bearer",

            BearerFormat =
                "JWT",

            Description =
                "Enter the JWT token obtained from /api/auth/login."
        }
    );

    // Apply Bearer authentication to protected API endpoints.
    options.AddSecurityRequirement(
        document =>
            new OpenApiSecurityRequirement
            {
                [
                    new OpenApiSecuritySchemeReference(
                        "bearer",
                        document
                    )
                ] = []
            }
    );
});


// ---------------------------------------------------------
// Build application
// ---------------------------------------------------------

var app =
    builder.Build();


// ---------------------------------------------------------
// Enable Swagger
// ---------------------------------------------------------

app.UseSwagger();

app.UseSwaggerUI();


// ---------------------------------------------------------
// Enable CORS
// ---------------------------------------------------------

app.UseCors(
    "AllowClients"
);


// ---------------------------------------------------------
// Enable authentication and authorization
//
// Authentication must come before Authorization.
// ---------------------------------------------------------

app.UseAuthentication();

app.UseAuthorization();


// ---------------------------------------------------------
// Map API controllers
// ---------------------------------------------------------

app.MapControllers();


// ---------------------------------------------------------
// Seed initial Backoffice administrator account
// ---------------------------------------------------------

using (var scope =
       app.Services.CreateScope())
{
    /*
     * Initialize MongoDB indexes before inserting
     * or retrieving application data.
     */
    var databaseInitializer =
        scope.ServiceProvider
            .GetRequiredService<
                DatabaseInitializerService
            >();

    await databaseInitializer
        .InitializeAsync();

    /*
     * Create the first Backoffice user when required.
     */
    var seedService =
        scope.ServiceProvider
            .GetRequiredService<
                SeedService
            >();

    await seedService.SeedAsync();
}

// ---------------------------------------------------------
// Start the Web API
// ---------------------------------------------------------

app.Run();