import { Navigate, Outlet, useLocation } from "react-router-dom";

export const PrivateRoute = () => {
  const token = localStorage.getItem("token") || localStorage.getItem("accessToken");
  const location = useLocation();

  // ✅ Allow OAuth redirect route WITHOUT token
  if (location.pathname === "/oauth2/redirect") {
    return <Outlet />;
  }

  if (!token) {
    return <Navigate to="/login" replace />;
  }

  return <Outlet />;
};