import { Navigate, useLocation } from "react-router-dom";
import { getCurrentUser } from "../services/authService";

// Protect application routes by checking authentication and user roles.
export default function ProtectedRoute({ children, roles = [] }) {
  
   // Get the current route and authenticated user's information.
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
