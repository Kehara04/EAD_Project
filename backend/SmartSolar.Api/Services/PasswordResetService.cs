/*
 * File: PasswordResetService.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Handles password reset requests, reset token validation,
 * secure token hashing, and email delivery for user accounts.
 */

using System.Net;
using System.Net.Mail;
using System.Security.Cryptography;
using System.Text;
using MongoDB.Driver;
using SmartSolar.Api.Data;
using SmartSolar.Api.Models;

namespace SmartSolar.Api.Services;

public class PasswordResetService
{
    private readonly MongoDbContext _context;
    private readonly IConfiguration _configuration;

    // Initializes the database context and application configuration.
    public PasswordResetService(
        MongoDbContext context,
        IConfiguration configuration)
    {
        _context = context;
        _configuration = configuration;
    }

    // Generates a password reset token and emails the reset link.
    public async Task RequestResetAsync(string email)
    {
        email = email.Trim().ToLowerInvariant();

        // Retrieves the user associated with the email address.
        var user = await _context.Users
            .Find(x => x.Email == email)
            .FirstOrDefaultAsync();

        
        if (user == null)
            return;

        var token = Convert.ToHexString(
            RandomNumberGenerator.GetBytes(32)
        );

        // Stores the token hash with a 15-minute expiration.
        var record = new PasswordResetToken
        {
            UserId = user.Id ?? string.Empty,
            TokenHash = HashToken(token),
            CreatedAt = DateTime.UtcNow,
            ExpiresAt = DateTime.UtcNow.AddMinutes(15)
        };

        await _context.PasswordResetTokens.DeleteManyAsync(
            x => x.UserId == record.UserId
        );

        await _context.PasswordResetTokens.InsertOneAsync(record);

        // Constructs the password reset link.
        var webUrl = (
            _configuration["WEB_BASE_URL"] ??
            "http://localhost:5173"
        ).TrimEnd('/');

        var resetUrl =
            $"{webUrl}/reset-password?token={Uri.EscapeDataString(token)}";

        try
        {
            await SendEmailAsync(user.Email, resetUrl);
        }
        catch
        {
            await _context.PasswordResetTokens.DeleteOneAsync(
                x => x.Id == record.Id
            );

            throw;
        }
    }

    // Validates the reset token and updates the user's password.
    public async Task<bool> ResetPasswordAsync(
        string token,
        string newPassword,
        string confirmPassword)
    {
        if (newPassword != confirmPassword)
            throw new InvalidOperationException(
                "New password and confirmation do not match."
            );

        if (string.IsNullOrWhiteSpace(token))
            return false;

        var hash = HashToken(token);

        // Atomically consumes the token to prevent reuse.
        var record = await _context.PasswordResetTokens
            .FindOneAndDeleteAsync(
                x => x.TokenHash == hash &&
                     x.ExpiresAt > DateTime.UtcNow
            );

        if (record == null)
            return false;

        // Retrieves the account associated with the reset token.
        var user = await _context.Users
            .Find(x => x.Id == record.UserId)
            .FirstOrDefaultAsync();

        if (user == null)
            return false;

        // Securely hashes the new password before storage.
        var newHash = BCrypt.Net.BCrypt.HashPassword(
            newPassword
        );

        await _context.Users.UpdateOneAsync(
            x => x.Id == user.Id,
            Builders<User>.Update
                .Set(x => x.PasswordHash, newHash)
                .Set(x => x.UpdatedAt, DateTime.UtcNow)
        );

        await _context.PasswordResetTokens.DeleteManyAsync(
            x => x.UserId == record.UserId
        );

        return true;
    }

    // Converts the reset token into a SHA-256 hash.
    private static string HashToken(string token)
    {
        var bytes = SHA256.HashData(
            Encoding.UTF8.GetBytes(token)
        );

        return Convert.ToHexString(bytes);
    }

    // Sends the password reset email using the configured SMTP server.
    private async Task SendEmailAsync(
        string email,
        string resetUrl)
    {
        // Retrieves SMTP configuration values.
        var host = _configuration["SMTP_HOST"];
        var username = _configuration["SMTP_USERNAME"];
        var password = _configuration["SMTP_PASSWORD"];
        var sender = _configuration["SMTP_FROM"];

        if (string.IsNullOrWhiteSpace(host) ||
            string.IsNullOrWhiteSpace(username) ||
            string.IsNullOrWhiteSpace(password) ||
            string.IsNullOrWhiteSpace(sender))
        {
            throw new InvalidOperationException(
                "SMTP configuration is incomplete."
            );
        }

        // Uses port 587 when no SMTP port is configured.
        var port = int.TryParse(
            _configuration["SMTP_PORT"],
            out var configuredPort
        )
            ? configuredPort
            : 587;

        // Creates the password reset email message.
        using var message = new MailMessage
        {
            From = new MailAddress(sender),
            Subject = "Smart Solar password reset",
            Body =
                "A password reset was requested for your Smart Solar account.\n\n" +
                $"Reset your password using this link:\n{resetUrl}\n\n" +
                "This link expires in 15 minutes.\n\n" +
                "If you did not request this change, ignore this email.",
            IsBodyHtml = false
        };

        message.To.Add(email);

        // Configures the SMTP client with SSL and credentials.
        using var smtp = new SmtpClient(host, port)
        {
            EnableSsl = true,
            Credentials = new NetworkCredential(
                username,
                password
            )
        };

        await smtp.SendMailAsync(message);
    }
}