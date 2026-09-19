using System.ComponentModel.DataAnnotations;

namespace SmartSolar.Api.DTOs;

public class UpdateStationStatusRequest
{
    [Required]
    [RegularExpression(
        "^(Active|Inactive)$",
        ErrorMessage =
            "Status must be Active or Inactive."
    )]
    public string Status { get; set; } =
        string.Empty;
}