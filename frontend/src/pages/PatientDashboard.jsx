import { useState } from "react";
import api, { errorMessage } from "../api";
import { formatDateTime, prettySymptom, useLoad } from "../hooks";
import Layout from "../components/Layout";
import { Alert, Card, Empty, Loading, Tabs } from "../components/ui";
import SymptomChecker from "./SymptomChecker";
import AppointmentTable from "./AppointmentTable";
import RecordList from "./RecordList";

const TIME_SLOTS = [];
for (let h = 9; h <= 17; h++) {
  for (const m of ["00", "30"]) {
    if (h === 17 && m === "30") continue;
    TIME_SLOTS.push(`${String(h).padStart(2, "0")}:${m}`);
  }
}
const today = () => new Date().toISOString().slice(0, 10);

function BookAppointment({ onBooked }) {
  const { data: doctors, loading, error } = useLoad("/doctors");
  const [form, setForm] = useState({ doctorId: "", appointmentDate: "", appointmentTime: "09:00", reason: "" });
  const [msg, setMsg] = useState({ type: "", text: "" });
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function submit(e) {
    e.preventDefault();
    setMsg({ type: "", text: "" });
    try {
      await api.post("/appointments", form);
      setMsg({ type: "success", text: "Appointment booked. The doctor will confirm it." });
      setForm({ ...form, reason: "" });
      onBooked();
    } catch (err) {
      setMsg({ type: "error", text: errorMessage(err) });
    }
  }

  return (
    <Card title="Book an appointment">
      <Loading loading={loading} error={error} />
      {doctors && doctors.length === 0 && <Empty>No doctors are available yet. Please ask the admin to add doctors.</Empty>}
      {doctors && doctors.length > 0 && (
        <form className="form" onSubmit={submit}>
          <label>
            Doctor
            <select value={form.doctorId} onChange={set("doctorId")} required>
              <option value="">-- choose a doctor --</option>
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.name} ({d.specialization})
                </option>
              ))}
            </select>
          </label>
          <div className="row">
            <label>
              Date
              <input type="date" min={today()} value={form.appointmentDate} onChange={set("appointmentDate")} required />
            </label>
            <label>
              Time
              <select value={form.appointmentTime} onChange={set("appointmentTime")}>
                {TIME_SLOTS.map((t) => (
                  <option key={t}>{t}</option>
                ))}
              </select>
            </label>
          </div>
          <label>
            Reason
            <textarea rows={3} value={form.reason} onChange={set("reason")} placeholder="Describe your problem briefly" required />
          </label>
          <Alert type={msg.type || "error"}>{msg.text}</Alert>
          <button className="btn">Book appointment</button>
        </form>
      )}
    </Card>
  );
}

function MyAppointments({ refreshKey }) {
  const { data, loading, error, reload } = useLoad(`/appointments/mine?k=${refreshKey}`);
  const [problem, setProblem] = useState("");

  async function cancel(id) {
    setProblem("");
    try {
      await api.put(`/appointments/${id}/status`, null, { params: { status: "CANCELLED" } });
      reload();
    } catch (e) {
      setProblem(errorMessage(e));
    }
  }

  return (
    <Card title="My appointments">
      <Loading loading={loading} error={error} />
      <Alert>{problem}</Alert>
      {data && (
        <AppointmentTable
          rows={data}
          showDoctor
          actions={(a) =>
            (a.status === "PENDING" || a.status === "CONFIRMED") && (
              <button className="btn btn-small btn-danger" onClick={() => cancel(a.id)}>
                Cancel
              </button>
            )
          }
        />
      )}
    </Card>
  );
}

function PredictionHistory() {
  const { data, loading, error } = useLoad("/prediction/history");
  return (
    <Card title="My symptom check history">
      <Loading loading={loading} error={error} />
      {data && data.length === 0 && <Empty>You have not used the symptom checker yet.</Empty>}
      {data &&
        data.map((p) => (
          <div key={p.id} className="record">
            <div className="record-head">
              <strong>{p.results?.[0]?.disease}</strong>
              <small className="muted">{formatDateTime(p.createdAt)}</small>
            </div>
            <div className="muted small">Symptoms: {p.symptoms.map(prettySymptom).join(", ")}</div>
            <div className="small">
              {p.results.map((r) => `${r.disease} ${r.probability}%`).join("  ·  ")}
            </div>
          </div>
        ))}
    </Card>
  );
}

function MyRecords() {
  const { data, loading, error } = useLoad("/records/mine");
  return (
    <Card title="My medical records">
      <Loading loading={loading} error={error} />
      {data && <RecordList rows={data} />}
    </Card>
  );
}

function Profile() {
  const { data, loading, error } = useLoad("/patients/me");
  return (
    <Card title="My profile">
      <Loading loading={loading} error={error} />
      {data && <ProfileForm patient={data} />}
    </Card>
  );
}

function ProfileForm({ patient }) {
  const [form, setForm] = useState({
    name: patient.name || "",
    age: patient.age || 0,
    gender: patient.gender || "",
    phone: patient.phone || "",
  });
  const [msg, setMsg] = useState({ type: "", text: "" });
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function save(e) {
    e.preventDefault();
    setMsg({ type: "", text: "" });
    try {
      await api.put(`/patients/${patient.id}`, { ...form, age: Number(form.age) });
      setMsg({ type: "success", text: "Profile saved" });
    } catch (err) {
      setMsg({ type: "error", text: errorMessage(err) });
    }
  }

  return (
    <form className="form" onSubmit={save}>
      <label>
        Name
        <input value={form.name} onChange={set("name")} required />
      </label>
      <label>
        Email
        <input value={patient.email} disabled />
      </label>
      <div className="row">
        <label>
          Age
          <input type="number" min="0" max="120" value={form.age} onChange={set("age")} />
        </label>
        <label>
          Gender
          <select value={form.gender} onChange={set("gender")}>
            <option value="">--</option>
            <option>Male</option>
            <option>Female</option>
            <option>Other</option>
          </select>
        </label>
      </div>
      <label>
        Phone
        <input value={form.phone} onChange={set("phone")} />
      </label>
      <Alert type={msg.type || "error"}>{msg.text}</Alert>
      <button className="btn">Save profile</button>
    </form>
  );
}

export default function PatientDashboard() {
  const [tab, setTab] = useState("check");
  const [refreshKey, setRefreshKey] = useState(0);

  const tabs = [
    { id: "check", label: "Symptom checker" },
    { id: "book", label: "Book appointment" },
    { id: "appointments", label: "My appointments" },
    { id: "history", label: "Check history" },
    { id: "records", label: "Medical records" },
    { id: "profile", label: "Profile" },
  ];

  return (
    <Layout title="Patient">
      <Tabs tabs={tabs} active={tab} onChange={setTab} />
      {tab === "check" && <SymptomChecker onBook={() => setTab("book")} />}
      {tab === "book" && (
        <BookAppointment
          onBooked={() => setRefreshKey((k) => k + 1)}
        />
      )}
      {tab === "appointments" && <MyAppointments refreshKey={refreshKey} />}
      {tab === "history" && <PredictionHistory />}
      {tab === "records" && <MyRecords />}
      {tab === "profile" && <Profile />}
    </Layout>
  );
}
