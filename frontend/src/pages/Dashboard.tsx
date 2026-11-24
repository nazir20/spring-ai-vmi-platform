import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import ArrivalsPanel from "../components/dashboard/ArrivalsPanel";
import AttentionQueue from "../components/dashboard/AttentionQueue";
import InventoryPanel from "../components/dashboard/InventoryPanel";
import PerformancePanel from "../components/dashboard/PerformancePanel";
import ShiftBrief from "../components/dashboard/ShiftBrief";
import SummaryCards from "../components/dashboard/SummaryCards";
import PageHeader from "../components/layout/PageHeader";
import { dashboardScope } from "../dashboardData";
import { useShiftBrief } from "../hooks/useShiftBrief";
import { useSpringRequest } from "../hooks/useSpringRequest";
import { usesSpringLogin } from "../api/config";
import "../dashboard.css";
import type { DashboardSnapshot, Filter } from "../types/dashboard";

const welcome = "Welcome to Orbit Logistics VMI Dashboard.";

export default function Dashboard() {
  useEffect(() => {
    document.title = "Dashboard | Orbit Logistics";
  }, []);

  if (!usesSpringLogin) {
    return (
      <div className="silo-inventory-view orbit-dashboard">
        <PageHeader
          title="Dashboard"
          subtitle={welcome}
          actions={
            <div className="inventory-actions">
              <Link className="inventory-button" to="/inventory/map">
                <i className="bi bi-map" aria-hidden="true" /> Silo map
              </Link>
              <Link className="inventory-button" to="/shipments">
                <i className="bi bi-truck" aria-hidden="true" /> Shipments
              </Link>
            </div>
          }
        />
        <SummaryCards />
        <p className="card dash-card">
          Use the Spring backend and sign in with a Spring administrator account to see stock,
          replenishment, and inbound deliveries.
        </p>
      </div>
    );
  }

  return <OperationsDashboard />;
}

function OperationsDashboard() {
  const [revision, refresh] = useState(0);
  const [siteId, setSite] = useState<number | null>(null);
  const [filter, setFilter] = useState<Filter>("all");
  const result = useSpringRequest<DashboardSnapshot>("/api/dashboard", revision);
  const data = result?.data;
  const sites = useMemo(() => {
    const choices = new Map<number, string>();
    data?.silos.forEach((silo) => choices.set(silo.outstationId, silo.outstationName));
    return [...choices].sort((a, b) => a[1].localeCompare(b[1]));
  }, [data]);
  const scope = useMemo(() => (data ? dashboardScope(data, siteId) : null), [data, siteId]);
  const { brief, drafting, draftBrief } = useShiftBrief(siteId, revision);

  function selectSite(value: string) {
    setSite(value ? Number(value) : null);
    setFilter("all");
  }

  return (
    <div className="silo-inventory-view orbit-dashboard">
      <PageHeader
        title="Dashboard"
        subtitle={welcome}
        actions={
          <div className="inventory-actions dash-controls">
            <select
              aria-label="Filter by site"
              className="dash-select"
              value={siteId ?? ""}
              disabled={!data}
              onChange={(event) => selectSite(event.target.value)}
            >
              <option value="">All sites</option>
              {sites.map(([id, name]) => (
                <option value={id} key={id}>
                  {name}
                </option>
              ))}
            </select>
            <button
              type="button"
              className="inventory-button"
              onClick={() => refresh((current) => current + 1)}
              disabled={!result}
              aria-label="Refresh dashboard"
            >
              <i className="bi bi-arrow-clockwise" aria-hidden="true" /> Refresh
            </button>
          </div>
        }
      />
      {!result && <p role="status">Loading dashboard…</p>}
      {result?.error && (
        <p className="alert alert-warning" role="alert">
          {result.error}
        </p>
      )}
      <SummaryCards scope={scope} />
      {data && scope && (
        <>
          <div className="dash-overview mb-4">
            <InventoryPanel silos={scope.silos} />
            <AttentionQueue scope={scope} filter={filter} onFilter={setFilter} />
          </div>
          <div className="dash-support mb-4">
            <ArrivalsPanel arrivals={scope.arrivals} silos={scope.silos} />
            <ShiftBrief
              snapshotTime={data.logistics.demoAsOf}
              aiAvailable={data.logistics.aiAvailable}
              topItem={scope.attention[0]}
              brief={brief}
              drafting={drafting}
              onDraft={draftBrief}
            />
          </div>
          <PerformancePanel performance={data.performance} siteId={siteId} />
        </>
      )}
    </div>
  );
}
