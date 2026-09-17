using MongoDB.Driver;
using SmartSolar.Api.Data;
using SmartSolar.Api.DTOs;

namespace SmartSolar.Api.Services;

public class AuthService
{
    private readonly MongoDbContext _context;
    private readonly JwtService _jwtService;

    public AuthService(
        MongoDbContext context,
        JwtService jwtService)
    {
        _context = context;
        _jwtService = jwtService;
    }

    public async Task<LoginResponse?> LoginAsync(
        LoginRequest request)
    {
        var email = request.Email.Trim().ToLower();

        var user = await _context.Users
            .Find(x => x.Email == email)
            .FirstOrDefaultAsync();

        if (user == null)
            return null;

        if (user.Status != "Active")
            return null;

        if (!BCrypt.Net.BCrypt.Verify(
                request.Password,
                user.PasswordHash))
        {
            return null;
        }

        return new LoginResponse
        {
            Token = _jwtService.GenerateToken(user),
            UserId = user.Id ?? string.Empty,
            Name = user.Name,
            Email = user.Email,
            Role = user.Role,
            ReferenceId = user.ReferenceId
        };
    }
}