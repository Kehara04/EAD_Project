using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class StationRequest
{
    [Required]
    [StringLength(100, MinimumLength = 2)]
    public string Name { get; set; } =
        string.Empty;

    [Required]
    [StringLength(250, MinimumLength = 3)]
    public string Address { get; set; } =
        string.Empty;

    [Range(-90, 90)]
    public double Latitude { get; set; }

    [Range(-180, 180)]
    public double Longitude { get; set; }

    [Range(0.1, double.MaxValue)]
    public double CapacityKw { get; set; }

    [Range(1, 1000)]
    public int TotalSlots { get; set; }

    [Required]
    public string OpeningTime { get; set; } =
        string.Empty;

    [Required]
    public string ClosingTime { get; set; } =
        string.Empty;
}