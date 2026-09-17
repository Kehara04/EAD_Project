import { useEffect, useState } from "react";
import {
  createUser,
  getUsers
} from "../../services/userService";

export default function UserManagementPage() {
  const [users, setUsers] = useState([]);

  const [form, setForm] = useState({
    name: "",
    email: "",
    password: "",
    role: "GridOperator"
  });

  async function load() {
    setUsers(await getUsers());
  }

  useEffect(() => {
    load();
  }, []);

  async function submit(e) {
    e.preventDefault();

    await createUser(form);

    setForm({
      name: "",
      email: "",
      password: "",
      role: "GridOperator"
    });

    await load();
  }

  return (
    <div className="container mt-4">
      <h2>User Management</h2>

      <form
        className="card p-3 mb-4"
        onSubmit={submit}
      >
        <input
          className="form-control mb-2"
          placeholder="Name"
          value={form.name}
          onChange={(e) =>
            setForm({
              ...form,
              name: e.target.value
            })
          }
        />

        <input
          className="form-control mb-2"
          placeholder="Email"
          value={form.email}
          onChange={(e) =>
            setForm({
              ...form,
              email: e.target.value
            })
          }
        />

        <input
          type="password"
          className="form-control mb-2"
          placeholder="Password"
          value={form.password}
          onChange={(e) =>
            setForm({
              ...form,
              password: e.target.value
            })
          }
        />

        <select
          className="form-select mb-2"
          value={form.role}
          onChange={(e) =>
            setForm({
              ...form,
              role: e.target.value
            })
          }
        >
          <option value="GridOperator">
            Grid Operator
          </option>

          <option value="Backoffice">
            Backoffice
          </option>
        </select>

        <button className="btn btn-primary">
          Create User
        </button>
      </form>

      <table className="table">
        <thead>
          <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Role</th>
            <th>Status</th>
          </tr>
        </thead>

        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>{u.name}</td>
              <td>{u.email}</td>
              <td>{u.role}</td>
              <td>{u.status}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}