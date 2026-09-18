import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
  getCurrentUser,
  login
} from "../../services/authService";
import { getApiError } from "../../services/errorService";

export default function LoginPage() {
  const navigate = useNavigate();

  const [email, setEmail] =
    useState("");

  const [password, setPassword] =
    useState("");

  const [showPassword, setShowPassword] =
    useState(false);

  const [error, setError] =
    useState("");

  const [loading, setLoading] =
    useState(false);


  useEffect(() => {
    const user =
      getCurrentUser();

    if (
      user.token &&
      user.role === "Backoffice"
    ) {
      navigate(
        "/backoffice",
        {
          replace: true
        }
      );
    } else if (
      user.token &&
      user.role === "GridOperator"
    ) {
      navigate(
        "/operator",
        {
          replace: true
        }
      );
    }
  }, [navigate]);


  async function handleSubmit(
    event
  ) {
    event.preventDefault();

    setError("");

    if (
      !email.trim() ||
      !password
    ) {
      setError(
        "Enter your email address and password."
      );

      return;
    }

    try {
      setLoading(true);

      const user =
        await login(
          email,
          password
        );

      if (
        user.role === "Backoffice"
      ) {
        navigate(
          "/backoffice",
          {
            replace: true
          }
        );
      } else if (
        user.role ===
        "GridOperator"
      ) {
        navigate(
          "/operator",
          {
            replace: true
          }
        );
      } else {
        setError(
          "This account does not have access to the web portal."
        );
      }

    } catch (err) {

      setError(
        getApiError(
          err,
          "Invalid credentials or account is not active."
        )
      );

    } finally {
      setLoading(false);
    }
  }


  return (
    <div className="login-page">

      {/* ======================================
          LEFT IMAGE PANEL
      ======================================= */}

      <section className="login-showcase d-none d-lg-flex">

        <div className="showcase-overlay" />

        <div className="showcase-content">

          <div className="showcase-badge">
            <span>
              ◆
            </span>

            SMART ENERGY OPERATIONS
          </div>


          <h1>
            Powering{" "}

            <span>
              smarter
            </span>

            <br />

            community energy

            <br />

            exchange

            <span>.</span>
          </h1>


          <p>
            Securely manage users,
            solar prosumers and
            microgrid operations
            from one connected
            platform.
          </p>


          <div className="showcase-metrics">

            <div className="showcase-feature">

              <div className="feature-icon">
                <ShieldIcon />
              </div>

              <div>

                <strong>
                  Secure
                </strong>

                <span>
                  JWT access control
                </span>

              </div>

            </div>


            <div className="feature-divider" />


            <div className="showcase-feature">

              <div className="feature-icon">
                <CloudIcon />
              </div>

              <div>

                <strong>
                  Connected
                </strong>

                <span>
                  Central REST API
                </span>

              </div>

            </div>


            <div className="feature-divider" />


            <div className="showcase-feature">

              <div className="feature-icon">
                <DatabaseIcon />
              </div>

              <div>

                <strong>
                  Cloud
                </strong>

                <span>
                  MongoDB Atlas
                </span>

              </div>

            </div>

          </div>

        </div>

      </section>


      {/* ======================================
          RIGHT LOGIN PANEL
      ======================================= */}

      <section className="login-panel">

        <div className="login-decoration login-decoration-top" />
        <div className="login-decoration login-decoration-bottom" />


        <div className="login-card-wrap">

          {/* Logo */}
          <div className="login-brand">

            <div className="login-logo">

              <span className="login-logo-sun">
                ☀
              </span>

              <span className="login-logo-letter">
                S
              </span>

            </div>


            <div>

              <div className="login-brand-title">
                Smart Solar
              </div>

              <div className="login-brand-subtitle">
                Microgrid Trading System
              </div>

            </div>

          </div>


          {/* Heading */}
          <div className="login-heading">

            <h2>
              Welcome back
            </h2>

            <p>
              Sign in to continue to
              your Smart Solar workspace.
            </p>

          </div>


          {/* Error */}
          {error && (
            <div
              className="alert alert-danger app-alert"
              role="alert"
            >
              {error}
            </div>
          )}


          {/* Form */}
          <form
            onSubmit={handleSubmit}
            noValidate
          >

            <div className="login-form-group">

              <label
                className="form-label"
                htmlFor="email"
              >
                Email address
              </label>


              <div className="login-input-wrapper">

                <span className="login-input-icon">
                  <EmailIcon />
                </span>

                <input
                  id="email"
                  type="email"
                  className="form-control app-input login-input"
                  placeholder="name@example.com"
                  autoComplete="email"
                  value={email}
                  onChange={(event) =>
                    setEmail(
                      event.target.value
                    )
                  }
                  disabled={loading}
                />

              </div>

            </div>


            <div className="login-form-group">

              <label
                className="form-label"
                htmlFor="password"
              >
                Password
              </label>


              <div className="login-input-wrapper">

                <span className="login-input-icon">
                  <LockIcon />
                </span>

                <input
                  id="password"
                  type={
                    showPassword
                      ? "text"
                      : "password"
                  }
                  className="form-control app-input login-input login-password-input"
                  placeholder="Enter your password"
                  autoComplete="current-password"
                  value={password}
                  onChange={(event) =>
                    setPassword(
                      event.target.value
                    )
                  }
                  disabled={loading}
                />


                <button
                  type="button"
                  className="password-toggle"
                  onClick={() =>
                    setShowPassword(
                      (value) =>
                        !value
                    )
                  }
                  tabIndex={-1}
                >
                  {showPassword
                    ? "Hide"
                    : "Show"}
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
                <>

                  <ArrowIcon />

                  <span>
                    Sign in
                  </span>

                </>
              )}

            </button>

          </form>


          <div className="login-note">

            <span />

            <p>
              Web access is available
              to Backoffice and Grid
              Operator accounts.
            </p>

            <span />

          </div>

        </div>

      </section>

    </div>
  );
}


/* ============================================
   ICONS
============================================ */

function EmailIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect
        x="3"
        y="5"
        width="18"
        height="14"
        rx="2"
      />

      <path d="m3 7 9 6 9-6" />
    </svg>
  );
}


function LockIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <rect
        x="5"
        y="10"
        width="14"
        height="11"
        rx="2"
      />

      <path d="M8 10V7a4 4 0 0 1 8 0v3" />
    </svg>
  );
}


function ArrowIcon() {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M5 12h14" />
      <path d="m13 6 6 6-6 6" />
    </svg>
  );
}


function ShieldIcon() {
  return (
    <svg
      width="19"
      height="19"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M12 3 5 6v5c0 5 3 8 7 10 4-2 7-5 7-10V6l-7-3Z" />

      <path d="m9.5 12 1.7 1.7 3.5-4" />
    </svg>
  );
}


function CloudIcon() {
  return (
    <svg
      width="19"
      height="19"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <path d="M17.5 19H7a5 5 0 0 1-.6-9.96A7 7 0 0 1 20 11a4 4 0 0 1-2.5 8Z" />
    </svg>
  );
}


function DatabaseIcon() {
  return (
    <svg
      width="19"
      height="19"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.8"
      strokeLinecap="round"
      strokeLinejoin="round"
    >
      <ellipse
        cx="12"
        cy="5"
        rx="8"
        ry="3"
      />

      <path d="M4 5v6c0 1.7 3.6 3 8 3s8-1.3 8-3V5" />

      <path d="M4 11v6c0 1.7 3.6 3 8 3s8-1.3 8-3v-6" />
    </svg>
  );
}