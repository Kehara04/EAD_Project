import apiClient from "../api/apiClient";

// Retrieves all Prosumers or filters them by the specified account status.
export async function getProsumers(status = "") {
  const response = await apiClient.get("/prosumers", {
    params: status ? { status } : undefined
  });

  return response.data;
}

// Retrieves Prosumer accounts awaiting activation.
export async function getPendingProsumers() {
  const response = await apiClient.get("/prosumers/pending");
  return response.data;
}

// Retrieves Prosumer accounts with pending deactivation requests.
export async function getDeactivationRequests() {
  const response = await apiClient.get(
    "/prosumers/deactivation-requests"
  );
  return response.data;
}

// Activates the Prosumer account identified by its NIC.
export async function activateProsumer(nic) {
  const response = await apiClient.patch(
    `/prosumers/${encodeURIComponent(nic)}/activate`
  );
  return response.data;
}

// Deactivates the Prosumer account identified by its NIC.
export async function deactivateProsumer(nic) {
  const response = await apiClient.patch(
    `/prosumers/${encodeURIComponent(nic)}/deactivate`
  );
  return response.data;
}

// Reactivates a previously deactivated Prosumer account.
export async function reactivateProsumer(nic) {
  const response = await apiClient.patch(
    `/prosumers/${encodeURIComponent(nic)}/reactivate`
  );
  return response.data;
}
