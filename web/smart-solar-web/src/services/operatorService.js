import apiClient from "../api/apiClient";

// Retrieves dashboard statistics and reservation information for the Grid Operator.
export async function getOperatorDashboardStats() {
    const response = await apiClient.get("/operator/dashboard");
    return response.data;
}
