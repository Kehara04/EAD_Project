import { NavLink, useNavigate } from "react-router-dom";
import { getCurrentUser, logout } from "../services/authService";

const backofficeLinks = [
  { to: "/backoffice", label: "Overview", short: "OV" },
  { to: "/backoffice/users", label: "User Management", short: "UM" },
  { to: "/backoffice/prosumers", label: "Prosumer Management", short: "PM" }
];

const operatorLinks = [
  { to: "/operator", label: "Operator Home", short: "OH" }
];

export default function DashboardLayout({ title, subtitle, children }) {
  const navigate = useNavigate();
  const user = getCurrentUser();
  const links = user.role === "Backoffice" ? backofficeLinks : operatorLinks;

  function handleLogout() {
    logout();
    navigate("/", { replace: true });
  }

  return (
    <div className="app-shell">
      <aside className="sidebar">
        <div className="brand-block">
          <div className="brand-mark">S</div>
          <div>
            <div className="brand-title">Smart Solar</div>
            <div className="brand-subtitle">Microgrid Trading</div>
          </div>
        </div>

        <div className="sidebar-label">Workspace</div>

        <nav className="sidebar-nav">
          {links.map((link) => (
            <NavLink
              key={link.to}
              to={link.to}
              end={link.to === "/backoffice" || link.to === "/operator"}
              className={({ isActive }) =>
                `sidebar-link ${isActive ? "active" : ""}`
              }
            >
              <span className="nav-icon">{link.short}</span>
              <span>{link.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="sidebar-footer">
          <div className="user-card">
            <div className="avatar">
              {(user.name || "U").charAt(0).toUpperCase()}
            </div>
            <div className="user-card-copy">
              <strong>{user.name || "User"}</strong>
              <span>{user.role}</span>
            </div>
          </div>

          <button
            type="button"
            className="btn btn-outline-light w-100 sidebar-logout"
            onClick={handleLogout}
          >
            Sign out
          </button>
        </div>
      </aside>

      <main className="main-content">
        <header className="topbar">
          <div>
            <p className="page-kicker">Smart Solar Administration</p>
            <h1>{title}</h1>
            {subtitle && <p className="page-subtitle">{subtitle}</p>}
          </div>

          <div className="topbar-user">
            <div className="avatar avatar-light">
              {(user.name || "U").charAt(0).toUpperCase()}
            </div>
            <div className="d-none d-md-block">
              <strong>{user.name || "User"}</strong>
              <span>{user.email}</span>
            </div>
          </div>
        </header>

        <section className="content-area">{children}</section>
      </main>
    </div>
  );
}
