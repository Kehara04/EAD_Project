import { useState } from "react";
import { Link } from "react-router-dom";

import { forgotPassword } from "../../services/accountService";
import { getApiError } from "../../services/errorService";

// Display the password recovery page and allow users to request a reset link.
export default function ForgotPasswordPage() {

  // Store the entered email address and request feedback states.
  const [email, setEmail] = useState("");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Submit the password reset request using the entered email address.
  async function handleSubmit(event) {
    event.preventDefault();

    setError("");
    setSuccess("");

    try {
      setLoading(true);

      const response = await forgotPassword(email);

      setSuccess(response.message);
    } catch (err) {
      setError(
        getApiError(
          err,
          "Unable to process your request."
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
            Secure access to your{" "}
            <span>energy workspace.</span>
          </h1>

          <p>
            Recover your account using a secure,
            time-limited password reset link.
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
            <h2>Forgot password?</h2>

            <p>
              Enter your registered email address to
              request a password reset.
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

            <div className="login-form-group">

              <label className="form-label">
                Email address
              </label>

              <input
                type="email"
                className="form-control app-input login-input"
                placeholder="name@example.com"
                value={email}
                onChange={(event) =>
                  setEmail(event.target.value)
                }
                required
              />

            </div>

            <button
              className="btn btn-solar w-100 login-submit"
              type="submit"
              disabled={loading}
            >
              {loading
                ? "Sending instructions..."
                : "Send reset link"}
            </button>

          </form>

          <Link
            to="/"
            className="d-block text-center mt-4"
          >
            ← Back to sign in
          </Link>

        </div>

      </section>

    </div>
  );
}