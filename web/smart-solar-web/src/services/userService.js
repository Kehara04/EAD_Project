import apiClient from "../api/apiClient";

// Retrieves all registered users from the backend.
export async function getUsers() {
  const response = await apiClient.get("/users");
  return response.data;
}

// Retrieves a specific user's details using their ID.
export async function getUserById(id) {
  const response = await apiClient.get(`/users/${id}`);
  return response.data;
}

// Creates a new user account with the provided details.
export async function createUser(data) {
  const response = await apiClient.post("/users", data);
  return response.data;
}

// Updates the details of an existing user account.
export async function updateUser(id, data) {
  const response = await apiClient.put(
    `/users/${id}`,
    data
  );

  return response.data;
}

// Updates the activation status of the specified user account.
export async function updateUserStatus(id, status) {
  const response = await apiClient.patch(
    `/users/${id}/status`,
    { status }
  );

  return response.data;
}
