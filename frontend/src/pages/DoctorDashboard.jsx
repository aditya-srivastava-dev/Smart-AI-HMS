import { useState } from "react";
import api, { errorMessage } from "../api";
import { formatDateTime, prettySymptom, useLoad } from "../hooks";
import Layout from "../components/Layout";
import { Alert, Card, Empty, Loading, Tabs } from "../components/ui";
import AppointmentTable from "./AppointmentTable";
import RecordList from "./RecordList";

function WriteRecord({ appointment, onDone, onCancel }) {
  const [form, setForm] = useState({ diagnosis: "", prescription: "", notes: "" });
  const [error, setError] = useState("");
  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value });

  async function save(e) {
    e.preventDefault();
    setError("");
    try {
      await api.post("/records", {
        patientId: appointment.patientId,
        appointmentId: appointment.id,
        ...form,
      });
      onDone();
    } catch (err) {
      setError(errorMessage(err));
    }
  }

  return (
    <Card title={`Medical record for ${appointment.patientName}`}>
      <form className="form" onSubmit={save}>
        <label>
          Diagnosis
          <input value={form.diagnosis} onChange={set("diagnosis")} required />
        </label>
        <label>
          Prescription
          <textarea rows={3} value={form.prescription} onChange={set("prescription")} />
        </label>
        <label>
          Notes
          <textarea rows={3} value={form.notes} onChange={set("notes")} />
        </label>
        <Alert>{error}</Alert>
        <div className="row">
          <button className="btn">Save record and complete appointment</button>
          <button type="button" className="btn btn-light" onClick={onCancel}>
            Close
          </button>
        </div>
      </form>
    </Card>
  );
}

function PatientDetails({ patientId, name, onClose }) {
  const checks = useLoad(`/prediction/patient/${patientId}`);
  const records = useLoad(`/records/patient/${patientId}`);
  return (
    <Card
      title={`Patient history: ${name}`}
      actions={
        <button className="btn btn-small btn-light" onClick={onClose}>
          Close
        </button>
      }
    >
      <h4>Symptom checker results</h4>
      <Loading loading={checks.loading} error={checks.error} />
      {checks.data && checks.data.length === 0 && <Empty>No symptom checks.</Empty>}
      {checks.data &&
        checks.data.map((p) => (
          <div key={p.id} className="record">
            <div className="record-head">
              <strong>{p.results?.[0]?.disease}</strong>
              <small className="muted">{formatDateTime(p.createdAt)}</small>
            </div>
            <div className="muted small">Symptoms: {p.symptoms.map(prettySymptom).join(", ")}</div>
            <div className="small">{p.results.map((r) => `${r.disease} ${r.probability}%`).join("  ·  ")}</div>
          </div>
        ))}
      <h4>Medical records</h4>
      <Loading loading={records.loading} error={records.error} />
      {records.data && <RecordList rows={records.data} />}
    </Card>
  );
}

function Appointments() {
  const { data, loading, error, reload } = useLoad("/appointments/mine");
  const [problem, setProblem] = useState("");
  const [writing, setWriting] = useState(null);
  const [viewing, setViewing] = useState(null);

  async function setStatus(id, status) {
    setProblem("");
    try {
      await api.put(`/appointments/${id}/status`, null, { params: { status } });
      reload();
    } catch (e) {
      setProblem(errorMessage(e));
    }
  }

  return (
    <>
      <Card title="My appointments">
        <Loading loading={loading} error={error} />
        <Alert>{problem}</Alert>
        {data && (
          <AppointmentTable
            rows={data}
            showPatient
            actions={(a) => (
              <>
                {a.status === "PENDING" && (
                  <button className="btn btn-small" onClick={() => setStatus(a.id, "CONFIRMED")}>
                    Confirm
                  </button>
                )}
                {(a.status === "PENDING" || a.status === "CONFIRMED") && (
                  <>
                    <button className="btn btn-small btn-light" onClick={() => setWriting(a)}>
                      Write record
                    </button>
                    <button className="btn btn-small btn-danger" onClick={() => setStatus(a.id, "CANCELLED")}>
                      Cancel
                    </button>
                  </>
                )}
                <button className="btn btn-small btn-light" onClick={() => setViewing(a)}>
                  Patient history
                </button>
              </>
            )}
          />
        )}
      </Card>
      {viewing && <PatientDetails patientId={viewing.patientId} name={viewing.patientName} onClose={() => setViewing(null)} />}
      {writing && (
        <WriteRecord
          appointment={writing}
          onCancel={() => setWriting(null)}
          onDone={() => {
            setWriting(null);
            reload();
          }}
        />
      )}
    </>
  );
}

function WrittenRecords() {
  const { data, loading, error } = useLoad("/records/mine");
  return (
    <Card title="Records I have written">
      <Loading loading={loading} error={error} />
      {data && <RecordList rows={data} showPatient />}
    </Card>
  );
}

export default function DoctorDashboard() {
  const [tab, setTab] = useState("appointments");
  const tabs = [
    { id: "appointments", label: "Appointments" },
    { id: "records", label: "Records" },
  ];
  return (
    <Layout title="Doctor">
      <Tabs tabs={tabs} active={tab} onChange={setTab} />
      {tab === "appointments" && <Appointments />}
      {tab === "records" && <WrittenRecords />}
    </Layout>
  );
}
