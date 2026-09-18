import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import LoginPage from "./pages/auth/LoginPage";
import BackofficeDashboard from "./pages/backoffice/BackofficeDashboard";
import UserManagementPage from "./pages/backoffice/UserManagementPage";
import ProsumerManagementPage from "./pages/backoffice/ProsumerManagementPage";
import OperatorHome from "./pages/operator/OperatorHome";
import ProtectedRoute from "./components/ProtectedRoute";

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<LoginPage />} />

        <Route
          path="/backoffice"
          element={
            <ProtectedRoute roles={["Backoffice"]}>
              <BackofficeDashboard />
            </ProtectedRoute>
          }
        />

        <Route
          path="/backoffice/users"
          element={
            <ProtectedRoute roles={["Backoffice"]}>
              <UserManagementPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/backoffice/prosumers"
          element={
            <ProtectedRoute roles={["Backoffice"]}>
              <ProsumerManagementPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/operator"
          element={
            <ProtectedRoute roles={["GridOperator"]}>
              <OperatorHome />
            </ProtectedRoute>
          }
        />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
