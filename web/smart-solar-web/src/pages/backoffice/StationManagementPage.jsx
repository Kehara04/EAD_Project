import StationAddressField from "../../components/StationAddressField";
import StatusBadge from "../../components/StatusBadge";
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
  capacityKw: "",
  totalSlots: "",
  openingTime: "08:00",
  closingTime: "18:00"
};

// Displays and manages microgrid station details, locations, and statuses.
export default function StationManagementPage() {
  const [selectedLocation, setSelectedLocation] = useState(null);
  const [statusFilter, setStatusFilter] = useState("All");
  const [availabilityFilter, setAvailabilityFilter] = useState("All");
  const [actionId, setActionId] = useState(null);

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


  // Load the management list from the API; search and dropdown filters are applied locally.
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


  // Updates the corresponding form field when its input value changes.
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


  // Keep the saved coordinates until the admin selects a replacement address.
  function editStation(station) {
    setEditingId(station.id);
    setSelectedLocation({ address: station.address, latitude: station.latitude,
      longitude: station.longitude, locationToken: null });

    setForm({
      name:
        station.name ?? "",

      address:
        station.address ?? "",

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


  // Clear both the form and selected location before returning to create mode.
  function cancelEdit() {
    setSelectedLocation(null);
    setEditingId(null);
    setForm(emptyForm);
  }


  // Create and edit share one payload; coordinates come from the protected address selection.
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

  // Changing the address invalidates the previous location, so require another selection.
    if (!selectedLocation || selectedLocation.address !== form.address.trim()) {
      setError("Choose an address suggestion to set the station's map location.");
      return;
    }

    const payload = {
      name:
        form.name.trim(),

      address:
        form.address.trim(),

      locationToken: selectedLocation.locationToken,

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
      setSelectedLocation(null);
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


  // The API enforces reservation rules; surface its rejection message without changing local status.
  async function toggleStatus(station) {
    if (actionId) return;
    const nextStatus =
      station.status === "Active"
        ? "Inactive"
        : "Active";

    try {
      setActionId(station.id);
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
    } finally {
      setActionId(null);
    }
  }


  // Combine name/address search, station status and free-slot availability.
  const filteredStations = useMemo(() => {
    const query = search.trim().toLowerCase();
    return stations.filter((station) => {
      const matchesSearch = !query || station.name?.toLowerCase().includes(query)
        || station.address?.toLowerCase().includes(query);
      const matchesStatus = statusFilter === "All" || station.status === statusFilter;
      const matchesAvailability = availabilityFilter === "All"
        || (availabilityFilter === "Available" ? station.availableSlots > 0 : station.availableSlots === 0);
      return matchesSearch && matchesStatus && matchesAvailability;
    });
  }, [stations, search, statusFilter, availabilityFilter]);

  return (
    <DashboardLayout
      title="Station Management"
      subtitle="Manage Smart Solar microgrid stations, capacity and operating information."
    >
      {error && <div className="alert alert-danger app-alert" role="alert">{error}</div>}

      <div className="row g-4">
        <div className="col-12 col-xl-4">
          <div className="dashboard-card form-card">
            <div className="section-heading">
              <span className="eyebrow">{editingId ? "Station settings" : "New station"}</span>
              <h3>{editingId ? "Edit station" : "Create station"}</h3>
              <p>Set the location, capacity and operating hours for your microgrid station.</p>
            </div>

            <form onSubmit={handleSubmit}>
              <fieldset disabled={saving}>
                <div className="mb-3">
                  <label className="form-label" htmlFor="station-name">Station name</label>
                  <input id="station-name" className="form-control app-input" name="name"
                    value={form.name} onChange={handleChange} placeholder="e.g. Negombo Solar Hub"
                    required minLength={2} maxLength={100} />
                </div>
                <StationAddressField value={form.address} location={selectedLocation}
                  onChange={(address) => {
                    setForm((current) => ({ ...current, address }));
                    setSelectedLocation(null);
                    setError("");
                  }}
                  onSelect={(location) => {
                    setForm((current) => ({ ...current, address: location.address }));
                    setSelectedLocation(location);
                    setError("");
                  }} />
                <div className="row g-3 mb-3">
                  <div className="col-6">
                    <label className="form-label" htmlFor="station-capacity">Capacity (kW)</label>
                    <input id="station-capacity" type="number" min="0.1" step="any" required
                      className="form-control app-input" name="capacityKw" placeholder="e.g. 300"
                      value={form.capacityKw} onChange={handleChange} />
                  </div>
                  <div className="col-6">
                    <label className="form-label" htmlFor="station-slots">Total slots</label>
                    <input id="station-slots" type="number" min={1} max={1000} step={1} required
                      className="form-control app-input" name="totalSlots" placeholder="e.g. 10"
                      value={form.totalSlots} onChange={handleChange} />
                  </div>
                  <div className="col-6">
                    <label className="form-label" htmlFor="station-opens">Opens</label>
                    <input id="station-opens" type="time" required className="form-control app-input"
                      name="openingTime" value={form.openingTime} onChange={handleChange} />
                  </div>
                  <div className="col-6">
                    <label className="form-label" htmlFor="station-closes">Closes</label>
                    <input id="station-closes" type="time" required className="form-control app-input"
                      name="closingTime" value={form.closingTime} onChange={handleChange} />
                  </div>
                </div>
                <button type="submit" className="btn btn-solar w-100 mt-2" disabled={saving}>
                  {saving ? "Saving..." : editingId ? "Save changes" : "Create station"}
                </button>
                {editingId && (
                  <button type="button" className="btn btn-outline-secondary w-100 mt-2" onClick={cancelEdit}>
                    Cancel edit
                  </button>
                )}
              </fieldset>
            </form>
          </div>
        </div>

        <div className="col-12 col-xl-8">
          <div className="dashboard-card">
            <div className="card-heading-row flex-wrap gap-3">
              <div>
                <h3>Microgrid network</h3>
                <p>{loading ? "Loading station information" : `${filteredStations.length} station(s) shown`}</p>
              </div>
            </div>
            <div className="filter-toolbar">
              <input type="search" className="form-control app-input search-input"
                aria-label="Search stations by name or address"
                placeholder="Search name or address..." value={search}
                onChange={(event) => setSearch(event.target.value)} />
              <select className="form-select app-input filter-select" aria-label="Station status"
                value={statusFilter} onChange={(event) => setStatusFilter(event.target.value)}>
                <option value="All">All statuses</option>
                <option value="Active">Active</option>
                <option value="Inactive">Inactive</option>
              </select>
              <select className="form-select app-input filter-select" aria-label="Slot availability"
                value={availabilityFilter} onChange={(event) => setAvailabilityFilter(event.target.value)}>
                <option value="All">All availability</option>
                <option value="Available">Available slots</option>
                <option value="Full">No available slots</option>
              </select>
            </div>

            {loading ? (
              <div className="loading-state" role="status">
                <div className="spinner-border text-success" aria-hidden="true" />
                <span>Loading stations...</span>
              </div>
            ) : filteredStations.length === 0 ? (
              <div className="empty-state">
                {(search.trim() || statusFilter !== "All" || availabilityFilter !== "All") ? "No stations match your search and filters." : "No stations yet. Create your first microgrid station to get started."}
              </div>
            ) : (
              <div className="table-responsive station-table-scroll" tabIndex={0}
                role="region" aria-label="All stations, scroll to view more">
                <table className="table app-table align-middle mb-0">
                  <thead>
                    <tr>
                      <th scope="col">Station</th>
                      <th scope="col">Capacity</th>
                      <th scope="col">Slots</th>
                      <th scope="col">Status</th>
                      <th scope="col" className="text-end">Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {filteredStations.map((station) => (
                      <tr key={station.id}>
                        <td>
                          <strong>{station.name}</strong>
                          <span className="table-subtext">{station.address}</span>
                          <span className="table-subtext">
                            {station.openingTime} – {station.closingTime}
                          </span>
                        </td>
                        <td className="text-nowrap">{station.capacityKw} kW</td>
                        <td>
                          <span className="text-nowrap">{station.availableSlots} / {station.totalSlots}</span>
                          <span className="table-subtext">available</span>
                        </td>
                        <td><StatusBadge status={station.status} /></td>
                        <td>
                          <div className="d-flex flex-wrap justify-content-end gap-2">
                            <button type="button" className="btn btn-sm btn-outline-success action-button"
                              disabled={saving} onClick={() => editStation(station)}>
                              Edit
                            </button>
                            <button type="button"
                              className={`btn btn-sm action-button ${station.status === "Active" ? "btn-outline-danger" : "btn-outline-success"}`}
                              disabled={saving || actionId !== null} onClick={() => toggleStatus(station)}>
                              {actionId === station.id ? "Updating..." : station.status === "Active" ? "Deactivate" : "Activate"}
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
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
