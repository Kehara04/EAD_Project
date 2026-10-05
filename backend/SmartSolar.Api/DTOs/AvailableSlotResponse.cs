/*
 * File: AvailableSlotResponse.cs
 * Project: Smart Solar Microgrid Trading System
 * Component: Energy Slot Reservation Management
 *
 * Description:
 * Defines the response returned when retrieving station
 * booking slots for a selected date and time.
 *
 * Includes slot identification, number, label, and availability.
 */
namespace SmartSolar.Api.DTOs;

// Represents an energy slot and its availability for the requested schedule.
public class AvailableSlotResponse
{
    public string SlotId { get; set; } =
        string.Empty;

    public int SlotNumber { get; set; }

    public string Label { get; set; } =
        string.Empty;

    public bool Available { get; set; }
}