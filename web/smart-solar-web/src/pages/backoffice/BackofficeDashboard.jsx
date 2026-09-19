import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import DashboardLayout from "../../components/DashboardLayout";
import StatCard from "../../components/StatCard";
import StatusBadge from "../../components/StatusBadge";
import { getStations } from "../../services/stationService";
import { getUsers } from "../../services/userService";
import { getProsumers } from "../../services/prosumerService";
import { getApiError } from "../../services/errorService";

export default function BackofficeDashboard() {
  const [stations, setStations] = useState([]);
  const [users, setUsers] = useState([]);
  const [prosumers, setProsumers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadDashboard() {
      try {
        setLoading(true);
        setError("");
        const [usersData, prosumersData, stationsData] = await Promise.all([
          getUsers(),
          getProsumers(),
          getStations()
        ]);
        setStations(stationsData);
        setUsers(usersData);
        setProsumers(prosumersData);
      } catch (err) {
        setError(getApiError(err, "Unable to load dashboard data."));
      } finally {
        setLoading(false);
      }
    }

    loadDashboard();
  }, []);

  const metrics = useMemo(() => {
    const webUsers = users.filter((user) => user.role !== "Prosumer");

    return {
      totalStations: stations.length,
      activeStations: stations.filter((s) => s.status === "Active").length,
      inactiveStations: stations.filter((s) => s.status === "Inactive").length,
      availableStations: stations.filter((s) => s.availableSlots > 0).length,
      webUsers: webUsers.length,
      activeWebUsers: webUsers.filter((user) => user.status === "Active").length,
      prosumers: prosumers.length,
      pending: prosumers.filter((p) => p.status === "Pending").length,
      deactivation: prosumers.filter(
        (p) => p.status === "DeactivationRequested"
      ).length
    };
  }, [users, prosumers, stations]);

  const recentProsumers = useMemo(
    () =>
      [...prosumers]
        .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
        .slice(0, 5),
    [prosumers]
  );

  return (
    <DashboardLayout
      title="Backoffice Overview"
      subtitle="Monitor account activity and manage Smart Solar access from one place."
    >
      {error && <div className="alert alert-danger app-alert">{error}</div>}

      <div className="row g-4 mb-4">
        <div className="col-12 col-md-6 col-xl-3">
          <StatCard
            label="Web users"
            value={loading ? "—" : metrics.webUsers}
            helper={`${metrics.activeWebUsers} active accounts`}
            icon="WU"
          />
        </div>
        <div className="col-12 col-md-6 col-xl-3">
          <StatCard
            label="Total prosumers"
            value={loading ? "—" : metrics.prosumers}
            helper="Registered solar prosumers"
            icon="PR"
          />
        </div>
        <div className="col-12 col-md-6 col-xl-3">
          <StatCard
            label="Pending activation"
            value={loading ? "—" : metrics.pending}
            helper="Require Backoffice review"
            icon="PA"
          />
        </div>
        <div className="col-12 col-md-6 col-xl-3">
          <StatCard
            label="Deactivation requests"
            value={loading ? "—" : metrics.deactivation}
            helper="Awaiting action"
            icon="DR"
          />
        </div>
      </div>

      <div className="card-heading-row flex-wrap gap-3">
        <div>
          <h3>Station network</h3>
          <p>Capacity and operating status across your microgrid stations.</p>
        </div>
        <Link className="btn btn-soft" to="/backoffice/stations">Manage stations</Link>
      </div>
      <div className="row g-4 mb-4">
        {[
          ["Total stations", metrics.totalStations, "All registered stations", "ST"],
          ["Active stations", metrics.activeStations, "Currently active in the network", "AS"],
          ["Inactive stations", metrics.inactiveStations, "Currently inactive", "IS"],
          ["Stations with available slots", metrics.availableStations, "With free slots, across all statuses", "AV"]
        ].map(([label, value, helper, icon]) => (
          <div className="col-12 col-md-6 col-xl-3" key={label}>
            <StatCard label={label} value={loading || error ? "—" : value} helper={helper} icon={icon} />
          </div>
        ))}
      </div>

      <div className="row g-4">
        <div className="col-12 col-xl-8">
          <div className="dashboard-card">
            <div className="card-heading-row">
              <div>
                <h3>Recent prosumer registrations</h3>
                <p>Newest accounts received through the mobile registration flow.</p>
              </div>
              <Link className="btn btn-soft" to="/backoffice/prosumers">
                View all
              </Link>
            </div>

            {loading ? (
              <div className="loading-state">
                <div className="spinner-border text-success" />
                <span>Loading registrations...</span>
              </div>
            ) : recentProsumers.length === 0 ? (
              <div className="empty-state">No prosumer registrations yet.</div>
            ) : (
              <div className="table-responsive">
                <table className="table app-table align-middle mb-0">
                  <thead>
                    <tr>
                      <th>Prosumer</th>
                      <th>NIC</th>
                      <th>Status</th>
                      <th>Registered</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentProsumers.map((prosumer) => (
                      <tr key={prosumer.nic}>
                        <td>
                          <strong>{prosumer.name}</strong>
                          <span className="table-subtext">{prosumer.email}</span>
                        </td>
                        <td>{prosumer.nic}</td>
                        <td>
                          <StatusBadge status={prosumer.status} />
                        </td>
                        <td>{formatDate(prosumer.createdAt)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>

        <div className="col-12 col-xl-4">
          <div className="dashboard-card h-100">
            <div className="card-heading-row">
              <div>
                <h3>Quick actions</h3>
                <p>Common account-management tasks.</p>
              </div>
            </div>

            <div className="quick-actions">
              <Link to="/backoffice/users" className="quick-action-item">
                <span className="quick-action-icon">+</span>
                <div>
                  <strong>Create web user</strong>
                  <span>Add Backoffice or Grid Operator access</span>
                </div>
              </Link>

              <Link to="/backoffice/prosumers" className="quick-action-item">
                <span className="quick-action-icon">✓</span>
                <div>
                  <strong>Review pending accounts</strong>
                  <span>Activate newly registered prosumers</span>
                </div>
              </Link>

              <Link to="/backoffice/prosumers" className="quick-action-item">
                <span className="quick-action-icon">!</span>
                <div>
                  <strong>Deactivation requests</strong>
                  <span>Review and approve account requests</span>
                </div>
              </Link>
            </div>
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
