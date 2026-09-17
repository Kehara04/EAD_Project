import { Link } from "react-router-dom";

export default function BackofficeDashboard() {
  return (
    <div className="container mt-4">
      <h2>Backoffice Dashboard</h2>

      <div className="mt-4">
        <Link
          className="btn btn-primary me-3"
          to="/backoffice/users"
        >
          Manage Users
        </Link>

        <Link
          className="btn btn-success"
          to="/backoffice/prosumers"
        >
          Manage Prosumers
        </Link>
      </div>
    </div>
  );
}