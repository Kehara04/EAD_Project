import {
  BrowserRouter,
  Navigate,
  Route,
  Routes
} from "react-router-dom";

import LoginPage
  from "./pages/auth/LoginPage";

import BackofficeDashboard
  from "./pages/backoffice/BackofficeDashboard";

import UserManagementPage
  from "./pages/backoffice/UserManagementPage";

import ProsumerManagementPage
  from "./pages/backoffice/ProsumerManagementPage";

import StationManagementPage
  from "./pages/backoffice/StationManagementPage";

import ReservationManagementPage
  from "./pages/backoffice/ReservationManagementPage";

import OperatorHome
  from "./pages/operator/OperatorHome";

import ProtectedRoute
  from "./components/ProtectedRoute";

// Account management
import MyProfilePage
  from "./pages/account/MyProfilePage";

import ChangePasswordPage
  from "./pages/account/ChangePasswordPage";

import ForgotPasswordPage
  from "./pages/auth/ForgotPasswordPage";

import ResetPasswordPage
  from "./pages/auth/ResetPasswordPage";

import EditUserPage
  from "./pages/backoffice/EditUserPage";


export default function App() {
  return (
    <BrowserRouter>

      <Routes>

            //LOGIN
        <Route
          path="/"
          element={<LoginPage />}
        />

            //PUBLIC PASSWORD RECOVERY
        <Route
          path="/forgot-password"
          element={<ForgotPasswordPage />}
        />

        <Route
          path="/reset-password"
          element={<ResetPasswordPage />}
        />

            //BACKOFFICE
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
          path="/backoffice/stations"
          element={
            <ProtectedRoute roles={["Backoffice"]}>
              <StationManagementPage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/backoffice/reservations"
          element={
            <ProtectedRoute roles={["Backoffice"]}>
              <ReservationManagementPage />
            </ProtectedRoute>
          }
        />

            //EDIT USER - BACKOFFICE ONLY
        <Route
          path="/backoffice/users/:id/edit"
          element={
            <ProtectedRoute roles={["Backoffice"]}>
              <EditUserPage />
            </ProtectedRoute>
          }
        />

            //GRID OPERATOR
        <Route
          path="/operator"
          element={
            <ProtectedRoute roles={["GridOperator"]}>
              <OperatorHome />
            </ProtectedRoute>
          }
        />

            //ACCOUNT MANAGEMENT
        <Route
          path="/account/profile"
          element={
            <ProtectedRoute
              roles={["Backoffice", "GridOperator"]}
            >
              <MyProfilePage />
            </ProtectedRoute>
          }
        />

        <Route
          path="/account/change-password"
          element={
            <ProtectedRoute
              roles={["Backoffice", "GridOperator"]}
            >
              <ChangePasswordPage />
            </ProtectedRoute>
          }
        />

            //FALLBACK - KEEP LAST
        <Route
          path="*"
          element={
            <Navigate
              to="/"
              replace
            />
          }
        />

      </Routes>

    </BrowserRouter>
  );
}