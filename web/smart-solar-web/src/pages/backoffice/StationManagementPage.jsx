import {
  useEffect,
  useMemo,
  useState
} from "react";

import DashboardLayout
  from "../../components/DashboardLayout";

import {
  createStation,
  getStations,
  updateStation,
  updateStationStatus
} from "../../services/stationService";

import {
  getApiError
} from "../../services/errorService";


const emptyForm = {
  name: "",
  address: "",
  latitude: "",
  longitude: "",
  capacityKw: "",
  totalSlots: "",
  openingTime: "08:00",
  closingTime: "18:00"
};


export default function StationManagementPage() {
  const [
    stations,
    setStations
  ] = useState([]);

  const [
    form,
    setForm
  ] = useState(emptyForm);

  const [
    editingId,
    setEditingId
  ] = useState(null);

  const [
    search,
    setSearch
  ] = useState("");

  const [
    loading,
    setLoading
  ] = useState(true);

  const [
    saving,
    setSaving
  ] = useState(false);

  const [
    error,
    setError
  ] = useState("");


  useEffect(() => {
    loadStations();
  }, []);


  async function loadStations() {
    try {
      setLoading(true);
      setError("");

      const data =
        await getStations();

      setStations(data);

    } catch (err) {
      setError(
        getApiError(
          err,
          "Unable to load stations."
        )
      );
    } finally {
      setLoading(false);
    }
  }


  function handleChange(event) {
    const {
      name,
      value
    } = event.target;

    setForm((current) => ({
      ...current,
      [name]: value
    }));
  }


  function editStation(station) {
    setEditingId(station.id);

    setForm({
      name:
        station.name ?? "",

      address:
        station.address ?? "",

      latitude:
        station.latitude ?? "",

      longitude:
        station.longitude ?? "",

      capacityKw:
        station.capacityKw ?? "",

      totalSlots:
        station.totalSlots ?? "",

      openingTime:
        station.openingTime ?? "08:00",

      closingTime:
        station.closingTime ?? "18:00"
    });

    window.scrollTo({
      top: 0,
      behavior: "smooth"
    });
  }


  function cancelEdit() {
    setEditingId(null);
    setForm(emptyForm);
  }


  async function handleSubmit(event) {
    event.preventDefault();

    if (
      !form.name.trim() ||
      !form.address.trim()
    ) {
      setError(
        "Station name and address are required."
      );
      return;
    }

    const payload = {
      name:
        form.name.trim(),

      address:
        form.address.trim(),

      latitude:
        Number(form.latitude),

      longitude:
        Number(form.longitude),

      capacityKw:
        Number(form.capacityKw),

      totalSlots:
        Number(form.totalSlots),

      openingTime:
        form.openingTime,

      closingTime:
        form.closingTime
    };

    try {
      setSaving(true);
      setError("");

      if (editingId) {
        await updateStation(
          editingId,
          payload
        );
      } else {
        await createStation(
          payload
        );
      }

      setForm(emptyForm);
      setEditingId(null);

      await loadStations();

    } catch (err) {
      setError(
        getApiError(
          err,
          "Unable to save station."
        )
      );
    } finally {
      setSaving(false);
    }
  }


  async function toggleStatus(station) {
    const nextStatus =
      station.status === "Active"
        ? "Inactive"
        : "Active";

    try {
      setError("");

      await updateStationStatus(
        station.id,
        nextStatus
      );

      await loadStations();

    } catch (err) {
      setError(
        getApiError(
          err,
          "Unable to update station status."
        )
      );
    }
  }


  const filteredStations =
    useMemo(() => {
      const query =
        search
          .trim()
          .toLowerCase();

      if (!query)
        return stations;

      return stations.filter(
        (station) =>
          station.name
            ?.toLowerCase()
            .includes(query) ||
          station.address
            ?.toLowerCase()
            .includes(query) ||
          station.status
            ?.toLowerCase()
            .includes(query)
      );
    }, [
      stations,
      search
    ]);


  return (
    <DashboardLayout
      title="Station Management"
      subtitle="Manage Smart Solar microgrid stations, capacity and operating information."
    >

      {error && (
        <div className="alert alert-danger">
          {error}
        </div>
      )}


      <div className="row g-4">

        <div className="col-lg-4">

          <div className="panel-card p-4">

            <p className="page-kicker">
              MICROGRID STATION
            </p>

            <h3>
              {editingId
                ? "Edit station"
                : "Create station"}
            </h3>

            <form
              onSubmit={handleSubmit}
              className="mt-4"
            >

              <div className="mb-3">
                <label className="form-label">
                  Station name
                </label>

                <input
                  className="form-control app-input"
                  name="name"
                  value={form.name}
                  onChange={handleChange}
                />
              </div>


              <div className="mb-3">
                <label className="form-label">
                  Address
                </label>

                <input
                  className="form-control app-input"
                  name="address"
                  value={form.address}
                  onChange={handleChange}
                />
              </div>


              <div className="row g-2">

                <div className="col-6">
                  <label className="form-label">
                    Latitude
                  </label>

                  <input
                    type="number"
                    step="any"
                    className="form-control app-input"
                    name="latitude"
                    value={form.latitude}
                    onChange={handleChange}
                  />
                </div>

                <div className="col-6">
                  <label className="form-label">
                    Longitude
                  </label>

                  <input
                    type="number"
                    step="any"
                    className="form-control app-input"
                    name="longitude"
                    value={form.longitude}
                    onChange={handleChange}
                  />
                </div>

              </div>


              <div className="row g-2 mt-1">

                <div className="col-6">
                  <label className="form-label">
                    Capacity (kW)
                  </label>

                  <input
                    type="number"
                    className="form-control app-input"
                    name="capacityKw"
                    value={form.capacityKw}
                    onChange={handleChange}
                  />
                </div>

                <div className="col-6">
                  <label className="form-label">
                    Total slots
                  </label>

                  <input
                    type="number"
                    className="form-control app-input"
                    name="totalSlots"
                    value={form.totalSlots}
                    onChange={handleChange}
                  />
                </div>

              </div>


              <div className="row g-2 mt-1">

                <div className="col-6">
                  <label className="form-label">
                    Opens
                  </label>

                  <input
                    type="time"
                    className="form-control app-input"
                    name="openingTime"
                    value={form.openingTime}
                    onChange={handleChange}
                  />
                </div>

                <div className="col-6">
                  <label className="form-label">
                    Closes
                  </label>

                  <input
                    type="time"
                    className="form-control app-input"
                    name="closingTime"
                    value={form.closingTime}
                    onChange={handleChange}
                  />
                </div>

              </div>


              <button
                type="submit"
                className="btn btn-solar w-100 mt-4"
                disabled={saving}
              >
                {saving
                  ? "Saving..."
                  : editingId
                  ? "Save changes"
                  : "Create station"}
              </button>


              {editingId && (
                <button
                  type="button"
                  className="btn btn-outline-secondary w-100 mt-2"
                  onClick={cancelEdit}
                >
                  Cancel edit
                </button>
              )}

            </form>

          </div>

        </div>


        <div className="col-lg-8">

          <div className="panel-card p-4">

            <div className="d-flex justify-content-between gap-3 align-items-center mb-4">

              <div>
                <p className="page-kicker mb-1">
                  STATIONS
                </p>

                <h3 className="mb-0">
                  Microgrid network
                </h3>
              </div>

              <input
                className="form-control app-input"
                style={{
                  maxWidth: "260px"
                }}
                placeholder="Search stations..."
                value={search}
                onChange={(event) =>
                  setSearch(
                    event.target.value
                  )
                }
              />

            </div>


            {loading ? (

              <div className="py-5 text-center">
                Loading stations...
              </div>

            ) : filteredStations.length === 0 ? (

              <div className="py-5 text-center text-muted">
                No stations found.
              </div>

            ) : (

              <div className="table-responsive">

                <table className="table align-middle">

                  <thead>
                    <tr>
                      <th>Station</th>
                      <th>Capacity</th>
                      <th>Slots</th>
                      <th>Status</th>
                      <th />
                    </tr>
                  </thead>

                  <tbody>

                    {filteredStations.map(
                      (station) => (

                        <tr key={station.id}>

                          <td>
                            <strong>
                              {station.name}
                            </strong>

                            <div className="small text-muted">
                              {station.address}
                            </div>
                          </td>

                          <td>
                            {station.capacityKw} kW
                          </td>

                          <td>
                            {station.availableSlots}
                            {" / "}
                            {station.totalSlots}
                          </td>

                          <td>
                            <span
                              className={
                                station.status === "Active"
                                  ? "badge text-bg-success"
                                  : "badge text-bg-secondary"
                              }
                            >
                              {station.status}
                            </span>
                          </td>

                          <td className="text-end">

                            <button
                              className="btn btn-sm btn-outline-success me-2"
                              onClick={() =>
                                editStation(station)
                              }
                            >
                              Edit
                            </button>

                            <button
                              className="btn btn-sm btn-outline-secondary"
                              onClick={() =>
                                toggleStatus(station)
                              }
                            >
                              {station.status === "Active"
                                ? "Deactivate"
                                : "Activate"}
                            </button>

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

      </div>

    </DashboardLayout>
  );
}