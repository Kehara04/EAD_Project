

import {
  NavLink,
  useNavigate
} from "react-router-dom";

import {
  getCurrentUser,
  logout
} from "../services/authService";


const backofficeLinks = [
  {
    to: "/backoffice",
    label: "Overview",
    short: "⌂"
  },
  {
    to: "/backoffice/users",
    label: "User Management",
    short: "U"
  },
  {
    to: "/backoffice/prosumers",
    label: "Prosumer Management",
    short: "P"
  },
  {
    // Station management navigation links to the protected Backoffice route.
    to: "/backoffice/stations",
    label: "Station Management",
    short: "S"
  },
  {
    to: "/backoffice/reservations",
    label: "Reservations",
    short: "R"
  }
];


const operatorLinks = [
  {
    to: "/operator",
    label: "Operator Home",
    short: "O"
  }
];


export default function DashboardLayout({
  title,
  subtitle,
  children
}) {

  const navigate =
    useNavigate();


  const user =
    getCurrentUser();


  const links =
    user.role === "Backoffice"
      ? backofficeLinks
      : operatorLinks;


  function handleLogout() {

    logout();

    navigate(
      "/",
      {
        replace: true
      }
    );
  }


  return (

    <div className="app-shell">

      {/* =========================================
          SIDEBAR
      ========================================== */}

      <aside className="sidebar">

        <div className="sidebar-main">

          {/* =====================================
              OFFICIAL SMART SOLAR LOGO
          ====================================== */}

          <div className="brand-block brand-block-logo">

            <img
              src="/images/smart-solar-logo.png"
              alt="Smart Solar - Powering a Smarter Tomorrow"
              className="sidebar-brand-logo"
            />

          </div>


          {/* =====================================
              NAVIGATION
          ====================================== */}

          <div className="sidebar-label">
            Workspace
          </div>


          <nav className="sidebar-nav">

            {links.map((link) => (

              <NavLink

                key={link.to}

                to={link.to}

                end={
                  link.to === "/backoffice"
                  ||
                  link.to === "/operator"
                }

                className={({ isActive }) =>
                  `sidebar-link ${
                    isActive
                      ? "active"
                      : ""
                  }`
                }

              >

                <span className="nav-icon">
                  {link.short}
                </span>


                <span className="nav-label">
                  {link.label}
                </span>

              </NavLink>

            ))}

          </nav>

        </div>


        {/* =========================================
            SIDEBAR FOOTER
        ========================================== */}

        <div className="sidebar-footer">

          {/* USER CARD */}

          <div className="sidebar-user-card">

            <div className="avatar sidebar-avatar">

              {(user.name || "U")
                .charAt(0)
                .toUpperCase()}

            </div>


            <div className="user-card-copy">

              <strong>
                {user.name || "User"}
              </strong>

              <span>
                {user.role}
              </span>

            </div>

          </div>


          {/* LOGOUT */}

          <button

            type="button"

            className="sidebar-logout"

            onClick={handleLogout}

          >

            <span className="logout-icon">
              ↪
            </span>

            Sign out

          </button>


          {/* ENERGY IMAGE */}

          <div className="sidebar-energy-art">

            <div className="sidebar-energy-overlay" />


            <div className="sidebar-energy-copy">

              <span className="energy-line" />


              <strong>
                CLEAN ENERGY
              </strong>


              <span>
                STRONGER COMMUNITIES
              </span>


              <span>
                A BRIGHTER TOMORROW
              </span>

            </div>

          </div>

        </div>

      </aside>


      {/* =========================================
          MAIN CONTENT
      ========================================== */}

      <main className="main-content">

        {/* =====================================
            TOPBAR
        ====================================== */}

        <header className="topbar">

          <div className="topbar-heading">

            <p className="page-kicker">
              Smart Solar Administration
            </p>


            <h1>
              {title}
            </h1>


            {subtitle && (

              <p className="page-subtitle">
                {subtitle}
              </p>

            )}

          </div>


          <div className="topbar-right">

            {/* TOP TAGLINE */}

            <div className="topbar-tagline">

              CLEAN ENERGY

              <span>
                •
              </span>

              CONNECTED

              <span>
                •
              </span>

              SUSTAINABLE

            </div>


            {/* LOGGED IN USER */}

            <div className="topbar-user">

              <div className="avatar avatar-light">

                {(user.name || "U")
                  .charAt(0)
                  .toUpperCase()}

              </div>


              <div className="d-none d-md-block">

                <strong>
                  {user.name || "User"}
                </strong>

                <span>
                  {user.email}
                </span>

              </div>

            </div>

          </div>

        </header>


        {/* =====================================
            PAGE CONTENT
        ====================================== */}

        <section className="content-area">
          {children}
        </section>

      </main>

    </div>
  );
}