import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import DashboardLayout from "../../components/DashboardLayout";
import StatusBadge from "../../components/StatusBadge";
import {
  createUser,
  getUsers,
  updateUserStatus
} from "../../services/userService";
import { getApiError } from "../../services/errorService";

const initialForm = {
  name: "",
  email: "",
  password: "",
  role: "GridOperator"
};

const PASSWORD_REGEX =
  /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[^A-Za-z0-9])\S{8,64}$/;

const EMAIL_REGEX =
  /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

const NAME_REGEX =
  /^[A-Za-z][A-Za-z\s.'-]*$/;

  // Displays and manages Backoffice and Grid Operator user accounts.
export default function UserManagementPage() {
  const [users, setUsers] = useState([]);

  const [form, setForm] =
    useState(initialForm);

  const [errors, setErrors] =
    useState({});

  const [search, setSearch] =
    useState("");

  const [roleFilter, setRoleFilter] =
    useState("All");

  const [statusFilter, setStatusFilter] =
    useState("All");

  const [loading, setLoading] =
    useState(true);

  const [saving, setSaving] =
    useState(false);

  const [actionId, setActionId] =
    useState("");

  const [error, setError] =
    useState("");

  const [success, setSuccess] =
    useState("");

  // Retrieves web application users from the backend, excluding Prosumers.
  async function loadUsers() {
    try {
      setLoading(true);
      setError("");

      const data =
        await getUsers();

      setUsers(
        data.filter(
          (user) =>
            user.role !== "Prosumer"
        )
      );
    } catch (err) {
      setError(
        getApiError(
          err,
          "Unable to load users."
        )
      );
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, []);

  // Filters user accounts by search text, role, and account status.
  const filteredUsers =
    useMemo(() => {
      const term =
        search
          .trim()
          .toLowerCase();

      return users.filter(
        (user) => {
          const matchesSearch =
            !term ||
            user.name
              ?.toLowerCase()
              .includes(term) ||
            user.email
              ?.toLowerCase()
              .includes(term);

          const matchesRole =
            roleFilter === "All" ||
            user.role === roleFilter;

          const matchesStatus =
            statusFilter === "All" ||
            user.status === statusFilter;

          return (
            matchesSearch &&
            matchesRole &&
            matchesStatus
          );
        }
      );
    }, [
      users,
      search,
      roleFilter,
      statusFilter
    ]);

  // Updates the selected form field and clears its validation error.
  function handleFieldChange(
    field,
    value
  ) {
    setForm((previous) => ({
      ...previous,
      [field]: value
    }));

    setErrors((previous) => ({
      ...previous,
      [field]: ""
    }));

    setError("");
    setSuccess("");
  }

  // Validates the user's name, email, password, and selected role.
  function validateForm() {
    const newErrors = {};

    const name =
      form.name.trim();

    const email =
      form.email.trim();

    const password =
      form.password;

    if (!name) {
      newErrors.name =
        "Full name is required.";
    } else if (
      name.length < 2
    ) {
      newErrors.name =
        "Name must contain at least 2 characters.";
    } else if (
      name.length > 100
    ) {
      newErrors.name =
        "Name cannot exceed 100 characters.";
    } else if (
      !NAME_REGEX.test(name)
    ) {
      newErrors.name =
        "Name can contain only letters, spaces, apostrophes, periods and hyphens.";
    }

    if (!email) {
      newErrors.email =
        "Email address is required.";
    } else if (
      !EMAIL_REGEX.test(email)
    ) {
      newErrors.email =
        "Enter a valid email address.";
    } else if (
      email.length > 150
    ) {
      newErrors.email =
        "Email address is too long.";
    }

    if (!password) {
      newErrors.password =
        "Password is required.";
    } else if (
      password.length < 8
    ) {
      newErrors.password =
        "Password must contain at least 8 characters.";
    } else if (
      password.length > 64
    ) {
      newErrors.password =
        "Password cannot exceed 64 characters.";
    } else if (
      !PASSWORD_REGEX.test(
        password
      )
    ) {
      newErrors.password =
        "Password must contain uppercase, lowercase, number and special character with no spaces.";
    }

    if (
      form.role !==
        "Backoffice" &&
      form.role !==
        "GridOperator"
    ) {
      newErrors.role =
        "Select a valid user role.";
    }

    setErrors(newErrors);

    return (
      Object.keys(
        newErrors
      ).length === 0
    );
  }

  // Validates and submits the new user account details to the backend.
  async function handleSubmit(
    event
  ) {
    event.preventDefault();

    setError("");
    setSuccess("");

    if (!validateForm()) {
      return;
    }

    try {
      setSaving(true);

      await createUser({
        name:
          form.name.trim(),

        email:
          form.email
            .trim()
            .toLowerCase(),

        password:
          form.password,

        role:
          form.role
      });

      setForm(initialForm);
      setErrors({});

      setSuccess(
        "User account created successfully."
      );

      await loadUsers();
    } catch (err) {
      setError(
        getApiError(
          err,
          "Unable to create the user account."
        )
      );
    } finally {
      setSaving(false);
    }
  }

  // Confirms and updates the selected user's activation status.
  async function handleStatusChange(
    user
  ) {
    const nextStatus =
      user.status === "Active"
        ? "Deactivated"
        : "Active";

    const action =
      nextStatus === "Active"
        ? "activate"
        : "deactivate";

    const confirmed =
      window.confirm(
        `Are you sure you want to ${action} ${user.name}?`
      );

    if (!confirmed) {
      return;
    }

    try {
      setActionId(user.id);
      setError("");
      setSuccess("");

      await updateUserStatus(
        user.id,
        nextStatus
      );

      setSuccess(
        `User ${
          action === "activate"
            ? "activated"
            : "deactivated"
        } successfully.`
      );

      await loadUsers();
    } catch (err) {
      setError(
        getApiError(
          err,
          "Unable to update user status."
        )
      );
    } finally {
      setActionId("");
    }
  }

  return (
    <DashboardLayout
      title="User Management"
      subtitle="Create and control Backoffice and Grid Operator web accounts."
    >
      {error && (
        <div className="alert alert-danger app-alert">
          {error}
        </div>
      )}

      {success && (
        <div className="alert alert-success app-alert">
          {success}
        </div>
      )}

      <div className="row g-4">
        <div className="col-12 col-xl-4">
          <div className="dashboard-card sticky-xl-top form-card">
            <div className="section-heading">
              <span className="eyebrow">
                New account
              </span>

              <h3>
                Create web user
              </h3>

              <p>
                Grant Backoffice
                or Grid Operator
                access to the web
                application.
              </p>
            </div>

            <form
              onSubmit={
                handleSubmit
              }
              noValidate
            >
              <div className="mb-3">
                <label className="form-label">
                  Full name
                </label>

                <input
                  type="text"
                  className={`form-control app-input ${
                    errors.name
                      ? "is-invalid"
                      : ""
                  }`}
                  value={
                    form.name
                  }
                  onChange={(
                    event
                  ) =>
                    handleFieldChange(
                      "name",
                      event.target
                        .value
                    )
                  }
                  placeholder="e.g. Nimal Perera"
                  maxLength={100}
                  disabled={
                    saving
                  }
                />

                {errors.name && (
                  <div className="invalid-feedback">
                    {
                      errors.name
                    }
                  </div>
                )}
              </div>

              <div className="mb-3">
                <label className="form-label">
                  Email address
                </label>

                <input
                  type="email"
                  className={`form-control app-input ${
                    errors.email
                      ? "is-invalid"
                      : ""
                  }`}
                  value={
                    form.email
                  }
                  onChange={(
                    event
                  ) =>
                    handleFieldChange(
                      "email",
                      event.target
                        .value
                    )
                  }
                  placeholder="name@example.com"
                  maxLength={150}
                  autoComplete="email"
                  disabled={
                    saving
                  }
                />

                {errors.email && (
                  <div className="invalid-feedback">
                    {
                      errors.email
                    }
                  </div>
                )}
              </div>

              <div className="mb-3">
                <label className="form-label">
                  Temporary password
                </label>

                <input
                  type="password"
                  className={`form-control app-input ${
                    errors.password
                      ? "is-invalid"
                      : ""
                  }`}
                  value={
                    form.password
                  }
                  onChange={(
                    event
                  ) =>
                    handleFieldChange(
                      "password",
                      event.target
                        .value
                    )
                  }
                  placeholder="Create a secure password"
                  maxLength={64}
                  autoComplete="new-password"
                  disabled={
                    saving
                  }
                />

                {errors.password && (
                  <div className="invalid-feedback">
                    {
                      errors.password
                    }
                  </div>
                )}

                <div className="form-text mt-2">
                  Minimum 8
                  characters with
                  uppercase,
                  lowercase,
                  number and special
                  character. Spaces
                  are not allowed.
                </div>
              </div>

              <div className="mb-4">
                <label className="form-label">
                  Role
                </label>

                <select
                  className={`form-select app-input ${
                    errors.role
                      ? "is-invalid"
                      : ""
                  }`}
                  value={
                    form.role
                  }
                  onChange={(
                    event
                  ) =>
                    handleFieldChange(
                      "role",
                      event.target
                        .value
                    )
                  }
                  disabled={
                    saving
                  }
                >
                  <option value="GridOperator">
                    Grid Operator
                  </option>

                  <option value="Backoffice">
                    Backoffice
                  </option>
                </select>

                {errors.role && (
                  <div className="invalid-feedback">
                    {
                      errors.role
                    }
                  </div>
                )}
              </div>

              <button
                type="submit"
                className="btn btn-solar w-100"
                disabled={
                  saving
                }
              >
                {saving
                  ? "Creating account..."
                  : "Create user"}
              </button>
            </form>
          </div>
        </div>

        <div className="col-12 col-xl-8">
          <div className="dashboard-card">
            <div className="card-heading-row flex-wrap gap-3">
              <div>
                <h3>
                  Web application
                  users
                </h3>

                <p>
                  {
                    filteredUsers.length
                  }{" "}
                  account(s) shown
                </p>
              </div>
            </div>

            <div className="filter-toolbar">
              <input
                className="form-control app-input search-input"
                placeholder="Search by name or email..."
                value={
                  search
                }
                onChange={(
                  event
                ) =>
                  setSearch(
                    event.target
                      .value
                  )
                }
              />

              <select
                className="form-select app-input filter-select"
                value={
                  roleFilter
                }
                onChange={(
                  event
                ) =>
                  setRoleFilter(
                    event.target
                      .value
                  )
                }
              >
                <option value="All">
                  All roles
                </option>

                <option value="Backoffice">
                  Backoffice
                </option>

                <option value="GridOperator">
                  Grid Operator
                </option>
              </select>

              <select
                className="form-select app-input filter-select"
                value={
                  statusFilter
                }
                onChange={(
                  event
                ) =>
                  setStatusFilter(
                    event.target
                      .value
                  )
                }
              >
                <option value="All">
                  All statuses
                </option>

                <option value="Active">
                  Active
                </option>

                <option value="Deactivated">
                  Deactivated
                </option>
              </select>
            </div>

            {loading ? (
              <div className="loading-state">
                <div className="spinner-border text-success" />

                <span>
                  Loading users...
                </span>
              </div>
            ) : filteredUsers.length ===
              0 ? (
              <div className="empty-state">
                No matching web
                users found.
              </div>
            ) : (
              <div className="table-responsive">
                <table className="table app-table align-middle mb-0">
                  <thead>
                    <tr>
                      <th>
                        User
                      </th>

                      <th>
                        Role
                      </th>

                      <th>
                        Status
                      </th>

                      <th>
                        Created
                      </th>

                      <th className="text-end">
                        Action
                      </th>
                    </tr>
                  </thead>

                  <tbody>
                    {filteredUsers.map(
                      (user) => (
                        <tr
                          key={
                            user.id
                          }
                        >
                          <td>
                            <div className="d-flex align-items-center gap-3">
                              <div className="table-avatar">
                                {user.name
                                  ?.charAt(
                                    0
                                  )
                                  .toUpperCase() ||
                                  "U"}
                              </div>

                              <div>
                                <strong>
                                  {
                                    user.name
                                  }
                                </strong>

                                <span className="table-subtext">
                                  {
                                    user.email
                                  }
                                </span>
                              </div>
                            </div>
                          </td>

                          <td>
                            {user.role ===
                            "GridOperator"
                              ? "Grid Operator"
                              : user.role}
                          </td>

                          <td>
                            <StatusBadge
                              status={
                                user.status
                              }
                            />
                          </td>

                          <td>
                            {formatDate(
                              user.createdAt
                            )}
                          </td>

                          
<td className="text-end">

  <div className="d-flex justify-content-end align-items-center gap-2">

    {/* ====================================
        EDIT USER
    ==================================== */}

    <Link
      to={`/backoffice/users/${user.id}/edit`}
      className="btn btn-outline-success btn-sm action-button"
    >
      Edit
    </Link>


    {/* ====================================
        ACTIVATE / DEACTIVATE USER
        Existing functionality unchanged
    ==================================== */}

    <button
      type="button"
      className={
        user.status === "Active"
          ? "btn btn-outline-danger btn-sm action-button"
          : "btn btn-outline-success btn-sm action-button"
      }
      disabled={actionId === user.id}
      onClick={() => handleStatusChange(user)}
    >
      {actionId === user.id
        ? "Updating..."
        : user.status === "Active"
          ? "Deactivate"
          : "Activate"}
    </button>

  </div>

</td>
                        </tr>
                      )
                    )}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}

// Formats a user's account creation date for display in the table.
function formatDate(value) {
  if (!value) {
    return "—";
  }

  return new Intl.DateTimeFormat(
    "en",
    {
      year: "numeric",
      month: "short",
      day: "2-digit"
    }
  ).format(
    new Date(value)
  );
}