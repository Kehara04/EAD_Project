import apiClient from "../api/apiClient";

export async function login(email, password) {
  const response = await apiClient.post(
    "/auth/login",
    { email, password }
  );

  const data = response.data;

  localStorage.setItem("token", data.token);
  localStorage.setItem("role", data.role);
  localStorage.setItem("name", data.name);

  return data;
}

export function logout() {
  localStorage.clear();
}