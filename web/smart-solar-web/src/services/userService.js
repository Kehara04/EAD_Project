import apiClient from "../api/apiClient";

export async function getUsers() {
  const response =
    await apiClient.get("/users");

  return response.data;
}

export async function createUser(data) {
  const response =
    await apiClient.post("/users", data);

  return response.data;
}