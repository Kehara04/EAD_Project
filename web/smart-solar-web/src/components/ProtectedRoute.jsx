import { Navigate, useLocation } from "react-router-dom";
import { getCurrentUser } from "../services/authService";

export default function ProtectedRoute({ children, roles = [] }) {
  const location = useLocation();
  const user = getCurrentUser();

  if (!user.token) {
    return <Navigate to="/" replace state={{ from: location }} />;
  }

  if (roles.length > 0 && !roles.includes(user.role)) {
    const home =
      user.role === "Backoffice"
        ? "/backoffice"
        : user.role === "GridOperator"
          ? "/operator"
          : "/";

    return <Navigate to={home} replace />;
  }

  return children;
}
