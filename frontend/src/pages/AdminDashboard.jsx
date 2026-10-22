import { useState } from "react";
import api, { errorMessage } from "../api";
import { useLoad } from "../hooks";
import Layout from "../components/Layout";
import { Alert, Card, Empty, Loading, Tabs } from "../components/ui";
import AppointmentTable from "./AppointmentTable";

function Stat({ label, value }) {
  return (
    <div className="stat">
      <div className="stat-value">{value}</div>
      <div className="stat-label">{label}</div>
    </div>
  );
}

function Overview() {
  const stats = useLoad("/admin/dashboard");
  const metrics = useLoad("/prediction/metrics");
  const s = stats.data;
  const m = metrics.data;
  return (
    <>
      <Card title="Hospital overview">
        <Loading loading={stats.loading} error={stats.error} />
        {s && (
          <>
            <div className="stats">
              <Stat label="Users" value={s.users} />
              <Stat label="Patients" value={s.patients} />
              <Stat label="Doctors" value={s.doctors} />
              <Stat label="Appointments" value={s.appointments} />
              <Stat label="Symptom checks" value={s.predictions} />
            </div>
            <h4>Appointments by status</h4>
            <div className="chips">
              {Object.keys(s.appointmentsByStatus).length === 0 && <span className="muted">No appointments yet</span>}
              {Object.entries(s.appointmentsByStatus).map(([k, v]) => (
                <span key={k} className="chip">
                  {k}: {v}
                </span>
              ))}
            </div>
            <h4>Most predicted diseases</h4>
            {s.topPredictedDiseases.length === 0 && <Empty>No symptom checks yet.</Empty>}
            {s.topPredictedDiseases.map((d) => (
              <div key={d.disease} className="small">
                {d.disease}: {d.count}
              </div>
            ))}
          </>
        )}
      </Card>
      <Card title="Prediction model (Naive Bayes)">
        <Loading loading={metrics.loading} error={metrics.error} />
        {m && (
          <>
            <div className="stats">
              <Stat label="Diseases" value={m.diseases} />
              <Stat label="Symptoms" value={m.symptoms} />
              <Stat label="Unique records" value={m.uniqueRecords} />
              <Stat label="Accuracy (average)" value={`${m.accuracyPercent}%`} />
              <Stat label="Top-3 accuracy" value={`${m.top3AccuracyPercent}%`} />
            </div>
            <p className="muted small">
              Average of {m.splitsTested} random 80/20 splits ({m.trainRecords} training / {m.testRecords} test records).
              Lowest {m.lowestAccuracyPercent}%, highest {m.highestAccuracyPercent}%.
            </p>
          </>
        )}
      </Card>
    </>
  );
}

