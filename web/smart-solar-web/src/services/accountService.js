import apiClient from "../api/apiClient";

export async function getMyProfile() {
  const response = await apiClient.get("/account/me");
  return response.data;
}

export async function updateMyProfile(data) {
  const response = await apiClient.put("/account/me", data);

  localStorage.setItem("name", response.data.name);
  localStorage.setItem("email", response.data.email);

  return response.data;
}

export async function changePassword(data) {
  const response = await apiClient.post(
    "/account/change-password",
    data
  );

  return response.data;
}

export async function forgotPassword(email) {
  const response = await apiClient.post(
    "/auth/forgot-password",
    { email }
  );

  return response.data;
}

export async function resetPassword(data) {
  const response = await apiClient.post(
    "/auth/reset-password",
    data
  );

  return response.data;
}