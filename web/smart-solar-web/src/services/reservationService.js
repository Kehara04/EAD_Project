import apiClient from "../api/apiClient";

export async function getReservations() {
  const response =
    await apiClient.get(
      "/reservations"
    );

  return response.data;
}


export async function approveReservation(id) {
  const response =
    await apiClient.patch(
      `/reservations/${id}/approve`
    );

  return response.data;
}


export async function getReservationById(id) {
  const response =
    await apiClient.get(
      `/reservations/${id}`
    );

  return response.data;
}