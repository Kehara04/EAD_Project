import { useEffect, useMemo, useState } from "react";
import DashboardLayout from "../../components/DashboardLayout";
import StatusBadge from "../../components/StatusBadge";
import {
  activateProsumer,
  deactivateProsumer,
  getProsumers,
  reactivateProsumer
} from "../../services/prosumerService";
import { getApiError } from "../../services/errorService";

export default function ProsumerManagementPage() {
  const [prosumers, setProsumers] = useState([]);
  const [search, setSearch] = useState("");
  const [statusFilter, setStatusFilter] = useState("All");
  const [loading, setLoading] = useState(true);
  const [actionNic, setActionNic] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  async function loadProsumers() {
    try {
      setLoading(true);
      setError("");
      setProsumers(await getProsumers());
    } catch (err) {
      setError(getApiError(err, "Unable to load prosumers."));
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadProsumers();
  }, []);

  const counts = useMemo(
    () => ({
      all: prosumers.length,
      pending: prosumers.filter((p) => p.status === "Pending").length,
      active: prosumers.filter((p) => p.status === "Active").length,
      requested: prosumers.filter(
        (p) => p.status === "DeactivationRequested"
      ).length,
      deactivated: prosumers.filter((p) => p.status === "Deactivated").length
    }),
    [prosumers]
  );

  const filteredProsumers = useMemo(() => {
    const term = search.trim().toLowerCase();

    return prosumers.filter((prosumer) => {
      const matchesSearch =
        !term ||
        prosumer.nic?.toLowerCase().includes(term) ||
        prosumer.name?.toLowerCase().includes(term) ||
        prosumer.email?.toLowerCase().includes(term) ||
        prosumer.phone?.toLowerCase().includes(term);

      const matchesStatus =
        statusFilter === "All" || prosumer.status === statusFilter;

      return matchesSearch && matchesStatus;
    });
  }, [prosumers, search, statusFilter]);

  async function runAction(prosumer, action) {
    const actionLabels = {
      activate: "activate",
      deactivate: "deactivate",
      approveDeactivation: "approve the deactivation request for",
      reactivate: "reactivate"
    };

    const label = actionLabels[action];

    if (!window.confirm(`Are you sure you want to ${label} ${prosumer.name}?`)) {
      return;
    }

    try {
      setActionNic(prosumer.nic);
      setError("");
      setSuccess("");

      if (action === "activate") {
        await activateProsumer(prosumer.nic);
        setSuccess("Prosumer activated successfully.");
      } else if (action === "reactivate") {
        await reactivateProsumer(prosumer.nic);
        setSuccess("Prosumer reactivated successfully.");
      } else {
        await deactivateProsumer(prosumer.nic);
        setSuccess("Prosumer deactivated successfully.");
      }

      await loadProsumers();
    } catch (err) {
      setError(getApiError(err, "Unable to update the Prosumer account."));
    } finally {
      setActionNic("");
    }
  }

  return (
    <DashboardLayout
      title="Prosumer Management"
      subtitle="Review registrations, manage account status and process deactivation requests."
    >
      {error && <div className="alert alert-danger app-alert">{error}</div>}
      {success && <div className="alert alert-success app-alert">{success}</div>}

      <div className="prosumer-summary-grid mb-4">
        <SummaryButton
          label="All"
          value={counts.all}
          active={statusFilter === "All"}
          onClick={() => setStatusFilter("All")}
        />
        <SummaryButton
          label="Pending"
          value={counts.pending}
          active={statusFilter === "Pending"}
          onClick={() => setStatusFilter("Pending")}
        />
        <SummaryButton
          label="Active"
          value={counts.active}
          active={statusFilter === "Active"}
          onClick={() => setStatusFilter("Active")}
        />
        <SummaryButton
          label="Deactivation requests"
          value={counts.requested}
          active={statusFilter === "DeactivationRequested"}
          onClick={() => setStatusFilter("DeactivationRequested")}
        />
        <SummaryButton
          label="Deactivated"
          value={counts.deactivated}
          active={statusFilter === "Deactivated"}
          onClick={() => setStatusFilter("Deactivated")}
        />
      </div>

      <div className="dashboard-card">
        <div className="card-heading-row flex-wrap gap-3">
          <div>
            <h3>Prosumer accounts</h3>
            <p>{filteredProsumers.length} account(s) shown</p>
          </div>

          <input
            className="form-control app-input search-input"
            placeholder="Search NIC, name, email or phone..."
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </div>

        {loading ? (
          <div className="loading-state">
            <div className="spinner-border text-success" />
            <span>Loading prosumer accounts...</span>
          </div>
        ) : filteredProsumers.length === 0 ? (
          <div className="empty-state">No Prosumer accounts match this filter.</div>
        ) : (
          <div className="table-responsive">
            <table className="table app-table align-middle mb-0">
              <thead>
                <tr>
                  <th>Prosumer</th>
                  <th>NIC</th>
                  <th>Contact</th>
                  <th>Status</th>
                  <th>Updated</th>
                  <th className="text-end">Action</th>
                </tr>
              </thead>
              <tbody>
                {filteredProsumers.map((prosumer) => (
                  <tr key={prosumer.nic}>
                    <td>
                      <div className="d-flex align-items-center gap-3">
                        <div className="table-avatar prosumer-avatar">
                          {prosumer.name?.charAt(0).toUpperCase() || "P"}
                        </div>
                        <div>
                          <strong>{prosumer.name}</strong>
                          <span className="table-subtext">{prosumer.address || "No address"}</span>
                        </div>
                      </div>
                    </td>
                    <td className="font-monospace small">{prosumer.nic}</td>
                    <td>
                      <span>{prosumer.email}</span>
                      <span className="table-subtext">{prosumer.phone}</span>
                    </td>
                    <td>
                      <StatusBadge status={prosumer.status} />
                    </td>
                    <td>{formatDate(prosumer.updatedAt || prosumer.createdAt)}</td>
                    <td className="text-end">
                      <ProsumerAction
                        prosumer={prosumer}
                        busy={actionNic === prosumer.nic}
                        onAction={runAction}
                      />
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </DashboardLayout>
  );
}

function SummaryButton({ label, value, active, onClick }) {
  return (
    <button
      type="button"
      className={`summary-filter ${active ? "active" : ""}`}
      onClick={onClick}
    >
      <span>{label}</span>
      <strong>{value}</strong>
    </button>
  );
}

function ProsumerAction({ prosumer, busy, onAction }) {
  if (busy) {
    return (
      <button className="btn btn-secondary btn-sm action-button" disabled>
        Updating...
      </button>
    );
  }

  if (prosumer.status === "Pending") {
    return (
      <button
        className="btn btn-success btn-sm action-button"
        onClick={() => onAction(prosumer, "activate")}
      >
        Activate
      </button>
    );
  }

  if (prosumer.status === "Active") {
    return (
      <button
        className="btn btn-outline-danger btn-sm action-button"
        onClick={() => onAction(prosumer, "deactivate")}
      >
        Deactivate
      </button>
    );
  }

  if (prosumer.status === "DeactivationRequested") {
    return (
      <button
        className="btn btn-danger btn-sm action-button"
        onClick={() => onAction(prosumer, "approveDeactivation")}
      >
        Approve deactivation
      </button>
    );
  }

  if (prosumer.status === "Deactivated") {
    return (
      <button
        className="btn btn-outline-success btn-sm action-button"
        onClick={() => onAction(prosumer, "reactivate")}
      >
        Reactivate
      </button>
    );
  }

  return <span className="text-muted">No action</span>;
}

function formatDate(value) {
  if (!value) return "—";
  return new Intl.DateTimeFormat("en", {
    year: "numeric",
    month: "short",
    day: "2-digit"
  }).format(new Date(value));
}
