import apiClient from "../api/apiClient";

// Fetch all reservations from the backend for management views.
export async function getReservations() {
  const response =
    await apiClient.get(
      "/reservations"
    );

  return response.data;
}


// Approve a pending reservation by id.
export async function approveReservation(id) {
  const response =
    await apiClient.patch(
      `/reservations/${id}/approve`
    );

  return response.data;
}


// Fetch a single reservation by its id for detail views.
export async function getReservationById(id) {
  const response =
    await apiClient.get(
      `/reservations/${id}`
    );

  return response.data;
}