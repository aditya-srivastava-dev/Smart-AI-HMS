import { useAuth } from "../auth";

export default function Layout({ title, children }) {
  const { user, logout } = useAuth();
  return (
    <div className="app">
      <header className="topbar">
        <div className="brand">
          <span className="logo">+</span>
          <div>
            <strong>Smart Healthcare</strong>
            <small>{title}</small>
          </div>
        </div>
        <div className="who">
          <span>
            {user.name} <em>({user.role.toLowerCase()})</em>
          </span>
          <button className="btn btn-light" onClick={logout}>
            Logout
          </button>
        </div>
      </header>
      <main className="content">{children}</main>
    </div>
  );
}
