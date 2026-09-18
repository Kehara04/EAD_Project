import { useEffect, useMemo, useState } from "react";
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

export default function UserManagementPage() {
  const [users, setUsers] = useState([]);
  const [form, setForm] = useState(initialForm);
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("All");
  const [statusFilter, setStatusFilter] = useState("All");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [actionId, setActionId] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadUsers() {
    try {
      setLoading(true);
      setError("");
      const data = await getUsers();
      setUsers(data.filter((user) => user.role !== "Prosumer"));
    } catch (err) {
      setError(getApiError(err, "Unable to load users."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadUsers();
  }, []);

  const filteredUsers = useMemo(() => {
    const term = search.trim().toLowerCase();

    return users.filter((user) => {
      const matchesSearch =
        !term ||
        user.name?.toLowerCase().includes(term) ||
        user.email?.toLowerCase().includes(term);
      const matchesRole = roleFilter === "All" || user.role === roleFilter;
      const matchesStatus =
        statusFilter === "All" || user.status === statusFilter;

      return matchesSearch && matchesRole && matchesStatus;
    });
  }, [users, search, roleFilter, statusFilter]);

  async function handleSubmit(event) {
    event.preventDefault();
    setError("");
    setSuccess("");

    if (!form.name.trim() || !form.email.trim() || !form.password) {
      setError("Complete all required user fields.");
      return;
    }

    if (form.password.length < 6) {
      setError("Password must contain at least 6 characters.");
      return;
    }

    try {
      setSaving(true);
      await createUser({
        ...form,
        name: form.name.trim(),
        email: form.email.trim()
      });
      setForm(initialForm);
      setSuccess("User account created successfully.");
      await loadUsers();
    } catch (err) {
      setError(getApiError(err, "Unable to create the user account."));
    } finally {
      setSaving(false);
    }
  }

  async function handleStatusChange(user) {
    const nextStatus = user.status === "Active" ? "Deactivated" : "Active";
    const action = nextStatus === "Active" ? "activate" : "deactivate";

    if (!window.confirm(`Are you sure you want to ${action} ${user.name}?`)) {
      return;
    }

    try {
      setActionId(user.id);
      setError("");
      setSuccess("");
      await updateUserStatus(user.id, nextStatus);
      setSuccess(`User ${action}d successfully.`);
      await loadUsers();
    } catch (err) {
      setError(getApiError(err, "Unable to update user status."));
    } finally {
      setActionId("");
    }
  }

  return (
    <DashboardLayout
      title="User Management"
      subtitle="Create and control Backoffice and Grid Operator web accounts."
    >
      {error && <div className="alert alert-danger app-alert">{error}</div>}
      {success && <div className="alert alert-success app-alert">{success}</div>}

      <div className="row g-4">
        <div className="col-12 col-xl-4">
          <div className="dashboard-card sticky-xl-top form-card">
            <div className="section-heading">
              <span className="eyebrow">New account</span>
              <h3>Create web user</h3>
              <p>Grant Backoffice or Grid Operator access to the web application.</p>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="mb-3">
                <label className="form-label">Full name</label>
                <input
                  className="form-control app-input"
                  value={form.name}
                  onChange={(event) =>
                    setForm({ ...form, name: event.target.value })
                  }
                  placeholder="e.g. Nimal Perera"
                  disabled={saving}
                />
              </div>

              <div className="mb-3">
                <label className="form-label">Email address</label>
                <input
                  type="email"
                  className="form-control app-input"
                  value={form.email}
                  onChange={(event) =>
                    setForm({ ...form, email: event.target.value })
                  }
                  placeholder="name@example.com"
                  disabled={saving}
                />
              </div>

              <div className="mb-3">
                <label className="form-label">Temporary password</label>
                <input
                  type="password"
                  className="form-control app-input"
                  value={form.password}
                  onChange={(event) =>
                    setForm({ ...form, password: event.target.value })
                  }
                  placeholder="Minimum 6 characters"
                  disabled={saving}
                />
              </div>

              <div className="mb-4">
                <label className="form-label">Role</label>
                <select
                  className="form-select app-input"
                  value={form.role}
                  onChange={(event) =>
                    setForm({ ...form, role: event.target.value })
                  }
                  disabled={saving}
                >
                  <option value="GridOperator">Grid Operator</option>
                  <option value="Backoffice">Backoffice</option>
                </select>
              </div>

              <button className="btn btn-solar w-100" disabled={saving}>
                {saving ? "Creating account..." : "Create user"}
              </button>
            </form>
          </div>
        </div>

        <div className="col-12 col-xl-8">
          <div className="dashboard-card">
            <div className="card-heading-row flex-wrap gap-3">
              <div>
                <h3>Web application users</h3>
                <p>{filteredUsers.length} account(s) shown</p>
              </div>
            </div>

            <div className="filter-toolbar">
              <input
                className="form-control app-input search-input"
                placeholder="Search by name or email..."
                value={search}
                onChange={(event) => setSearch(event.target.value)}
              />

              <select
                className="form-select app-input filter-select"
                value={roleFilter}
                onChange={(event) => setRoleFilter(event.target.value)}
              >
                <option value="All">All roles</option>
                <option value="Backoffice">Backoffice</option>
                <option value="GridOperator">Grid Operator</option>
              </select>

              <select
                className="form-select app-input filter-select"
                value={statusFilter}
                onChange={(event) => setStatusFilter(event.target.value)}
              >
                <option value="All">All statuses</option>
                <option value="Active">Active</option>
                <option value="Deactivated">Deactivated</option>
              </select>
            </div>

            {loading ? (
              <div className="loading-state">
                <div className="spinner-border text-success" />
                <span>Loading users...</span>
              </div>
            ) : filteredUsers.length === 0 ? (
              <div className="empty-state">No matching web users found.</div>
            ) : (
              <div className="table-responsive">
                <table className="table app-table align-middle mb-0">
                  <thead>
                    <tr>
                      <th>User</th>
                      <th>Role</th>
                      <th>Status</th>
                      <th>Created</th>
                      <th className="text-end">Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredUsers.map((user) => (
                      <tr key={user.id}>
                        <td>
                          <div className="d-flex align-items-center gap-3">
                            <div className="table-avatar">
                              {user.name?.charAt(0).toUpperCase() || "U"}
                            </div>
                            <div>
                              <strong>{user.name}</strong>
                              <span className="table-subtext">{user.email}</span>
                            </div>
                          </div>
                        </td>
                        <td>
                          {user.role === "GridOperator" ? "Grid Operator" : user.role}
                        </td>
                        <td>
                          <StatusBadge status={user.status} />
                        </td>
                        <td>{formatDate(user.createdAt)}</td>
                        <td className="text-end">
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
                        </td>
                      </tr>
                    ))}
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

function formatDate(value) {
  if (!value) return "—";
  return new Intl.DateTimeFormat("en", {
    year: "numeric",
    month: "short",
    day: "2-digit"
  }).format(new Date(value));
}
