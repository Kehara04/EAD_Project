import { useEffect, useState } from "react";
import {
  activateProsumer,
  deactivateProsumer,
  getProsumers,
  reactivateProsumer
} from "../../services/prosumerService";

export default function ProsumerManagementPage() {
  const [prosumers, setProsumers] =
    useState([]);

  async function load() {
    setProsumers(await getProsumers());
  }

  useEffect(() => {
    load();
  }, []);

  async function activate(nic) {
    await activateProsumer(nic);
    load();
  }

  async function deactivate(nic) {
    await deactivateProsumer(nic);
    load();
  }

  async function reactivate(nic) {
    await reactivateProsumer(nic);
    load();
  }

  return (
    <div className="container mt-4">
      <h2>Prosumer Management</h2>

      <table className="table">
        <thead>
          <tr>
            <th>NIC</th>
            <th>Name</th>
            <th>Email</th>
            <th>Status</th>
            <th>Action</th>
          </tr>
        </thead>

        <tbody>
          {prosumers.map((p) => (
            <tr key={p.nic}>
              <td>{p.nic}</td>
              <td>{p.name}</td>
              <td>{p.email}</td>
              <td>{p.status}</td>

              <td>
                {p.status === "Pending" && (
                  <button
                    className="btn btn-success btn-sm"
                    onClick={() =>
                      activate(p.nic)
                    }
                  >
                    Activate
                  </button>
                )}

                {p.status === "Active" && (
                  <button
                    className="btn btn-danger btn-sm"
                    onClick={() =>
                      deactivate(p.nic)
                    }
                  >
                    Deactivate
                  </button>
                )}

                {p.status === "Deactivated" && (
                  <button
                    className="btn btn-warning btn-sm"
                    onClick={() =>
                      reactivate(p.nic)
                    }
                  >
                    Reactivate
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}