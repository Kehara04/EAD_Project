import { Navigate } from "react-router-dom";

export default function ProtectedRoute({
  children,
  role
}) {
  const token = localStorage.getItem("token");
  const currentRole =
    localStorage.getItem("role");

  if (!token) {
    return <Navigate to="/" replace />;
  }

  if (role && currentRole !== role) {
    return <Navigate to="/" replace />;
  }

  return children;
}