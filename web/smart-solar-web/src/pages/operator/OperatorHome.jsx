import { useState, useEffect } from "react";
import DashboardLayout from "../../components/DashboardLayout";
import { getOperatorDashboardStats } from "../../services/operatorService";

export default function OperatorHome() {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadDashboard = async () => {
    setLoading(true);
    setError("");
    try {
      const data = await getOperatorDashboardStats();
      setStats(data);
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
                  stats.stationSummaries.map(s => (
                    <div key={s.stationId} className="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom">
                      <span className="fw-bold">{s.stationName}</span>
                      <span className="text-success">{s.completedToday} completed today</span>
                    </div>
                  ))
                ) : (
                  <p className="text-muted mb-0" style={{ fontSize: '13px' }}>No completed transfers at any station today.</p>
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