import apiClient from "../api/apiClient";

export async function login(email, password) {
  const response = await apiClient.post("/auth/login", {
    email: email.trim(),
    password
  });

  const data = response.data;

  localStorage.setItem("token", data.token);
  localStorage.setItem("role", data.role);
  localStorage.setItem("name", data.name || "");
  localStorage.setItem("email", data.email || "");
  localStorage.setItem("userId", data.userId || "");
  localStorage.setItem("referenceId", data.referenceId || "");

  return data;
}

export function logout() {
  localStorage.removeItem("token");
  localStorage.removeItem("role");
  localStorage.removeItem("name");
  localStorage.removeItem("email");
  localStorage.removeItem("userId");
  localStorage.removeItem("referenceId");
}

export function getCurrentUser() {
  return {
    token: localStorage.getItem("token"),
    role: localStorage.getItem("role"),
    name: localStorage.getItem("name"),
    email: localStorage.getItem("email"),
    userId: localStorage.getItem("userId"),
    referenceId: localStorage.getItem("referenceId")
  };
}

export function isAuthenticated() {
  return Boolean(localStorage.getItem("token"));
}
