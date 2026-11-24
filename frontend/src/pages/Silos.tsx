import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import PageHeader from "../components/layout/PageHeader";
import { requestSpring } from "../springApi";
import { condition, date, freshness, number, siloPath, sources } from "../siloInventory";
import type { Silo } from "../types/inventory";

export default function Silos() {
  const [silos, setSilos] = useState<Silo[] | null>(null);
  const [error, setError] = useState<string>();
  const [request, setRequest] = useState(0);

  useEffect(() => { document.title = "Silos & Tanks | Orbit Logistics"; }, []);

  useEffect(() => {
    const controller = new AbortController();
    requestSpring<Silo[]>("/api/silos", { signal: controller.signal })
      .then(setSilos)
      .catch((e: Error) => {
        if (!controller.signal.aborted) setError(e.message);
      });
    return () => controller.abort();
  }, [request]);

  const demoAsOf = silos?.find((silo) => silo.demoAsOf)?.demoAsOf;

  return (
    <>
      <PageHeader title="Silos & Tanks" subtitle="Stock, operating limits, and reading freshness by site."
        actions={<div className="inventory-actions">
          <Link className="inventory-button" to="/inventory/map"><i className="bi bi-map" aria-hidden="true" /> Silo map</Link>
          <button type="button" className="inventory-button"
            onClick={() => { setError(undefined); setSilos(null); setRequest((previous) => previous + 1); }}>Refresh</button>
        </div>} />
      {error && <div className="alert alert-warning" role="alert">
        {error}
      </div>}
      {!error && silos === null && <p role="status">Loading silos…</p>}
      {silos?.length === 0 && <p>No silos are available.</p>}
      {demoAsOf && <p className="text-muted small">Demo snapshot · {date.format(new Date(demoAsOf))} UTC</p>}
      {!!silos?.length && <div className="row g-3">
        {silos.map((silo) => {
          const stock = condition[silo.stockCondition];
          const quantity = (value: number) => `${number.format(value)} ${silo.unit}`;
          return (
            <div className="col-md-6 col-xxl-4" key={silo.id}>
              <article className="card h-100 silo-list-card">
                <div className="card-body">
                  <div className="text-muted small">{silo.outstationName} · {silo.siloCode}</div>
                  <h2 className="h5 my-2">
                    <Link to={siloPath(silo)} className="stretched-link silo-card-link">
                      {silo.name}<i className="bi bi-arrow-up-right" aria-hidden="true" />
                    </Link>
                  </h2>
                  <p className="text-muted mb-3">{silo.productName}</p>
                  <div className="d-flex flex-wrap gap-2 mb-3">
                    <span className={`badge text-bg-${stock.color}`}>{stock.label}</span>
                    <span className="badge text-bg-light">{silo.status === "ACTIVE" ? "Active" : "Inactive"}</span>
                    <span className="badge text-bg-light">{freshness[silo.readingFreshness]}</span>
                  </div>
                  <div className="mb-2">
                    <strong>{silo.stockCondition === "UNKNOWN" ? "Unknown" : quantity(silo.currentStock)}</strong>
                    <span className="text-muted"> / {quantity(silo.capacity)}</span>
                  </div>
                  {silo.fillPercentage !== undefined && <div className="progress mb-2" role="progressbar"
                    aria-label={`${silo.name} fill percentage`} aria-valuenow={silo.fillPercentage}
                    aria-valuemin={0} aria-valuemax={100}>
                    <div className={`progress-bar bg-${stock.color}`} style={{ width: `${silo.fillPercentage}%` }} />
                  </div>}
                  <div className="text-muted small">
                    {silo.fillPercentage !== undefined && `${number.format(silo.fillPercentage)}% full · `}
                    Limits {quantity(silo.minimumStock)}–{quantity(silo.maximumStock)}
                  </div>
                  {silo.stockSource && silo.stockObservedAt && <div className="text-muted small mt-2">
                    {sources[silo.stockSource]} · {date.format(new Date(silo.stockObservedAt))} UTC
                  </div>}
                </div>
              </article>
            </div>
          );
        })}
      </div>}
    </>
  );
}
