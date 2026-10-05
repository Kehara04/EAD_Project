/*reference : https://youtu.be/La5cL2jNoVw?si=tZ7XAxpHepyAaetE
*/

import apiClient from "../api/apiClient";

// Load stations with an optional API status filter.
export async function getStations(status = "") {
  const response =
    await apiClient.get(
      "/stations",
      {
        params:
          status
            ? { status }
            : {}
      }
    );

  return response.data;
}


// Refresh one station by its persisted identifier.
export async function getStation(id) {
  const response =
    await apiClient.get(
      `/stations/${id}`
    );

  return response.data;
}


// Send the selected address token; the backend resolves and stores coordinates.
export async function createStation(data) {
  const response =
    await apiClient.post(
      "/stations",
      data
    );

  return response.data;
}


// Save edits through the backend, where location and capacity rules are enforced.
export async function updateStation(
  id,
  data
) {
  const response =
    await apiClient.put(
      `/stations/${id}`,
      data
    );

  return response.data;
}


// Request activation or deactivation; booked reservations can block the change.
export async function updateStationStatus(
  id,
  status
) {
  const response =
    await apiClient.patch(
      `/stations/${id}/status`,
      {
        status
      }
    );

  return response.data;
}

// Proxy address search through the backend so the provider API key stays private.
export async function getStationAddressSuggestions(query, signal) {
  const response = await apiClient.get("/stations/address-suggestions", {
    params: { query }, signal
  });
  return response.data;
}
