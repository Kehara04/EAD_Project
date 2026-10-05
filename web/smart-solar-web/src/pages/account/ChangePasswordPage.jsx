import { useState } from "react";
import DashboardLayout from "../../components/DashboardLayout";

import { changePassword } from "../../services/accountService";
import { getApiError } from "../../services/errorService";

const PASSWORD_REGEX =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,64}$/;

// Render the password change page and manage password update operations.
export default function ChangePasswordPage() {

  // Store the current password, new password, and confirmation.
  const [form, setForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: ""
  });

  // Track submission progress and display feedback messages.
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

   // Update the selected password field when the user enters a value.
  function updateField(field, value) {
    setForm((previous) => ({
      ...previous,
      [field]: value
    }));
  }

  // Validate the password form and submit the password change request.
  async function handleSubmit(event) {
    event.preventDefault();

    // Clear previous error and success messages.
    setError("");
    setSuccess("");

    if (!PASSWORD_REGEX.test(form.newPassword)) {
      setError(
        "New password must contain uppercase, lowercase, number and special character, with no spaces."
      );
      return;
    }

    if (form.newPassword !== form.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    try {
      setSaving(true);

      const response = await changePassword(form);

      setSuccess(response.message);

      setForm({
        currentPassword: "",
        newPassword: "",
        confirmPassword: ""
      });
    } catch (err) {
      setError(
        getApiError(err, "Unable to change password.")
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <DashboardLayout
      title="Change Password"
      subtitle="Protect your Smart Solar account with a secure password."
    >
      <div className="row justify-content-center">
        <div className="col-12 col-lg-7 col-xl-6">

          <div className="dashboard-card account-page-card">

            <div className="section-heading">
              <span className="eyebrow">
                ACCOUNT SECURITY
              </span>

              <h3>Update your password</h3>

              <p>
                Enter your existing password before setting a new one.
              </p>
            </div>

            {error && (
              <div className="alert alert-danger">
                {error}
              </div>
            )}

            {success && (
              <div className="alert alert-success">
                {success}
              </div>
            )}

            <form onSubmit={handleSubmit}>

              {[
                ["currentPassword", "Current password"],
                ["newPassword", "New password"],
                ["confirmPassword", "Confirm new password"]
              ].map(([field, label]) => (
                <div className="mb-3" key={field}>

                  <label className="form-label">
                    {label}
                  </label>

                  <input
                    type="password"
                    className="form-control app-input"
                    value={form[field]}
                    onChange={(event) =>
                      updateField(field, event.target.value)
                    }
                    autoComplete={
                      field === "currentPassword"
                        ? "current-password"
                        : "new-password"
                    }
                    required
                  />

                </div>
              ))}

              <p className="text-muted small">
                Use at least 8 characters with uppercase,
                lowercase, number and special character.
              </p>

              <button
                type="submit"
                className="btn btn-solar w-100"
                disabled={saving}
              >
                {saving
                  ? "Updating password..."
                  : "Change password"}
              </button>

            </form>

          </div>

        </div>
      </div>
    </DashboardLayout>
  );
}