function Doctors() {
  const { data, loading, error, reload } = useLoad("/doctors");
  const empty = { name: "", email: "", password: "", specialization: "", phone: "", licenseNumber: "" };
  const [form, setForm] = useState(empty);
  const [msg, setMsg] = useState({ type: "", text: "" });
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function create(e) {
    e.preventDefault();
    setMsg({ type: "", text: "" });
    try {
      await api.post("/admin/doctors", form);
      setMsg({ type: "success", text: "Doctor account created" });
      setForm(empty);
      reload();
    } catch (err) {
      setMsg({ type: "error", text: errorMessage(err) });
    }
  }

  async function remove(d) {
    if (!window.confirm(`Delete ${d.name}? The doctor's login will also be removed.`)) return;
    try {
      await api.delete(`/doctors/${d.id}`);
      reload();
    } catch (err) {
      setMsg({ type: "error", text: errorMessage(err) });
    }
  }

  return (
    <>
      <Card title="Add a doctor">
        <form className="form" onSubmit={create}>
          <div className="row">
            <label>
              Name
              <input value={form.name} onChange={set("name")} required />
            </label>
            <label>
              Specialization
              <input value={form.specialization} onChange={set("specialization")} required />
            </label>
          </div>
          <div className="row">
            <label>
              Email (login)
              <input type="email" value={form.email} onChange={set("email")} required />
            </label>
            <label>
              Password
              <input type="password" minLength={6} value={form.password} onChange={set("password")} required />
            </label>
          </div>
          <div className="row">
            <label>
              Phone
              <input value={form.phone} onChange={set("phone")} />
            </label>
            <label>
              License number
              <input value={form.licenseNumber} onChange={set("licenseNumber")} required />
            </label>
          </div>
          <Alert type={msg.type || "error"}>{msg.text}</Alert>
          <button className="btn">Create doctor account</button>
        </form>
      </Card>
      <Card title="Doctors">
        <Loading loading={loading} error={error} />
        {data && data.length === 0 && <Empty>No doctors yet.</Empty>}
        {data && data.length > 0 && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Specialization</th>
                  <th>Email</th>
                  <th>Phone</th>
                  <th>License</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {data.map((d) => (
                  <tr key={d.id}>
                    <td>{d.name}</td>
                    <td>{d.specialization}</td>
                    <td>{d.email}</td>
                    <td>{d.phone}</td>
                    <td>{d.licenseNumber}</td>
                    <td>
                      <button className="btn btn-small btn-danger" onClick={() => remove(d)}>
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </>
  );
}

function Patients() {
  const { data, loading, error, reload } = useLoad("/patients");
  const [problem, setProblem] = useState("");

  async function remove(p) {
    if (!window.confirm(`Delete patient ${p.name}? Their login will also be removed.`)) return;
    try {
      await api.delete(`/admin/patients/${p.id}`);
      reload();
    } catch (err) {
      setProblem(errorMessage(err));
    }
  }

  return (
    <Card title="Patients">
      <Loading loading={loading} error={error} />
      <Alert>{problem}</Alert>
      {data && data.length === 0 && <Empty>No patients yet.</Empty>}
      {data && data.length > 0 && (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Age</th>
                <th>Gender</th>
                <th>Phone</th>
                <th />
              </tr>
            </thead>
            <tbody>
              {data.map((p) => (
                <tr key={p.id}>
                  <td>{p.name}</td>
                  <td>{p.email}</td>
                  <td>{p.age || ""}</td>
                  <td>{p.gender}</td>
                  <td>{p.phone}</td>
                  <td>
                    <button className="btn btn-small btn-danger" onClick={() => remove(p)}>
                      Delete
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </Card>
  );
}

function Appointments() {
  const { data, loading, error, reload } = useLoad("/appointments/mine");
  const [problem, setProblem] = useState("");

  async function remove(a) {
    if (!window.confirm("Delete this appointment?")) return;
    try {
      await api.delete(`/appointments/${a.id}`);
      reload();
    } catch (err) {
      setProblem(errorMessage(err));
    }
  }

  return (
    <Card title="All appointments">
      <Loading loading={loading} error={error} />
      <Alert>{problem}</Alert>
      {data && (
        <AppointmentTable
          rows={data}
          showPatient
          showDoctor
          actions={(a) => (
            <button className="btn btn-small btn-danger" onClick={() => remove(a)}>
              Delete
            </button>
          )}
        />
      )}
    </Card>
  );
}

function Users() {
  const { data, loading, error } = useLoad("/admin/users");
  const [form, setForm] = useState({ email: "", newPassword: "" });
  const [msg, setMsg] = useState({ type: "", text: "" });

  async function reset(e) {
    e.preventDefault();
    setMsg({ type: "", text: "" });
    try {
      const res = await api.post("/admin/users/reset-password", form);
      setMsg({ type: "success", text: res.data.message });
      setForm({ email: "", newPassword: "" });
    } catch (err) {
      setMsg({ type: "error", text: errorMessage(err) });
    }
  }

  return (
    <>
      <Card title="Reset a user's password">
        <form className="form" onSubmit={reset}>
          <div className="row">
            <label>
              User email
              <input type="email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} required />
            </label>
            <label>
              New password
              <input
                type="password"
                minLength={6}
                value={form.newPassword}
                onChange={(e) => setForm({ ...form, newPassword: e.target.value })}
                required
              />
            </label>
          </div>
          <Alert type={msg.type || "error"}>{msg.text}</Alert>
          <button className="btn">Reset password</button>
        </form>
      </Card>
      <Card title="All users">
        <Loading loading={loading} error={error} />
        {data && (
          <div className="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Email</th>
                  <th>Role</th>
                </tr>
              </thead>
              <tbody>
                {data.map((u) => (
                  <tr key={u.id}>
                    <td>{u.name}</td>
                    <td>{u.email}</td>
                    <td>{u.role}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </>
  );
}

export default function AdminDashboard() {
  const [tab, setTab] = useState("overview");
  const tabs = [
    { id: "overview", label: "Overview" },
    { id: "doctors", label: "Doctors" },
    { id: "patients", label: "Patients" },
    { id: "appointments", label: "Appointments" },
    { id: "users", label: "Users" },
  ];
  return (
    <Layout title="Admin">
      <Tabs tabs={tabs} active={tab} onChange={setTab} />
      {tab === "overview" && <Overview />}
      {tab === "doctors" && <Doctors />}
      {tab === "patients" && <Patients />}
      {tab === "appointments" && <Appointments />}
      {tab === "users" && <Users />}
    </Layout>
  );
}
