export function Alert({ type = "error", children }) {
  if (!children) return null;
  return <div className={`alert alert-${type}`}>{children}</div>;
}

export function StatusBadge({ status }) {
  return <span className={`badge badge-${(status || "").toLowerCase()}`}>{status}</span>;
}

export function Tabs({ tabs, active, onChange }) {
  return (
    <div className="tabs">
      {tabs.map((t) => (
        <button key={t.id} className={t.id === active ? "tab active" : "tab"} onClick={() => onChange(t.id)}>
          {t.label}
        </button>
      ))}
    </div>
  );
}

export function Card({ title, children, actions }) {
  return (
    <section className="card">
      {(title || actions) && (
        <div className="card-head">
          <h3>{title}</h3>
          <div>{actions}</div>
        </div>
      )}
      {children}
    </section>
  );
}

export function Loading({ loading, error }) {
  if (loading) return <p className="muted">Loading...</p>;
  return <Alert>{error}</Alert>;
}

export function Empty({ children }) {
  return <p className="muted">{children}</p>;
}
