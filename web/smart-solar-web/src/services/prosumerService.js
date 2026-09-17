import apiClient from "../api/apiClient";

export async function getProsumers() {
  const response =
    await apiClient.get("/prosumers");

  return response.data;
}

export async function activateProsumer(nic) {
  await apiClient.patch(
    `/prosumers/${nic}/activate`
  );
}

export async function deactivateProsumer(nic) {
  await apiClient.patch(
    `/prosumers/${nic}/deactivate`
  );
}

export async function reactivateProsumer(nic) {
  await apiClient.patch(
    `/prosumers/${nic}/reactivate`
  );
}