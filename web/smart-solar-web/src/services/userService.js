import apiClient from "../api/apiClient";

export async function getUsers() {
  const response = await apiClient.get("/users");
  return response.data;
}

export async function getUserById(id) {
  const response = await apiClient.get(`/users/${id}`);
  return response.data;
}

export async function createUser(data) {
  const response = await apiClient.post("/users", data);
  return response.data;
}

export async function updateUserStatus(id, status) {
  const response = await apiClient.patch(
    `/users/${id}/status`,
    { status }
  );

  return response.data;
}
