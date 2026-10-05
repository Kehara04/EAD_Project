// Display a reusable dashboard statistics card with a label, value, optional helper text, and icon.
export default function StatCard({ label, value, helper, icon }) {
  return (
    <div className="dashboard-card stat-card h-100">
      <div className="d-flex align-items-start justify-content-between gap-3">
        <div>
          <div className="stat-label">{label}</div>
          <div className="stat-value">{value}</div>
          {helper && <div className="stat-helper">{helper}</div>}
        </div>
        <div className="stat-icon" aria-hidden="true">
          {icon}
        </div>
      </div>
    </div>
  );
}
