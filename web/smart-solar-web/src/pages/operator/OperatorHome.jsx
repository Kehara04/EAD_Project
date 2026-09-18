import DashboardLayout from "../../components/DashboardLayout";

export default function OperatorHome() {
  return (
    <DashboardLayout
      title="Grid Operator Workspace"
      subtitle="Operational station, booking and QR tools will be connected here."
    >
      <div className="row g-4">
        <div className="col-12 col-lg-8">
          <div className="dashboard-card operator-welcome">
            <span className="eyebrow">Operator access active</span>
            <h3>Welcome to Smart Solar operations</h3>
            <p>
              Your authentication and role-based access are working. Station,
              reservation and QR verification modules can be connected to this
              workspace by the relevant component owners.
            </p>
          </div>
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