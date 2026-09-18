import apiClient from "../api/apiClient";

export async function getProsumers(status = "") {
  const response = await apiClient.get("/prosumers", {
    params: status ? { status } : undefined
  });

  return response.data;
}

export async function getPendingProsumers() {
  const response = await apiClient.get("/prosumers/pending");
  return response.data;
}

export async function getDeactivationRequests() {
  const response = await apiClient.get(
    "/prosumers/deactivation-requests"
  );
  return response.data;
}

export async function activateProsumer(nic) {
  const response = await apiClient.patch(
    `/prosumers/${encodeURIComponent(nic)}/activate`
  );
  return response.data;
}

export async function deactivateProsumer(nic) {
  const response = await apiClient.patch(
    `/prosumers/${encodeURIComponent(nic)}/deactivate`
  );
  return response.data;
}

export async function reactivateProsumer(nic) {
  const response = await apiClient.patch(
    `/prosumers/${encodeURIComponent(nic)}/reactivate`
  );
  return response.data;
}
