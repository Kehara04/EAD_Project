import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { getCurrentUser, login } from "../../services/authService";
import { getApiError } from "../../services/errorService";

export default function LoginPage() {
  const navigate = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const user = getCurrentUser();

    if (user.token && user.role === "Backoffice") {
      navigate("/backoffice", { replace: true });
    } else if (user.token && user.role === "GridOperator") {
      navigate("/operator", { replace: true });
    }
  }, [navigate]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");

    if (!email.trim() || !password) {
      setError("Enter your email address and password.");
      return;
    }

    try {
      setLoading(true);
      const user = await login(email, password);

      if (user.role === "Backoffice") {
        navigate("/backoffice", { replace: true });
      } else if (user.role === "GridOperator") {
        navigate("/operator", { replace: true });
      } else {
        setError("This account does not have access to the web portal.");
      }
    } catch (err) {
      setError(
        getApiError(err, "Invalid credentials or account is not active.")
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
          <div className="showcase-badge">SMART ENERGY OPERATIONS</div>
          <h1>Powering smarter community energy exchange.</h1>
          <p>
            Securely manage users, solar prosumers and microgrid operations from
            one connected platform.
          </p>

          <div className="showcase-metrics">
            <div>
              <strong>Secure</strong>
              <span>JWT access control</span>
            </div>
            <div>
              <strong>Connected</strong>
              <span>Central REST API</span>
            </div>
            <div>
              <strong>Cloud</strong>
              <span>MongoDB Atlas</span>
            </div>
          </div>
        </div>
      </section>

      <section className="login-panel">
        <div className="login-card-wrap">
          <div className="login-brand">
            <div className="brand-mark large">S</div>
            <div>
              <div className="brand-title dark">Smart Solar</div>
              <div className="brand-subtitle">Microgrid Trading System</div>
            </div>
          </div>

          <div className="login-heading">
            <h2>Welcome back</h2>
            <p>Sign in to continue to your Smart Solar workspace.</p>
          </div>

          {error && (
            <div className="alert alert-danger app-alert" role="alert">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} noValidate>
            <div className="mb-3">
              <label className="form-label" htmlFor="email">
                Email address
              </label>
              <input
                id="email"
                type="email"
                className="form-control app-input"
                placeholder="name@example.com"
                autoComplete="email"
                value={email}
                onChange={(event) => setEmail(event.target.value)}
                disabled={loading}
              />
            </div>

            <div className="mb-4">
              <label className="form-label" htmlFor="password">
                Password
              </label>
              <div className="password-field">
                <input
                  id="password"
                  type={showPassword ? "text" : "password"}
                  className="form-control app-input"
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) => setPassword(event.target.value)}
                  disabled={loading}
                />
                <button
                  type="button"
                  className="password-toggle"
                  onClick={() => setShowPassword((value) => !value)}
                  tabIndex={-1}
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-solar w-100 login-submit"
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" />
                  Signing in...
                </>
              ) : (
                "Sign in"
              )}
            </button>
          </form>

          <div className="login-note">
            Web access is available to Backoffice and Grid Operator accounts.
          </div>
        </div>
      </section>
    </div>
  );
}
