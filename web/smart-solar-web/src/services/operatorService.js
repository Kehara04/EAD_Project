import apiClient from "../api/apiClient";

export async function getOperatorDashboardStats() {
    const response = await apiClient.get("/operator/dashboard");
    return response.data;
}
