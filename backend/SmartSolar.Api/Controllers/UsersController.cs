/*
 * File: UsersController.cs
 * Project: Smart Solar Microgrid Trading System
 * Description:
 * Provides Backoffice endpoints for system user management.
 */

using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using SmartSolar.Api.Constants;
using SmartSolar.Api.DTOs;
using SmartSolar.Api.Services;

namespace SmartSolar.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Authorize(Roles = UserRoles.Backoffice)]
public class UsersController : ControllerBase
{
    private readonly UserService _userService;

    // ---------------------------------------------------------
    // Constructor: Stores the user service dependency for account management endpoints.
    // ---------------------------------------------------------
    public UsersController(
        UserService userService)
    {
        // Store user service.
        _userService = userService;
    }

    [HttpGet]
    // ---------------------------------------------------------
    // GetAll: Returns all registered system users to authorized backoffice staff.
    // ---------------------------------------------------------
    public async Task<IActionResult> GetAll()
    {
        // Return all Backoffice, Grid Operator
        // and Prosumer authentication accounts.
        var users =
            await _userService.GetAllAsync();

        return Ok(users);
    }

    [HttpGet("{id}")]
    // ---------------------------------------------------------
    // GetById: Retrieves a single user account by their unique ID.
    // ---------------------------------------------------------
    public async Task<IActionResult> GetById(
        string id)
    {
        // Retrieve an individual user account.
        var user =
            await _userService
                .GetByIdAsync(id);

        if (user == null)
        {
            return NotFound(new
            {
                message =
                    "User was not found."
            });
        }

        return Ok(user);
    }

    [HttpPost]
    // ---------------------------------------------------------
    // Create: Creates a new Backoffice or Grid Operator account.
    // ---------------------------------------------------------
    public async Task<IActionResult> Create(
        CreateUserRequest request)
    {
        // Create Backoffice or Grid Operator account.
        try
        {
            var user =
                await _userService
                    .CreateAsync(request);

            return Created(
                $"/api/users/{user.Id}",
                user
            );
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }

    [HttpPatch("{id}/status")]
    // ---------------------------------------------------------
    // UpdateStatus: Activates or deactivates a user account status.
    // ---------------------------------------------------------
    public async Task<IActionResult>
        UpdateStatus(
            string id,
            UpdateUserStatusRequest request)
    {
        // Activate or deactivate a web application user.
        try
        {
            var user =
                await _userService
                    .UpdateStatusAsync(
                        id,
                        request.Status
                    );

            if (user == null)
            {
                return NotFound(new
                {
                    message =
                        "User was not found."
                });
            }

            return Ok(user);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequest(new
            {
                message = ex.Message
            });
        }
    }
}