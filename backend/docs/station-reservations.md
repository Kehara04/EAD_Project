# Station deactivation and reservation integration

`StationService.UpdateStatusAsync` rejects deactivation when `AvailableSlots < TotalSlots`.
The availability comparison is part of the MongoDB update filter, so the check and
status change happen atomically on the station document. A rejected request returns
HTTP 400 with a message displayed by Station Management. Reactivation is allowed.

This is an interim booked-slot safeguard, not a query of active reservations.
There is no reservation model or collection integration in this repository yet.
Reservations not reflected in station slot counts cannot be detected by this guard.

## Member 3 integration

Before reservation functionality is released:

1. Agree on station ID linkage, reservation collection/service, statuses that block
   deactivation, and expiration/cancellation/completion rules.
2. Replace or supplement the slot comparison in `UpdateStatusAsync` with an
   authoritative check for blocking reservations, including future reservations
   if those should keep a station active.
3. Coordinate reservation creation and station deactivation atomically (for
   example, a transaction that also writes the station document). A separate
   read followed by an unconditional status update leaves a race condition.
4. Keep slot counts synchronized when bookings are created, cancelled or completed.
5. Verify active bookings block deactivation, cancelled/completed bookings do not,
   activation still succeeds, and concurrent booking/deactivation cannot both succeed.

Station edits must not reduce total slots below the number already booked.
The web availability filter and dashboard count mean `AvailableSlots > 0` across
both statuses; combine the availability and Active filters to see usable stations.
