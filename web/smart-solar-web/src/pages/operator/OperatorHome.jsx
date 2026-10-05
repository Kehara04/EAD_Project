import { useState, useEffect } from "react";
import DashboardLayout from "../../components/DashboardLayout";
import { getOperatorDashboardStats } from "../../services/operatorService";

// Formats reservation dates and times for display in the dashboard.
const formatDateTime = (value) => {
  if (!value) return "—";

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return "—";

  return date.toLocaleString([], {
    dateStyle: "medium",
    timeStyle: "short"
  });
};

// Displays Grid Operator statistics and completed reservation history.
export default function OperatorHome() {
  const [stats, setStats] = useState(null);
  const [completedHistory, setCompletedHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Retrieves dashboard statistics and sorts completed reservations by date.
  const loadDashboard = async () => {
    setLoading(true);
    setError("");

    try {
      const statsResponse = await getOperatorDashboardStats();

      setStats(statsResponse);

      const history = statsResponse.completedHistory || [];
      const sorted = [...history].sort((a, b) => {
        const aDate = new Date(a.completedAt || a.updatedAt || a.scheduledAt || 0).getTime();
        const bDate = new Date(b.completedAt || b.updatedAt || b.scheduledAt || 0).getTime();
        return bDate - aDate;
      });

      setCompletedHistory(sorted);
    } catch (err) {
      console.error(err);
      setError("Could not load dashboard stats.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboard();
  }, []);

  return (
    <DashboardLayout
      title="Grid Operator Workspace"
      subtitle="Operational station, booking and QR tools will be connected here."
    >
      <div className="row g-4">
        <div className="col-12 col-lg-8">
          <div className="dashboard-card operator-welcome mb-4">
            <span className="eyebrow">Operator access active</span>
            <h3>Welcome to Smart Solar operations</h3>
            <p>
              Your authentication and role-based access are working. Station,
              reservation and QR verification modules can be connected to this
              workspace by the relevant component owners.
            </p>
          </div>

          <h4 className="mb-3">Today's Reservations</h4>
          {loading ? (
            <p>Loading stats...</p>
          ) : error ? (
            <p className="text-danger">{error}</p>
          ) : (
            <>
              <div className="dashboard-card mb-4">
                <div className="d-flex align-items-center justify-content-between">
                  <div>
                    <h2 className="mb-0 display-4 fw-bold">{stats?.todayTotal || 0}</h2>
                    <small className="text-muted">total bookings scheduled</small>
                  </div>
                </div>
              </div>

              <div className="row g-3 mb-4">
                <div className="col-6 col-md-3">
                  <div className="dashboard-card text-center">
                    <small className="text-muted mb-2 d-block">Pending</small>
                    <h3 className="mb-0 text-warning">{stats?.pendingCount || 0}</h3>
                  </div>
                </div>
                <div className="col-6 col-md-3">
                  <div className="dashboard-card text-center">
                    <small className="text-muted mb-2 d-block">Approved</small>
                    <h3 className="mb-0 text-success">{stats?.approvedCount || 0}</h3>
                  </div>
                </div>
                <div className="col-6 col-md-3">
                  <div className="dashboard-card text-center">
                    <small className="text-muted mb-2 d-block">Completed</small>
                    <h3 className="mb-0 text-info">{stats?.completedCount || 0}</h3>
                  </div>
                </div>
                <div className="col-6 col-md-3">
                  <div className="dashboard-card text-center">
                    <small className="text-muted mb-2 d-block">Cancelled</small>
                    <h3 className="mb-0 text-danger">{stats?.cancelledCount || 0}</h3>
                  </div>
                </div>
              </div>

              <h4 className="mb-3">Station Completions Today</h4>
              <div className="dashboard-card mb-4">
                {stats?.stationSummaries && stats.stationSummaries.length > 0 ? (
                  stats.stationSummaries.map((s) => (
                    <div
                      key={s.stationId}
                      className="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom"
                    >
                      <span className="fw-bold">{s.stationName}</span>
                      <span className="text-success">{s.completedToday} completed today</span>
                    </div>
                  ))
                ) : (
                  <p className="text-muted mb-0" style={{ fontSize: "13px" }}>
                    No completed transfers at any station today.
                  </p>
                )}
              </div>

              <h4 className="mb-3">Completed Reservation History</h4>
              <div className="dashboard-card mb-4">
                {completedHistory.length === 0 ? (
                  <p className="text-muted mb-0" style={{ fontSize: "13px" }}>
                    No completed reservation history recorded yet.
                  </p>
                ) : (
                  <div className="table-responsive">
                    <table className="table app-table align-middle mb-0">
                      <thead>
                        <tr>
                          <th>Prosumer</th>
                          <th>NIC / ID</th>
                          <th>Station</th>
                          <th>Scheduled</th>
                          <th>Completed</th>
                        </tr>
                      </thead>
                      <tbody>
                        {completedHistory.slice(0, 8).map((reservation) => (
                          <tr key={reservation.id || reservation._id || reservation.bookingSlotId}>
                            <td>
                              <strong>{reservation.prosumerName || "Prosumer"}</strong>
                            </td>
                            <td className="small text-muted">
                              {reservation.prosumerId || "—"}
                            </td>
                            <td>
                              <span>{reservation.stationName || "Unknown Station"}</span>
                            </td>
                            <td>{formatDateTime(reservation.scheduledAt)}</td>
                            <td>{formatDateTime(reservation.completedAt || reservation.updatedAt)}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                )}
              </div>

              <button
                className="btn btn-outline-success w-100"
                onClick={loadDashboard}
              >
                Refresh Stats
              </button>
            </>
          )}
        </div>

        <div className="col-12 col-lg-4">
          <div className="dashboard-card h-100">
            <h3 className="mb-3">Account status</h3>
            <div className="status-badge status-active">
              <span className="status-dot" />
              Active Grid Operator
            </div>
          </div>
        </div>
      </div>
    </DashboardLayout>
  );
}