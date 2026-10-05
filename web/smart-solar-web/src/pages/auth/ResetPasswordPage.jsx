import { useState } from "react";
import { Link, useSearchParams } from "react-router-dom";

import { resetPassword } from "../../services/accountService";
import { getApiError } from "../../services/errorService";

const PASSWORD_REGEX =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,64}$/;

// Display the password reset page and manage the account recovery process.
export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams();

  const token = searchParams.get("token") || "";

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Validate the reset token and passwords before submitting the reset request.
  async function handleSubmit(event) {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (!token) {
      setError("Password reset link is invalid.");
      return;
    }

    if (!PASSWORD_REGEX.test(newPassword)) {
      setError(
        "Password must contain uppercase, lowercase, number and special character."
      );
      return;
    }

    if (newPassword !== confirmPassword) {
      setError("Passwords do not match.");
      return;
    }

    try {
      setLoading(true);

      const response = await resetPassword({
        token,
        newPassword,
        confirmPassword
      });

      setSuccess(response.message);

      setNewPassword("");
      setConfirmPassword("");
    } catch (err) {
      setError(
        getApiError(
          err,
          "Unable to reset password."
        )
      );
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="login-page">

      <section className="login-showcase d-none d-lg-flex">

        <div className="showcase-overlay" />

        <div className="showcase-content">

          <div className="showcase-badge">
            SMART SOLAR ACCOUNT SECURITY
          </div>

          <h1>
            Create a new{" "}
            <span>secure password.</span>
          </h1>

          <p>
            Restore access to your Smart Solar account.
          </p>

        </div>

      </section>

      <section className="login-panel">

        <div className="login-card-wrap">

          <div className="login-official-brand">
            <img
              src="/images/smart-solar-logo.png"
              alt="Smart Solar"
              className="login-official-logo"
            />
          </div>

          <div className="login-heading">

            <h2>Reset password</h2>

            <p>
              Choose a strong password for your account.
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

          {!success && (
            <form onSubmit={handleSubmit}>

              <div className="login-form-group">

                <label className="form-label">
                  New password
                </label>

                <input
                  type="password"
                  className="form-control app-input login-input"
                  value={newPassword}
                  onChange={(event) =>
                    setNewPassword(event.target.value)
                  }
                  autoComplete="new-password"
                  required
                />

              </div>

              <div className="login-form-group">

                <label className="form-label">
                  Confirm password
                </label>

                <input
                  type="password"
                  className="form-control app-input login-input"
                  value={confirmPassword}
                  onChange={(event) =>
                    setConfirmPassword(event.target.value)
                  }
                  autoComplete="new-password"
                  required
                />

              </div>

              <button
                className="btn btn-solar w-100 login-submit"
                type="submit"
                disabled={loading || !token}
              >
                {loading
                  ? "Resetting password..."
                  : "Reset password"}
              </button>

            </form>
          )}

          <Link
            to="/"
            className="d-block text-center mt-4"
          >
            Back to sign in
          </Link>

        </div>

      </section>

    </div>
  );
}