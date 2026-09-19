export function getApiError(error, fallback = "Something went wrong.") {
  const data = error?.response?.data;

  if (typeof data === "string" && data.trim()) {
    return data;
  }

  if (data?.message) {
    return data.message;
  }

  if (data?.errors && typeof data.errors === "object") {
    const firstError = Object.values(data.errors).flat()[0];
    if (firstError) {
      return firstError;
    }
  }

  if (error?.message === "Network Error") {
    return "Unable to reach the Smart Solar API. Check that the backend is running.";
  }

  return fallback;
}
