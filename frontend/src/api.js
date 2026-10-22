import axios from "axios";

// Backend address. Change it with VITE_API_URL in a .env file if needed.
export const API_URL = import.meta.env.VITE_API_URL || "http://localhost:8080/api";

const api = axios.create({ baseURL: API_URL });

// Put the login token on every request
api.interceptors.request.use((config) => {
  const token = localStorage.getItem("token");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

// Turn any error into a readable message
export function errorMessage(error) {
  const data = error?.response?.data;
  if (data && typeof data === "object" && data.message) return data.message;
  if (typeof data === "string" && data) return data;
  if (error?.response?.status === 403) return "You are not allowed to do this";
  if (error?.code === "ERR_NETWORK") return "Cannot reach the server. Is the backend running?";
  return error?.message || "Something went wrong";
}

export default api;
