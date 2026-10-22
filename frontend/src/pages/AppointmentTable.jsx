import { StatusBadge, Empty } from "../components/ui";

/** Shared table of appointments. `actions(a)` can add buttons for each row. */
export default function AppointmentTable({ rows, showPatient, showDoctor, actions }) {
  if (!rows || rows.length === 0) return <Empty>No appointments yet.</Empty>;
  const sorted = [...rows].sort((a, b) =>
    (b.appointmentDate + b.appointmentTime).localeCompare(a.appointmentDate + a.appointmentTime)
  );
  return (
    <div className="table-wrap">
      <table>
        <thead>
          <tr>
            <th>Date</th>
            <th>Time</th>
            {showPatient && <th>Patient</th>}
            {showDoctor && <th>Doctor</th>}
            <th>Reason</th>
            <th>Status</th>
            {actions && <th>Actions</th>}
          </tr>
        </thead>
        <tbody>
          {sorted.map((a) => (
            <tr key={a.id}>
              <td>{a.appointmentDate}</td>
              <td>{a.appointmentTime}</td>
              {showPatient && <td>{a.patientName}</td>}
              {showDoctor && (
                <td>
                  {a.doctorName}
                  <small className="muted"> {a.specialization}</small>
                </td>
              )}
              <td>{a.reason}</td>
              <td>
                <StatusBadge status={a.status} />
              </td>
              {actions && <td className="actions">{actions(a)}</td>}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
