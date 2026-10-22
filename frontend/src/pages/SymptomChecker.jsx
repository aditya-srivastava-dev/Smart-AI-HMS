import { useMemo, useState } from "react";
import api, { errorMessage } from "../api";
import { prettySymptom, useLoad } from "../hooks";
import { Alert, Card, Loading } from "../components/ui";

/** Choose symptoms -> the Naive Bayes model suggests the most likely diseases. */
export default function SymptomChecker({ onBook }) {
  const { data: symptoms, loading, error } = useLoad("/prediction/symptoms");
  const [selected, setSelected] = useState([]);
  const [search, setSearch] = useState("");
  const [result, setResult] = useState(null);
  const [problem, setProblem] = useState("");
  const [busy, setBusy] = useState(false);

  const visible = useMemo(() => {
    const q = search.trim().toLowerCase().replace(/ /g, "_");
    return (symptoms || []).filter((s) => !selected.includes(s) && s.includes(q)).slice(0, 12);
  }, [symptoms, search, selected]);

  const add = (s) => {
    setSelected([...selected, s]);
    setSearch("");
    setResult(null);
  };
  const remove = (s) => {
    setSelected(selected.filter((x) => x !== s));
    setResult(null);
  };

  async function predict() {
    setProblem("");
    setBusy(true);
    try {
      const res = await api.post("/prediction/predict", { symptoms: selected });
      setResult(res.data);
    } catch (e) {
      setProblem(errorMessage(e));
    } finally {
      setBusy(false);
    }
  }

  return (
    <Card title="Symptom checker">
      <Loading loading={loading} error={error} />
      {symptoms && (
        <>
          <p className="muted">Type a symptom, pick it from the list, and add all the symptoms you have.</p>
          <input
            className="wide"
            placeholder="Search symptom, e.g. fever, cough, headache"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
          {search && (
            <div className="chips suggestions">
              {visible.length === 0 && <span className="muted">No matching symptom</span>}
              {visible.map((s) => (
                <button key={s} className="chip chip-add" onClick={() => add(s)}>
                  + {prettySymptom(s)}
                </button>
              ))}
            </div>
          )}

          <h4>Selected symptoms ({selected.length})</h4>
          <div className="chips">
            {selected.length === 0 && <span className="muted">Nothing selected yet</span>}
            {selected.map((s) => (
              <button key={s} className="chip chip-on" onClick={() => remove(s)} title="Click to remove">
                {prettySymptom(s)} ✕
              </button>
            ))}
          </div>

          <button className="btn" disabled={selected.length === 0 || busy} onClick={predict}>
            {busy ? "Checking..." : "Check likely diseases"}
          </button>
          <Alert>{problem}</Alert>
        </>
      )}

      {result && (
        <div className="result">
          <h4>Most likely diseases</h4>
          {result.urgent && (
            <Alert type="warning">
              One of these conditions can be serious. Please see a doctor soon. If you have severe chest pain,
              weakness of the face or limbs, or trouble breathing, go to the emergency department now.
            </Alert>
          )}
          {result.results.map((r) => (
            <div key={r.disease} className="bar-row">
              <div className="bar-label">
                {r.disease} {r.urgent && <span className="badge badge-cancelled">see doctor soon</span>}
              </div>
              <div className="bar">
                <div className="bar-fill" style={{ width: `${Math.max(r.probability, 2)}%` }} />
              </div>
              <div className="bar-value">{r.probability}%</div>
            </div>
          ))}
          <Alert type="info">{result.disclaimer}</Alert>
          {onBook && (
            <button className="btn btn-light" onClick={onBook}>
              Book a doctor appointment
            </button>
          )}
        </div>
      )}
    </Card>
  );
}
