import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";

import {
  getCurrentUser,
  login
} from "../../services/authService";

import { getApiError } from "../../services/errorService";


export default function LoginPage() {
  const navigate = useNavigate();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");

  const [showPassword, setShowPassword] =
    useState(false);

  const [error, setError] = useState("");
  const [loading, setLoading] =
    useState(false);

  useEffect(() => {
    const user = getCurrentUser();

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

  async function handleSubmit(event) {
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

      <section className="login-showcase d-none d-lg-flex">

        <div className="showcase-overlay" />

        <div className="showcase-content">

          {/* Small category label */}
          <div className="showcase-badge">

            <span className="showcase-badge-dot">
              ☀
            </span>

            SMART SOLAR MICROGRID PLATFORM

          </div>


          {/* Clear application purpose */}
          <h1 className="showcase-main-title">

            Manage your solar

            <br />

            <span>
              microgrid
            </span>

            {" "}from one

            <br />

            connected platform

            <span>.</span>

          </h1>


          <p className="showcase-description">

            Smart Solar connects
            Backoffice teams and Grid
            Operators to manage prosumers,
            microgrid stations, reservations
            and energy operations securely.

          </p>


          {/* Purpose highlights */}
          <div className="showcase-purpose">

            <span>
              PROSUMERS
            </span>

            <span className="purpose-dot">
              •
            </span>

            <span>
              MICROGRIDS
            </span>

            <span className="purpose-dot">
              •
            </span>

            <span>
              ENERGY TRADING
            </span>

          </div>


          {/* Platform features */}
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
                  Role-based access
                </span>

              </div>

            </div>


            <div className="feature-divider" />


            <div className="showcase-feature">

              <div className="feature-icon">
                <EnergyIcon />
              </div>

              <div>

                <strong>
                  Smart Energy
                </strong>

                <span>
                  Microgrid operations
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
                  Central platform
                </span>

              </div>

            </div>

          </div>

        </div>

      </section>

      <section className="login-panel">

        <div
          className="
            login-decoration
            login-decoration-top
          "
        />

        <div
          className="
            login-decoration
            login-decoration-bottom
          "
        />


        <div className="login-card-wrap">

          <div className="login-official-brand">

            <img
              src="/images/smart-solar-logo.png"
              alt="Smart Solar - Powering a Smarter Tomorrow"
              className="login-official-logo"
            />

          </div>

          <div className="login-heading">

            <div className="login-portal-label">
              OPERATIONS PORTAL
            </div>

            <h2>
              Welcome to Smart Solar
            </h2>

            <p>
              Sign in to manage Smart Solar
              microgrid operations.
            </p>

          </div>

          <div className="login-access-info">

            <div className="login-access-icon">
              <ShieldIcon />
            </div>

            <div>

              <strong>
                Authorized portal access
              </strong>

              <span>
                For Backoffice and Grid
                Operator accounts
              </span>

            </div>

          </div>

          {error && (
            <div
              className="
                alert
                alert-danger
                app-alert
              "
              role="alert"
            >
              {error}
            </div>
          )}

          <form
            onSubmit={handleSubmit}
            noValidate
          >

            {/* EMAIL */}
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
                  className="
                    form-control
                    app-input
                    login-input
                  "
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


            {/* PASSWORD */}
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
                  className="
                    form-control
                    app-input
                    login-input
                    login-password-input
                  "
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
                  aria-label={
                    showPassword
                      ? "Hide password"
                      : "Show password"
                  }
                >
                  {showPassword
                    ? "Hide"
                    : "Show"}
                </button>

              </div>

            </div>

            <div className="text-end mb-3">
  <Link
    to="/forgot-password"
    className="account-forgot-link"
  >
    Forgot password?
  </Link>
</div>


            {/* LOGIN BUTTON */}
            <button
              type="submit"
              className="
                btn
                btn-solar
                w-100
                login-submit
              "
              disabled={loading}
            >

              {loading ? (
                <>

                  <span
                    className="
                      spinner-border
                      spinner-border-sm
                      me-2
                    "
                  />

                  Signing in...

                </>
              ) : (
                <>

                  <span>
                    Sign in to portal
                  </span>

                  <ArrowIcon />

                </>
              )}

            </button>

          </form>

          <div className="login-note">

            <span />

            <p>
              Secure • Connected • Sustainable
            </p>

            <span />

          </div>


          <p className="login-footer-message">
            Powering smarter community
            energy management.
          </p>

        </div>

      </section>

    </div>
  );
}

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

      <path
        d="
          M8 10V7
          a4 4 0 0 1 8 0v3
        "
      />

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

      <path
        d="
          M12 3
          5 6v5
          c0 5 3 8 7 10
          4-2 7-5 7-10
          V6l-7-3Z
        "
      />

      <path
        d="m9.5 12 1.7 1.7 3.5-4"
      />

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

      <path
        d="
          M17.5 19H7
          a5 5 0 0 1-.6-9.96
          A7 7 0 0 1 20 11
          a4 4 0 0 1-2.5 8Z
        "
      />

    </svg>
  );
}


function EnergyIcon() {
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

      <path
        d="M13 2 5.5 13H11l-1 9 8.5-12H13l0-8Z"
      />

    </svg>
  );
}