using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class UpdateProsumerRequest
{
    [Required]
    public string Name { get; set; } = string.Empty;

    [Required]
    [EmailAddress]
    public string Email { get; set; } = string.Empty;

    public string Phone { get; set; } = string.Empty;

    public string Address { get; set; } = string.Empty;
}