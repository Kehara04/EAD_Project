import apiClient from "../api/apiClient";

// Retrieves the currently authenticated user's profile details.
export async function getMyProfile() {
  const response = await apiClient.get("/account/me");
  return response.data;
}

// Updates the user's profile and stores the updated name and email locally.
export async function updateMyProfile(data) {
  const response = await apiClient.put("/account/me", data);

  localStorage.setItem("name", response.data.name);
  localStorage.setItem("email", response.data.email);

  return response.data;
}

// Changes the authenticated user's password.
export async function changePassword(data) {
  const response = await apiClient.post(
    "/account/change-password",
    data
  );

  return response.data;
}

// Sends a password reset request using the user's email address.
export async function forgotPassword(email) {
  const response = await apiClient.post(
    "/auth/forgot-password",
    { email }
  );

  return response.data;
}

// Submits the password reset details to set a new password.
export async function resetPassword(data) {
  const response = await apiClient.post(
    "/auth/reset-password",
    data
  );

  return response.data;
}