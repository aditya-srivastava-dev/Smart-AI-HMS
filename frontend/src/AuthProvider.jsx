import { useCallback, useEffect, useState } from "react";
import api from "./api";
import { AuthContext } from "./auth";

export default function AuthProvider({ children }) {
  const [user, setUser] = useState(null);
  const [loading, setLoading] = useState(() => !!localStorage.getItem("token"));

  // If a token is already saved, ask the backend who we are
  useEffect(() => {
    if (!localStorage.getItem("token")) return;
    api
      .get("/me")
      .then((res) => setUser(res.data))
      .catch(() => localStorage.removeItem("token"))
      .finally(() => setLoading(false));
  }, []);

  const login = useCallback(async (email, password) => {
    const res = await api.post("/auth/login", { email, password });
    localStorage.setItem("token", res.data);
    const me = await api.get("/me");
    setUser(me.data);
  }, []);

  const register = useCallback(
    async (name, email, password) => {
      await api.post("/auth/register", { name, email, password });
      await login(email, password);
    },
    [login]
  );

  const logout = useCallback(() => {
    localStorage.removeItem("token");
    setUser(null);
  }, []);

  return (
    <AuthContext.Provider value={{ user, loading, login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}
