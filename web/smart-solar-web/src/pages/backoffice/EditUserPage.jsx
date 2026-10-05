import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";

import DashboardLayout from "../../components/DashboardLayout";

import {
  getUserById,
  updateUser
} from "../../services/userService";

import { getApiError } from "../../services/errorService";

// Displays the selected user's details and allows Backoffice to update them.
export default function EditUserPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [user, setUser] = useState(null);

  const [form, setForm] = useState({
    name: "",
    email: ""
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);

  const [error, setError] = useState("");

  useEffect(() => {

    // Retrieves the selected user's details and populates the edit form.
    async function loadUser() {
      try {
        const data = await getUserById(id);

        setUser(data);

        setForm({
          name: data.name || "",
          email: data.email || ""
        });
      } catch (err) {
        setError(
          getApiError(err, "Unable to load user.")
        );
      } finally {
        setLoading(false);
      }
    }

    loadUser();
  }, [id]);

  // Validates and submits the updated user details to the backend.
  async function handleSubmit(event) {
    event.preventDefault();

    setError("");

    if (form.name.trim().length < 2) {
      setError("Enter a valid full name.");
      return;
    }

    try {
      setSaving(true);

      await updateUser(id, {
        name: form.name.trim(),
        email: form.email.trim()
      });

      navigate("/backoffice/users", {
        replace: true
      });
    } catch (err) {
      setError(
        getApiError(err, "Unable to update user.")
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <DashboardLayout
      title="Edit User"
      subtitle="Update an existing Backoffice or Grid Operator account."
    >
      <div className="row justify-content-center">
        <div className="col-12 col-lg-8 col-xl-7">

          <div className="dashboard-card account-page-card">

            <div className="section-heading">
              <span className="eyebrow">
                USER MANAGEMENT
              </span>

              <h3>Account information</h3>

              <p>
                Update the selected user's personal details.
              </p>
            </div>

            {error && (
              <div className="alert alert-danger">
                {error}
              </div>
            )}

            {loading ? (
              <p>Loading user...</p>
            ) : user && (
              <form onSubmit={handleSubmit}>

                <div className="mb-3">
                  <label className="form-label">
                    Full name
                  </label>

                  <input
                    className="form-control app-input"
                    value={form.name}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        name: event.target.value
                      })
                    }
                    required
                  />
                </div>

                <div className="mb-3">
                  <label className="form-label">
                    Email address
                  </label>

                  <input
                    type="email"
                    className="form-control app-input"
                    value={form.email}
                    onChange={(event) =>
                      setForm({
                        ...form,
                        email: event.target.value
                      })
                    }
                    required
                  />
                </div>

                <div className="mb-3">
                  <label className="form-label">
                    Role
                  </label>

                  <input
                    className="form-control app-input"
                    value={user.role}
                    disabled
                  />
                </div>

                <div className="d-flex gap-2 flex-wrap">

                  <button
                    className="btn btn-solar"
                    type="submit"
                    disabled={saving}
                  >
                    {saving
                      ? "Saving..."
                      : "Save changes"}
                  </button>

                  <Link
                    className="btn btn-outline-secondary"
                    to="/backoffice/users"
                  >
                    Cancel
                  </Link>

                </div>

              </form>
            )}

          </div>

        </div>
      </div>
    </DashboardLayout>
  );
}