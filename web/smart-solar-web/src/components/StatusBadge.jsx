export default function StatusBadge({ status }) {
  const className = {
    Active: "status-active",
    Pending: "status-pending",
    Deactivated: "status-deactivated",
    DeactivationRequested: "status-requested"
  }[status] || "status-neutral";

  const label =
    status === "DeactivationRequested"
      ? "Deactivation requested"
      : status;

  return (
    <span className={`status-badge ${className}`}>
      <span className="status-dot" />
      {label}
    </span>
  );
}
