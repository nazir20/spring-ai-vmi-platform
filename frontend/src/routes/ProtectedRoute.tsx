import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../auth/useAuth";

export default function ProtectedRoute() {
  const { session } = useAuth();
  const { pathname, search, hash } = useLocation();

  if (!session) {
    return <Navigate to="/login" state={{ from: pathname + search + hash }} replace />;
  }

  return <Outlet />;
}
