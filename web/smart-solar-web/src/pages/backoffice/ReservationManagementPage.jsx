import {
  useEffect,
  useMemo,
  useState
} from "react";

import DashboardLayout
  from "../../components/DashboardLayout";

import StatusBadge
  from "../../components/StatusBadge";

import {
  approveReservation,
  getReservations
} from "../../services/reservationService";

import {
  getApiError
} from "../../services/errorService";


export default function ReservationManagementPage() {

  const [
    reservations,
    setReservations
  ] = useState([]);

  const [
    search,
    setSearch
  ] = useState("");

  const [
    statusFilter,
    setStatusFilter
  ] = useState("All");

  const [
    loading,
    setLoading
  ] = useState(true);

  const [
    actionId,
    setActionId
  ] = useState("");

  const [
    error,
    setError
  ] = useState("");

  const [
    success,
    setSuccess
  ] = useState("");


  useEffect(() => {
    loadReservations();
  }, []);


  async function loadReservations() {

    try {

      setLoading(true);

      setError("");

      const data =
        await getReservations();

      setReservations(
        Array.isArray(data)
          ? data
          : []
      );

    } catch (err) {

      setError(
        getApiError(
          err,
          "Unable to load reservations."
        )
      );

    } finally {

      setLoading(false);
    }
  }


  const counts =
    useMemo(() => {

      return {

        all:
          reservations.length,

        pending:
          reservations.filter(
            (reservation) =>
              reservation.status ===
              "Pending"
          ).length,

        approved:
          reservations.filter(
            (reservation) =>
              reservation.status ===
              "Approved"
          ).length,

        cancelled:
          reservations.filter(
            (reservation) =>
              reservation.status ===
              "Cancelled"
          ).length,

        completed:
          reservations.filter(
            (reservation) =>
              reservation.status ===
              "Completed"
          ).length

      };

    }, [reservations]);


  const filteredReservations =
    useMemo(() => {

      const term =
        search
          .trim()
          .toLowerCase();


      return reservations.filter(
        (reservation) => {

          const matchesSearch =
            !term ||

            reservation.prosumerName
              ?.toLowerCase()
              .includes(term) ||

            reservation.prosumerId
              ?.toLowerCase()
              .includes(term) ||

            reservation.stationName
              ?.toLowerCase()
              .includes(term) ||

            reservation.stationAddress
              ?.toLowerCase()
              .includes(term) ||

            String(
              reservation.slotNumber ?? ""
            ).includes(term);


          const matchesStatus =
            statusFilter === "All" ||

            reservation.status ===
              statusFilter;


          return (
            matchesSearch &&
            matchesStatus
          );
        }
      );

    }, [
      reservations,
      search,
      statusFilter
    ]);


  // Approve a pending reservation after a confirmation prompt.
  async function handleApprove(
    reservation
  ) {

    if (
      reservation.status !==
      "Pending"
    ) {

      return;
    }


    const confirmed =
      window.confirm(
        `Approve the reservation for ${reservation.prosumerName || reservation.prosumerId} at ${reservation.stationName}?`
      );


    if (!confirmed) {
      return;
    }


    try {

      setActionId(
        reservation.id
      );

      setError("");

      setSuccess("");


      await approveReservation(
        reservation.id
      );


      setSuccess(
        "Reservation approved successfully."
      );


      await loadReservations();

    } catch (err) {

      setError(
        getApiError(
          err,
          "Unable to approve the reservation."
        )
      );

    } finally {

      setActionId("");
    }
  }


  return (

    <DashboardLayout

      title="Reservation Management"

      subtitle="Review Smart Solar energy reservations and approve pending bookings."

    >

      {error && (

        <div
          className="alert alert-danger app-alert"
          role="alert"
        >
          {error}
        </div>

      )}


      {success && (

        <div
          className="alert alert-success app-alert"
          role="alert"
        >
          {success}
        </div>

      )}


      {/* =====================================
          SUMMARY FILTERS
      ====================================== */}

      <div className="prosumer-summary-grid mb-4">

        <SummaryButton

          label="All"

          value={counts.all}

          active={
            statusFilter === "All"
          }

          onClick={() =>
            setStatusFilter("All")
          }

        />


        <SummaryButton

          label="Pending"

          value={counts.pending}

          active={
            statusFilter === "Pending"
          }

          onClick={() =>
            setStatusFilter(
              "Pending"
            )
          }

        />


        <SummaryButton

          label="Approved"

          value={counts.approved}

          active={
            statusFilter ===
            "Approved"
          }

          onClick={() =>
            setStatusFilter(
              "Approved"
            )
          }

        />


        <SummaryButton

          label="Cancelled"

          value={counts.cancelled}

          active={
            statusFilter ===
            "Cancelled"
          }

          onClick={() =>
            setStatusFilter(
              "Cancelled"
            )
          }

        />


        <SummaryButton

          label="Completed"

          value={counts.completed}

          active={
            statusFilter ===
            "Completed"
          }

          onClick={() =>
            setStatusFilter(
              "Completed"
            )
          }

        />

      </div>


      {/* =====================================
          RESERVATION TABLE
      ====================================== */}

      <div className="dashboard-card">

        <div
          className="card-heading-row flex-wrap gap-3"
        >

          <div>

            <h3>
              Energy reservations
            </h3>

            <p>
              {filteredReservations.length} reservation(s) shown
            </p>

          </div>


          <input

            type="search"

            className="form-control app-input search-input"

            placeholder="Search prosumer, NIC or station..."

            value={search}

            onChange={(event) =>
              setSearch(
                event.target.value
              )
            }

          />

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

        ) : filteredReservations.length ===
          0 ? (

          <div className="empty-state">

            No reservations match
            this filter.

          </div>

        ) : (

          <div className="table-responsive">

            <table
              className="table app-table align-middle mb-0"
            >

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
                    Slot
                  </th>

                  <th>
                    Status
                  </th>

                  <th>
                    Created
                  </th>

                  <th className="text-end">
                    Action
                  </th>

                </tr>

              </thead>


              <tbody>

                {filteredReservations.map(
                  (reservation) => (

                    <tr
                      key={
                        reservation.id
                      }
                    >

                      {/* PROSUMER */}

                      <td>

                        <strong>
                          {
                            reservation.prosumerName ||
                            "Prosumer"
                          }
                        </strong>

                        <span className="table-subtext">

                          NIC:{" "}

                          {
                            reservation.prosumerId ||
                            "—"
                          }

                        </span>

                      </td>


                      {/* STATION */}

                      <td>

                        <strong>

                          {
                            reservation.stationName ||
                            "Unknown station"
                          }

                        </strong>


                        <span className="table-subtext">

                          {
                            reservation.stationAddress ||
                            "No address"
                          }

                        </span>

                      </td>


                      {/* SCHEDULE */}

                      <td className="text-nowrap">

                        {
                          formatDateTime(
                            reservation.scheduledAt
                          )
                        }

                      </td>


                      {/* SLOT */}

                      <td>

                        <span className="fw-semibold">

                          Slot{" "}

                          {
                            reservation.slotNumber
                          }

                        </span>

                      </td>


                      {/* STATUS */}

                      <td>

                        <StatusBadge
                          status={
                            reservation.status
                          }
                        />

                      </td>


                      {/* CREATED */}

                      <td className="text-nowrap">

                        {
                          formatDate(
                            reservation.createdAt
                          )
                        }

                      </td>


                      {/* ACTION */}

                      <td className="text-end">

                        <ReservationAction

                          reservation={
                            reservation
                          }

                          busy={
                            actionId ===
                            reservation.id
                          }

                          onApprove={
                            handleApprove
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

    </DashboardLayout>
  );
}


function SummaryButton({
  label,
  value,
  active,
  onClick
}) {

  return (

    <button

      type="button"

      className={
        `summary-filter ${
          active
            ? "active"
            : ""
        }`
      }

      onClick={onClick}

    >

      <span>
        {label}
      </span>

      <strong>
        {value}
      </strong>

    </button>
  );
}


function ReservationAction({
  reservation,
  busy,
  onApprove
}) {

  if (busy) {

    return (

      <button
        type="button"
        className="btn btn-secondary btn-sm action-button"
        disabled
      >

        Approving...

      </button>

    );
  }


  if (
    reservation.status ===
    "Pending"
  ) {

    return (

      <button

        type="button"

        className="btn btn-success btn-sm action-button"

        onClick={() =>
          onApprove(
            reservation
          )
        }

      >

        Approve

      </button>

    );
  }


  if (
    reservation.status ===
    "Approved"
  ) {

    return (

      <span className="text-success fw-semibold">

        Approved

      </span>

    );
  }


  if (
    reservation.status ===
    "Cancelled"
  ) {

    return (

      <span className="text-danger">

        Cancelled

      </span>

    );
  }


  if (
    reservation.status ===
    "Completed"
  ) {

    return (

      <span className="text-muted">

        Completed

      </span>

    );
  }


  return (

    <span className="text-muted">
      No action
    </span>

  );
}


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


function formatDate(value) {

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

      day: "2-digit"

    }

  ).format(date);
}