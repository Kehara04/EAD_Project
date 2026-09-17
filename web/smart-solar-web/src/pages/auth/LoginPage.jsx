import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../../services/authService";

export default function LoginPage() {
  const [email, setEmail] = useState("");
  const [password, setPassword] =
    useState("");

  const [error, setError] = useState("");

  const navigate = useNavigate();

  async function handleSubmit(e) {
    e.preventDefault();

    try {
      const user =
        await login(email, password);

      if (user.role === "Backoffice") {
        navigate("/backoffice");
      } else if (
        user.role === "GridOperator"
      ) {
        navigate("/operator");
      }
    } catch {
      setError(
        "Invalid login or inactive account."
      );
    }
  }

  return (
    <div className="container mt-5">
      <div className="col-md-5 mx-auto">
        <div className="card p-4 shadow">
          <h3>Smart Solar Login</h3>

          {error && (
            <div className="alert alert-danger">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit}>
            <input
              className="form-control mb-3"
              placeholder="Email"
              value={email}
              onChange={(e) =>
                setEmail(e.target.value)
              }
            />

            <input
              type="password"
              className="form-control mb-3"
              placeholder="Password"
              value={password}
              onChange={(e) =>
                setPassword(e.target.value)
              }
            />

            <button
              className="btn btn-primary w-100"
            >
              Login
            </button>
          </form>
        </div>
      </div>
    </div>
  );
}