
import { lazy, Suspense } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";

import AppLayout from "./components/layout/AppLayout";
import ProtectedRoute from "./routes/ProtectedRoute";
import PageHeader from "./components/layout/PageHeader";
import "./App.css";
import Login from "./pages/Login";
import Silos from "./pages/Silos";
import Dashboard from "./pages/Dashboard";
import "./siloInventory.css";

const SiloDetail = lazy(() => import("./pages/SiloDetail"));
const SiloMapPage = lazy(() => import("./pages/SiloMapPage"));
const Shipments = lazy(() => import("./pages/Shipments"));
const Assistant = lazy(() => import("./pages/Assistant"));

function PlaceholderPage({ title }: { title: string }) {
  return (
    <PageHeader
      title={title}
      subtitle={`Manage and monitor ${title.toLowerCase()}.`}
    />
  );
}

export default function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/login" element={<Login />} />
        <Route element={<ProtectedRoute />}>
          <Route element={<AppLayout />}>
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/chat" element={<Suspense fallback={<p role="status">Loading assistant…</p>}><Assistant /></Suspense>} />
            <Route path="/inventory/silos" element={<Silos />} />
            <Route path="/inventory/silos/:siloId" element={<Suspense fallback={<p role="status">Loading silo…</p>}><SiloDetail /></Suspense>} />
            <Route path="/inventory/map" element={<Suspense fallback={<p role="status">Loading map…</p>}><SiloMapPage /></Suspense>} />
            <Route path="/inventory/stock" element={<PlaceholderPage title="Stock Levels" />} />
            <Route path="/inventory/consumption" element={<PlaceholderPage title="Consumption" />} />
            <Route path="/inventory/forecasts" element={<PlaceholderPage title="Forecasts" />} />
            <Route path="/shipments" element={<Suspense fallback={<p role="status">Loading shipments…</p>}><Shipments /></Suspense>} />
            <Route path="/purchase-orders" element={<PlaceholderPage title="Purchase Orders" />} />
            <Route path="/alerts" element={<PlaceholderPage title="Alerts" />} />
            <Route path="/users" element={<PlaceholderPage title="User Management" />} />
            <Route path="/settings" element={<PlaceholderPage title="Settings" />} />
          </Route>
        </Route>

        <Route path="*" element={<Navigate to="/dashboard" replace />} />
      </Routes>
    </BrowserRouter>
  );
}
