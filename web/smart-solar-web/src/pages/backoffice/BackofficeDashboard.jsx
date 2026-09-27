import {
  useEffect,
  useMemo,
  useState
} from "react";

import {
  Link
} from "react-router-dom";

import DashboardLayout
  from "../../components/DashboardLayout";

import StatCard
  from "../../components/StatCard";

import StatusBadge
  from "../../components/StatusBadge";

import {
  getStations
} from "../../services/stationService";

import {
  getUsers
} from "../../services/userService";

import {
  getProsumers
} from "../../services/prosumerService";

import {
  getReservations
} from "../../services/reservationService";

import {
  getApiError
} from "../../services/errorService";

// Displays the Backoffice overview and management statistics.
export default function BackofficeDashboard() {

  // Stores station records retrieved from the API.
  const [
    stations,
    setStations
  ] = useState([]);

  // Stores registered system users.
  const [
    users,
    setUsers
  ] = useState([]);

  // Stores registered Prosumers.
  const [
    prosumers,
    setProsumers
  ] = useState([]);

  // Stores energy reservation records.
  const [
    reservations,
    setReservations
  ] = useState([]);

  // Tracks whether dashboard data is being loaded.
  const [
    loading,
    setLoading
  ] = useState(true);

  // Stores any error encountered while loading data.
  const [
    error,
    setError
  ] = useState("");

  // Loads dashboard data when the component first mounts.
  useEffect(() => {

    // Retrieves the data required for dashboard statistics.
    async function loadDashboard() {

      try {

        setLoading(true);

        setError("");


        // Requests all dashboard datasets concurrently.
        const [
          usersData,
          prosumersData,
          stationsData,
          reservationsData
        ] = await Promise.all([

          getUsers(),

          getProsumers(),

          getStations(),

          getReservations()

        ]);

        // Stores valid API arrays and falls back to empty arrays.
        setUsers(
          Array.isArray(usersData)
            ? usersData
            : []
        );


        setProsumers(
          Array.isArray(prosumersData)
            ? prosumersData
            : []
        );


        setStations(
          Array.isArray(stationsData)
            ? stationsData
            : []
        );


        setReservations(
          Array.isArray(reservationsData)
            ? reservationsData
            : []
        );


      } catch (err) {

        // Displays a readable message when loading fails.
        setError(
          getApiError(
            err,
            "Unable to load dashboard data."
          )
        );

      } finally {

        setLoading(false);
      }
    }


    loadDashboard();

  }, []);


  // Calculates dashboard statistics when the source data changes.
  const metrics =
    useMemo(() => {

      // Excludes Prosumers from the web-user account count.
      const webUsers =
        users.filter(
          (user) =>
            user.role !== "Prosumer"
        );


      return {

        /* USERS */

        webUsers:
          webUsers.length,

        activeWebUsers:
          webUsers.filter(
            (user) =>
              user.status === "Active"
          ).length,


        /* PROSUMERS */

        prosumers:
          prosumers.length,

        pending:
          prosumers.filter(
            (prosumer) =>
              prosumer.status === "Pending"
          ).length,

        deactivation:
          prosumers.filter(
            (prosumer) =>
              prosumer.status ===
              "DeactivationRequested"
          ).length,


        /* STATIONS */

        totalStations:
          stations.length,

        activeStations:
          stations.filter(
            (station) =>
              station.status === "Active"
          ).length,

        inactiveStations:
          stations.filter(
            (station) =>
              station.status === "Inactive"
          ).length,

        availableStations:
          stations.filter(
            (station) =>
              station.availableSlots > 0
          ).length,


        /* RESERVATIONS */

        reservations:
          reservations.length,

        pendingReservations:
          reservations.filter(
            (reservation) =>
              reservation.status ===
              "Pending"
          ).length,

        approvedReservations:
          reservations.filter(
            (reservation) =>
              reservation.status ===
              "Approved"
          ).length,

        cancelledReservations:
          reservations.filter(
            (reservation) =>
              reservation.status ===
              "Cancelled"
          ).length,

        completedReservations:
          reservations.filter(
            (reservation) =>
              reservation.status ===
              "Completed"
          ).length

      };

    }, [
      users,
      prosumers,
      stations,
      reservations
    ]);

  // Selects the five most recently registered Prosumers.
  const recentProsumers =
    useMemo(() => {

      return [
        ...prosumers
      ]
        .sort(
          (a, b) =>
            new Date(
              b.createdAt
            )
            -
            new Date(
              a.createdAt
            )
        )
        .slice(
          0,
          5
        );

    }, [prosumers]);

  // Selects the five most recently created reservations.
  const recentReservations =
    useMemo(() => {

      return [
        ...reservations
      ]
        .sort(
          (a, b) =>
            new Date(
              b.createdAt
            )
            -
            new Date(
              a.createdAt
            )
        )
        .slice(
          0,
          5
        );

    }, [reservations]);


  return (

    <DashboardLayout

      title="Backoffice Overview"

      subtitle="Monitor account activity, station operations and Smart Solar reservations from one place."

    >

      {error && (

        <div
          className="alert alert-danger app-alert"
          role="alert"
        >
          {error}
        </div>

      )}

      <div className="row g-4 mb-4">

        <div className="col-12 col-md-6 col-xl-3">

          <StatCard

            label="Web users"

            value={
              loading
                ? "—"
                : metrics.webUsers
            }

            helper={
              `${metrics.activeWebUsers} active accounts`
            }

            icon="WU"

          />

        </div>


        <div className="col-12 col-md-6 col-xl-3">

          <StatCard

            label="Total prosumers"

            value={
              loading
                ? "—"
                : metrics.prosumers
            }

            helper="Registered solar prosumers"

            icon="PR"

          />

        </div>


        <div className="col-12 col-md-6 col-xl-3">

          <StatCard

            label="Pending activation"

            value={
              loading
                ? "—"
                : metrics.pending
            }

            helper="Require Backoffice review"

            icon="PA"

          />

        </div>


        <div className="col-12 col-md-6 col-xl-3">

          <StatCard

            label="Deactivation requests"

            value={
              loading
                ? "—"
                : metrics.deactivation
            }

            helper="Awaiting action"

            icon="DR"

          />

        </div>

      </div>

      <div className="card-heading-row flex-wrap gap-3">

        <div>

          <h3>
            Energy reservations
          </h3>

          <p>
            Booking activity across the Smart Solar network.
          </p>

        </div>


        <Link
          className="btn btn-soft"
          to="/backoffice/reservations"
        >
          Manage reservations
        </Link>

      </div>


      <div className="row g-4 mb-4">

        <div className="col-12 col-md-6 col-xl-3">

          <StatCard

            label="Total reservations"

            value={
              loading
                ? "—"
                : metrics.reservations
            }

            helper="All energy bookings"

            icon="RS"

          />

        </div>


        <div className="col-12 col-md-6 col-xl-3">

          <StatCard

            label="Pending reservations"

            value={
              loading
                ? "—"
                : metrics.pendingReservations
            }

            helper="Require approval"

            icon="PN"

          />

        </div>


        <div className="col-12 col-md-6 col-xl-3">

          <StatCard

            label="Approved reservations"

            value={
              loading
                ? "—"
                : metrics.approvedReservations
            }

            helper="Approved energy bookings"

            icon="AP"

          />

        </div>


        <div className="col-12 col-md-6 col-xl-3">

          <StatCard

            label="Completed reservations"

            value={
              loading
                ? "—"
                : metrics.completedReservations
            }

            helper="Completed energy transfers"

            icon="CP"

          />

        </div>

      </div>

      <div className="card-heading-row flex-wrap gap-3">

        <div>

          <h3>
            Station network
          </h3>

          <p>
            Capacity and operating status across your microgrid stations.
          </p>

        </div>


        <Link
          className="btn btn-soft"
          to="/backoffice/stations"
        >
          Manage stations
        </Link>

      </div>


      <div className="row g-4 mb-4">

        {[
          [
            "Total stations",
            metrics.totalStations,
            "All registered stations",
            "ST"
          ],

          [
            "Active stations",
            metrics.activeStations,
            "Currently active in the network",
            "AS"
          ],

          [
            "Inactive stations",
            metrics.inactiveStations,
            "Currently inactive",
            "IS"
          ],

          [
            "Stations with available slots",
            metrics.availableStations,
            "Stations with free energy slots",
            "AV"
          ]

        ].map(
          (
            [
              label,
              value,
              helper,
              icon
            ]
          ) => (

            <div
              className="col-12 col-md-6 col-xl-3"
              key={label}
            >

              <StatCard

                label={label}

                value={
                  loading || error
                    ? "—"
                    : value
                }

                helper={helper}

                icon={icon}

              />

            </div>

          )
        )}

      </div>

      <div className="row g-4">

        <div className="col-12 col-xl-8">

          <div className="dashboard-card">

            <div className="card-heading-row">

              <div>

                <h3>
                  Recent reservations
                </h3>

                <p>
                  Latest energy bookings submitted by prosumers.
                </p>

              </div>


              <Link
                className="btn btn-soft"
                to="/backoffice/reservations"
              >
                View all
              </Link>

            </div>


            {loading ? (

              <div className="loading-state">

                <div
                  className="spinner-border text-success"
                />

                <span>
                  Loading reservations...
                </span>

              </div>

            ) : recentReservations.length === 0 ? (

              <div className="empty-state">
                No reservations yet.
              </div>

            ) : (

              <div className="table-responsive">

                <table className="table app-table align-middle mb-0">

                  <thead>

                    <tr>

                      <th>
                        Prosumer
                      </th>

                      <th>
                        Station
                      </th>

                      <th>
                        Schedule
                      </th>

                      <th>
                        Status
                      </th>

                    </tr>

                  </thead>


                  <tbody>

                    {recentReservations.map(
                      (reservation) => (

                        <tr
                          key={
                            reservation.id
                          }
                        >

                          <td>

                            <strong>

                              {
                                reservation.prosumerName
                                ||
                                "Prosumer"
                              }

                            </strong>


                            <span className="table-subtext">

                              {
                                reservation.prosumerId
                                ||
                                "—"
                              }

                            </span>

                          </td>


                          <td>

                            <strong>

                              {
                                reservation.stationName
                                ||
                                "Unknown station"
                              }

                            </strong>


                            <span className="table-subtext">

                              Slot{" "}

                              {
                                reservation.slotNumber
                                ??
                                "—"
                              }

                            </span>

                          </td>


                          <td>

                            {
                              formatDateTime(
                                reservation.scheduledAt
                              )
                            }

                          </td>


                          <td>

                            <StatusBadge
                              status={
                                reservation.status
                              }
                            />

                          </td>

                        </tr>

                      )
                    )}

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

                <h3>
                  Quick actions
                </h3>

                <p>
                  Common Smart Solar administration tasks.
                </p>

              </div>

            </div>


            <div className="quick-actions">

              {/* CREATE USER */}

              <Link
                to="/backoffice/users"
                className="quick-action-item"
              >

                <span className="quick-action-icon">
                  +
                </span>


                <div>

                  <strong>
                    Create web user
                  </strong>

                  <span>
                    Add Backoffice or Grid Operator access
                  </span>

                </div>

              </Link>


              {/* PENDING PROSUMERS */}

              <Link
                to="/backoffice/prosumers"
                className="quick-action-item"
              >

                <span className="quick-action-icon">
                  ✓
                </span>


                <div>

                  <strong>
                    Review pending accounts
                  </strong>

                  <span>
                    Activate newly registered prosumers
                  </span>

                </div>

              </Link>


              {/* RESERVATIONS */}

              <Link
                to="/backoffice/reservations"
                className="quick-action-item"
              >

                <span className="quick-action-icon">
                  R
                </span>


                <div>

                  <strong>
                    Review reservations
                  </strong>

                  <span>

                    {metrics.pendingReservations > 0

                      ? `${metrics.pendingReservations} booking(s) awaiting approval`

                      : "No pending reservations"}

                  </span>

                </div>

              </Link>


              {/* DEACTIVATION */}

              <Link
                to="/backoffice/prosumers"
                className="quick-action-item"
              >

                <span className="quick-action-icon">
                  !
                </span>


                <div>

                  <strong>
                    Deactivation requests
                  </strong>

                  <span>
                    Review and approve account requests
                  </span>

                </div>

              </Link>

            </div>

          </div>

        </div>

      </div>

      <div className="dashboard-card mt-4">

        <div className="card-heading-row">

          <div>

            <h3>
              Recent prosumer registrations
            </h3>

            <p>
              Newest accounts received through the mobile registration flow.
            </p>

          </div>


          <Link
            className="btn btn-soft"
            to="/backoffice/prosumers"
          >
            View all
          </Link>

        </div>


        {loading ? (

          <div className="loading-state">

            <div
              className="spinner-border text-success"
            />

            <span>
              Loading registrations...
            </span>

          </div>

        ) : recentProsumers.length === 0 ? (

          <div className="empty-state">
            No prosumer registrations yet.
          </div>

        ) : (

          <div className="table-responsive">

            <table className="table app-table align-middle mb-0">

              <thead>

                <tr>

                  <th>
                    Prosumer
                  </th>

                  <th>
                    NIC
                  </th>

                  <th>
                    Status
                  </th>

                  <th>
                    Registered
                  </th>

                </tr>

              </thead>


              <tbody>

                {recentProsumers.map(
                  (prosumer) => (

                    <tr
                      key={
                        prosumer.nic
                      }
                    >

                      <td>

                        <strong>
                          {prosumer.name}
                        </strong>


                        <span className="table-subtext">
                          {prosumer.email}
                        </span>

                      </td>


                      <td>
                        {prosumer.nic}
                      </td>


                      <td>

                        <StatusBadge
                          status={
                            prosumer.status
                          }
                        />

                      </td>


                      <td>

                        {
                          formatDate(
                            prosumer.createdAt
                          )
                        }

                      </td>

                    </tr>

                  )
                )}

              </tbody>

            </table>

          </div>

        )}

      </div>

    </DashboardLayout>
  );
}

// Formats a date for display in the dashboard tables.
function formatDate(value) {

  if (!value) {
    return "—";
  }


  // Converts the supplied value into a JavaScript Date.
  const date =
    new Date(value);


  if (
    Number.isNaN(
      date.getTime()
    )
  ) {

    return "—";
  }


  return new Intl.DateTimeFormat(
    "en",
    {
      year: "numeric",
      month: "short",
      day: "2-digit"
    }
  ).format(date);
}

// Formats a date and time for reservation schedule display.
function formatDateTime(value) {

  if (!value) {
    return "—";
  }


  const date =
    new Date(value);


  if (
    Number.isNaN(
      date.getTime()
    )
  ) {

    return "—";
  }


  return new Intl.DateTimeFormat(
    "en",
    {
      year: "numeric",
      month: "short",
      day: "2-digit",
      hour: "2-digit",
      minute: "2-digit"
    }
  ).format(date);
}
