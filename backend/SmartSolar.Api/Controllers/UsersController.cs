using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Services;

namespace SmartSolar.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Roles = "Backoffice")]
public class UsersController : ControllerBase
{
    private readonly UserService _userService;

    public UsersController(UserService userService)
    {
        _userService = userService;
    }

    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var users =
            await _userService.GetAllAsync();

        return Ok(users.Select(x => new
        {
            x.Id,
            x.Name,
            x.Email,
            x.Role,
            x.Status,
            x.CreatedAt
        }));
    }

    [HttpPost]
    public async Task<IActionResult> Create(
        CreateUserRequest request)
    {
        try
        {
            var user =
                await _userService.CreateAsync(request);

            return Ok(new
            {
                user.Id,
                user.Name,
                user.Email,
                user.Role,
                user.Status
            });
        }
        catch (Exception ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }

    [HttpPatch("{id}/status")]
    public async Task<IActionResult> UpdateStatus(
        string id,
        [FromBody] string status)
    {
        var updated =
            await _userService.UpdateStatusAsync(
                id,
                status
            );

        if (!updated)
            return BadRequest();

        return Ok(new
        {
            message = "User status updated."
        });
    }
}