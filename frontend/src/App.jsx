import AuthProvider from "./AuthProvider";
import { useAuth } from "./auth";
import AuthPage from "./pages/AuthPage";
import PatientDashboard from "./pages/PatientDashboard";
import DoctorDashboard from "./pages/DoctorDashboard";
import AdminDashboard from "./pages/AdminDashboard";

function Screen() {
  const { user, loading } = useAuth();
  if (loading) return <p className="center muted">Loading...</p>;
  if (!user) return <AuthPage />;
  if (user.role === "ADMIN") return <AdminDashboard />;
  if (user.role === "DOCTOR") return <DoctorDashboard />;
  return <PatientDashboard />;
}

export default function App() {
  return (
    <AuthProvider>
      <Screen />
    </AuthProvider>
  );
}
