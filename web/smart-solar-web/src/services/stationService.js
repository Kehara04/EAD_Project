import apiClient from "../api/apiClient";

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


export async function getStation(id) {
  const response =
    await apiClient.get(
      `/stations/${id}`
    );

  return response.data;
}


export async function createStation(data) {
  const response =
    await apiClient.post(
      "/stations",
      data
    );

  return response.data;
}


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