/**
 * Reference:React Axios CRUD with REST API
 * https://www.youtube.com/watch?v=59z1_3-vTOk
 */

import apiClient from "../api/apiClient";

// Retrieves all energy reservations from the backend.
export async function getReservations() {
  const response =
    await apiClient.get(
      "/reservations"
    );

  return response.data;
}

// Approves the reservation identified by its ID.
export async function approveReservation(id) {
  const response =
    await apiClient.patch(
      `/reservations/${id}/approve`
    );

  return response.data;
}

// Retrieves the details of a specific reservation by its ID.
export async function getReservationById(id) {
  const response =
    await apiClient.get(
      `/reservations/${id}`
    );

  return response.data;
}