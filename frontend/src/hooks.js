import { useCallback, useEffect, useState } from "react";
import api, { errorMessage } from "./api";

/** Loads data from a GET url and gives {data, error, loading, reload}. */
export function useLoad(url) {
  const [state, setState] = useState({ data: null, error: "", loading: true });

  const reload = useCallback(() => {
    return api
      .get(url)
      .then((res) => setState({ data: res.data, error: "", loading: false }))
      .catch((e) => setState({ data: null, error: errorMessage(e), loading: false }));
  }, [url]);

  useEffect(() => {
    let cancelled = false;
    api
      .get(url)
      .then((res) => !cancelled && setState({ data: res.data, error: "", loading: false }))
      .catch((e) => !cancelled && setState({ data: null, error: errorMessage(e), loading: false }));
    return () => {
      cancelled = true;
    };
  }, [url]);

  return { ...state, reload };
}

/** "skin_rash" -> "Skin rash" */
export function prettySymptom(name) {
  const text = name.replace(/_/g, " ").trim();
  return text.charAt(0).toUpperCase() + text.slice(1);
}

export function formatDateTime(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  return isNaN(d) ? iso : d.toLocaleString();
}
