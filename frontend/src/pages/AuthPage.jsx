import { useState } from "react";
import { useAuth } from "../auth";
import { errorMessage } from "../api";
import { Alert } from "../components/ui";

export default function AuthPage() {
  const { login, register } = useAuth();
  const [mode, setMode] = useState("login");
  const [form, setForm] = useState({ name: "", email: "", password: "" });
  const [error, setError] = useState("");
  const [busy, setBusy] = useState(false);

  const set = (key) => (e) => setForm({ ...form, [key]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setError("");
    setBusy(true);
    try {
      if (mode === "login") await login(form.email, form.password);
      else await register(form.name, form.email, form.password);
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  }

  return (
    <div className="auth-wrap">
      <div className="auth-card">
        <div className="brand brand-big">
          <span className="logo">+</span>
          <div>
            <strong>Smart Healthcare Management Platform</strong>
            <small>AI disease suggestion + hospital management</small>
          </div>
        </div>

        <div className="tabs">
          <button className={mode === "login" ? "tab active" : "tab"} onClick={() => setMode("login")}>
            Login
          </button>
          <button className={mode === "register" ? "tab active" : "tab"} onClick={() => setMode("register")}>
            Register (patient)
          </button>
        </div>

        <form onSubmit={submit} className="form">
          {mode === "register" && (
            <label>
              Full name
              <input value={form.name} onChange={set("name")} required />
            </label>
          )}
          <label>
            Email
            <input type="email" value={form.email} onChange={set("email")} required />
          </label>
          <label>
            Password
            <input type="password" value={form.password} onChange={set("password")} minLength={6} required />
          </label>
          <Alert>{error}</Alert>
          <button className="btn" disabled={busy}>
            {busy ? "Please wait..." : mode === "login" ? "Login" : "Create account"}
          </button>
        </form>
        <p className="muted small">Doctors get their account from the hospital admin.</p>
      </div>
    </div>
  );
}
