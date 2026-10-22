import { formatDateTime } from "../hooks";
import { Empty } from "../components/ui";

export default function RecordList({ rows, showPatient }) {
  if (!rows || rows.length === 0) return <Empty>No medical records yet.</Empty>;
  const sorted = [...rows].sort((a, b) => (b.createdAt || "").localeCompare(a.createdAt || ""));
  return (
    <div className="records">
      {sorted.map((r) => (
        <div key={r.id} className="record">
          <div className="record-head">
            <strong>{r.diagnosis}</strong>
            <small className="muted">{formatDateTime(r.createdAt)}</small>
          </div>
          <div className="muted small">
            {showPatient ? `Patient: ${r.patientName} · ` : ""}Doctor: {r.doctorName}
          </div>
          {r.prescription && (
            <p>
              <b>Prescription:</b> {r.prescription}
            </p>
          )}
          {r.notes && (
            <p>
              <b>Notes:</b> {r.notes}
            </p>
          )}
        </div>
      ))}
    </div>
  );
}
