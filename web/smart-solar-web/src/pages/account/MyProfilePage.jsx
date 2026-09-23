
import {
  useEffect,
  useState
} from "react";

import { Link } from "react-router-dom";

import DashboardLayout
  from "../../components/DashboardLayout";

import {
  getMyProfile,
  updateMyProfile
} from "../../services/accountService";

import { getApiError }
  from "../../services/errorService";


export default function MyProfilePage() {

  const [profile, setProfile] =
    useState(null);

  const [form, setForm] =
    useState({
      name: "",
      email: ""
    });

  const [loading, setLoading] =
    useState(true);

  const [saving, setSaving] =
    useState(false);

  const [error, setError] =
    useState("");

  const [success, setSuccess] =
    useState("");


  /* ==========================================
     LOAD CURRENT USER PROFILE
  ========================================== */

  useEffect(() => {

    async function loadProfile() {

      try {

        const data =
          await getMyProfile();

        setProfile(data);

        setForm({
          name: data.name || "",
          email: data.email || ""
        });

      } catch (err) {

        setError(
          getApiError(
            err,
            "Unable to load your profile."
          )
        );

      } finally {

        setLoading(false);

      }
    }

    loadProfile();

  }, []);


  /* ==========================================
     UPDATE PROFILE
  ========================================== */

  async function handleSubmit(event) {

    event.preventDefault();

    setError("");
    setSuccess("");

    const name =
      form.name.trim();

    const email =
      form.email.trim();


    if (name.length < 2) {

      setError(
        "Enter a valid full name."
      );

      return;
    }


    if (
      !/^[A-Za-z][A-Za-z\s.'-]*$/.test(name)
    ) {

      setError(
        "Name can contain only letters, spaces, apostrophes, periods and hyphens."
      );

      return;
    }


    if (
      !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)
    ) {

      setError(
        "Enter a valid email address."
      );

      return;
    }


    try {

      setSaving(true);

      const updated =
        await updateMyProfile({
          name,
          email
        });

      setProfile(updated);

      setForm({
        name: updated.name || "",
        email: updated.email || ""
      });

      setSuccess(
        "Profile updated successfully."
      );

    } catch (err) {

      setError(
        getApiError(
          err,
          "Unable to update profile."
        )
      );

    } finally {

      setSaving(false);

    }
  }


  return (

    <DashboardLayout
      title="My Profile"
      subtitle="Manage your personal information and account security."
    >

      <div className="row justify-content-center">

        <div className="col-12 col-lg-9 col-xl-8">


          {/* ====================================
              PROFILE HEADING
          ===================================== */}

          <div className="account-profile-intro">

            <div className="account-profile-avatar">

              {(profile?.name || "U")
                .charAt(0)
                .toUpperCase()}

            </div>


            <div>

              <span className="eyebrow">
                MY ACCOUNT
              </span>

              <h2>
                {profile?.name || "My Profile"}
              </h2>

              <p>
                {profile?.email || "Smart Solar account"}
              </p>

            </div>

          </div>


          {/* ====================================
              PERSONAL INFORMATION CARD
          ===================================== */}

          <div className="dashboard-card account-page-card">

            <div className="section-heading">

              <span className="eyebrow">
                PERSONAL INFORMATION
              </span>

              <h3>
                Profile details
              </h3>

              <p>
                View and update your account information.
              </p>

            </div>


            {/* ERROR MESSAGE */}

            {error && (

              <div
                className="alert alert-danger app-alert"
                role="alert"
              >
                {error}
              </div>

            )}


            {/* SUCCESS MESSAGE */}

            {success && (

              <div
                className="alert alert-success app-alert"
                role="status"
              >
                {success}
              </div>

            )}


            {loading ? (

              <div className="loading-state">

                <div className="spinner-border text-success" />

                <span>
                  Loading your profile...
                </span>

              </div>

            ) : profile && (

              <form onSubmit={handleSubmit}>

                {/* FULL NAME */}

                <div className="mb-3">

                  <label
                    className="form-label"
                    htmlFor="profileName"
                  >
                    Full name
                  </label>

                  <input
                    id="profileName"
                    type="text"
                    className="form-control app-input"
                    value={form.name}
                    maxLength={100}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        name: event.target.value
                      })
                    }
                    disabled={saving}
                    required
                  />

                </div>


                {/* EMAIL */}

                <div className="mb-3">

                  <label
                    className="form-label"
                    htmlFor="profileEmail"
                  >
                    Email address
                  </label>

                  <input
                    id="profileEmail"
                    type="email"
                    className="form-control app-input"
                    value={form.email}
                    maxLength={150}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        email: event.target.value
                      })
                    }
                    disabled={saving}
                    required
                  />

                </div>


                {/* ROLE */}

                <div className="mb-3">

                  <label
                    className="form-label"
                    htmlFor="profileRole"
                  >
                    Account role
                  </label>

                  <input
                    id="profileRole"
                    className="form-control app-input"
                    value={
                      profile.role === "GridOperator"
                        ? "Grid Operator"
                        : profile.role || ""
                    }
                    disabled
                  />

                </div>


                {/* STATUS */}

                <div className="mb-4">

                  <label
                    className="form-label"
                    htmlFor="profileStatus"
                  >
                    Account status
                  </label>

                  <input
                    id="profileStatus"
                    className="form-control app-input"
                    value={profile.status || ""}
                    disabled
                  />

                </div>


                {/* SAVE BUTTON */}

                <button
                  type="submit"
                  className="btn btn-solar"
                  disabled={saving}
                >

                  {saving
                    ? "Saving changes..."
                    : "Save changes"}

                </button>

              </form>

            )}

          </div>


          {/* ====================================
              ACCOUNT SECURITY CARD
          ===================================== */}

          <div className="dashboard-card account-security-card">

            <div className="account-security-content">

              <div className="account-security-icon">

                <svg
                  width="24"
                  height="24"
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
                    d="M8 10V7a4 4 0 0 1 8 0v3"
                  />

                </svg>

              </div>


              <div className="account-security-copy">

                <span className="eyebrow">
                  ACCOUNT SECURITY
                </span>

                <h3>
                  Password & security
                </h3>

                <p>
                  Keep your account protected by
                  updating your password regularly.
                </p>

              </div>

            </div>


            {/* CHANGE PASSWORD BUTTON */}

            <Link
              to="/account/change-password"
              className="btn btn-solar account-security-button"
            >

              Change password

              <span aria-hidden="true">
                →
              </span>

            </Link>

          </div>

        </div>

      </div>

    </DashboardLayout>

  );
}