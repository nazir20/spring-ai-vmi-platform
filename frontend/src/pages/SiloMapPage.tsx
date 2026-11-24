import { useEffect, useMemo } from "react";
import { Link, useSearchParams } from "react-router-dom";
import SiloMap from "../components/SiloMap";
import PageHeader from "../components/layout/PageHeader";
import { useSpringRequest } from "../hooks/useSpringRequest";
import { condition, groupMappedSites, siloPath } from "../siloInventory";
import type { Silo, StockCondition } from "../types/inventory";

const legend: StockCondition[] = ["LOW", "HIGH", "NORMAL", "UNKNOWN"];

export default function SiloMapPage() {
  const result = useSpringRequest<Silo[]>("/api/silos");
  const sites = useMemo(() => groupMappedSites(result?.data ?? []), [result?.data]);
  const [query] = useSearchParams();
  const focusId = Number(query.get("silo"));
  const mappedIds = new Set(sites.flatMap((site) => site.silos.map((silo) => silo.id)));
  const unmapped = result?.data?.filter((silo) => !mappedIds.has(silo.id)) ?? [];
  useEffect(() => { document.title = "Silo Map | Orbit Logistics"; }, []);

  return (
    <div className="silo-inventory-view">
      <PageHeader title="Silo Map" subtitle="Site locations and silo stock conditions."
        actions={<Link to="/inventory/silos" className="inventory-button"><i className="bi bi-list-ul" aria-hidden="true" /> Silo list</Link>} />
      {result?.error && <div className="alert alert-warning" role="alert">
        {result.error}
      </div>}
      {!result && <p role="status">Loading site locations…</p>}
      {result?.data && <div className="silo-map-layout">
        <section className="inventory-panel silo-map-panel" aria-label="Germany site map">
          <div className="inventory-panel-header">
            <h2>Site locations</h2>
            <span className="silo-site-count">{sites.length} {sites.length === 1 ? "site" : "sites"} · {mappedIds.size} {mappedIds.size === 1 ? "silo" : "silos"}</span>
          </div>
          <SiloMap sites={sites} focusSiloId={focusId || undefined} />
          <ul className="silo-map-legend" aria-label="Stock condition legend">
            {legend.map((status) => <li key={status} className={`stock-${status.toLowerCase()}`}>
              <span aria-hidden="true">{condition[status].symbol}</span> {condition[status].label}
            </li>)}
          </ul>
          {sites.length === 0 && <p className="silo-map-note">No silos with saved site coordinates.</p>}
        </section>
        <aside className="inventory-panel silo-map-directory" aria-labelledby="site-directory-title">
          <div className="inventory-panel-header"><h2 id="site-directory-title">Silos by site</h2></div>
          {sites.map((site) => <section className="silo-directory-site" key={site.id}>
            <h3>{site.name}</h3>
            {site.city && <p>{site.city}</p>}
            <ul>{site.silos.map((silo) => <SiloDirectoryRow silo={silo} focus={silo.id === focusId} key={silo.id} />)}</ul>
          </section>)}
          {unmapped.length > 0 && <section className="silo-directory-site">
            <h3>Location unavailable</h3>
            <ul>{unmapped.map((silo) => <SiloDirectoryRow silo={silo} focus={silo.id === focusId} key={silo.id} />)}</ul>
          </section>}
          {result.data.length === 0 && <p>No silos are available.</p>}
        </aside>
      </div>}
    </div>
  );
}

function SiloDirectoryRow({ silo, focus }: { silo: Silo; focus: boolean }) {
  return (
    <li><Link to={siloPath(silo)} className={focus ? "silo-directory-selected" : undefined}>
      <span><strong>{silo.name}</strong><small className={`stock-${silo.stockCondition.toLowerCase()}`}>
        {condition[silo.stockCondition].label}
      </small></span>
      <i className="bi bi-chevron-right" aria-hidden="true" />
    </Link></li>
  );
}
