import { Link } from "react-router-dom";
import { condition, freshness, siloPath } from "../../siloInventory";
import type { Silo } from "../../types/inventory";

const percent = new Intl.NumberFormat(undefined, { style: "percent", maximumFractionDigits: 1 });
const boundedPercent = (value: number) => Math.min(100, Math.max(0, value));

export default function InventoryPanel({ silos }: { silos: Silo[] }) {
  const groups = new Map<number, Silo[]>();
  for (const silo of silos) {
    const group = groups.get(silo.outstationId);
    if (group) group.push(silo);
    else groups.set(silo.outstationId, [silo]);
  }
  return (
    <section className="card dash-card dash-inventory" aria-labelledby="dashboard-inventory">
      <div className="card-header dash-panel-header">
        <h2 className="card-title" id="dashboard-inventory">
          Inventory Overview
        </h2>
        <Link className="dash-inline-link" to="/inventory/map">
          Silo map <i className="bi bi-arrow-up-right" aria-hidden="true" />
        </Link>
      </div>
      {groups.size ? (
        <div className="dash-site-grid">
          {[...groups].map(([site, items]) => (
            <div className="dash-site" key={site}>
              <h3>{items[0].outstationName}</h3>
              {items.map((silo) => (
                <Link className="dash-silo-row" key={silo.id} to={siloPath(silo)}>
                  <span className="dash-silo-name">
                    <strong>{silo.name}</strong>
                    <small>{silo.productName}</small>
                  </span>
                  <span className={`dash-silo-level stock-${silo.stockCondition.toLowerCase()}`}>
                    <span className="dash-silo-value">
                      {silo.fillPercentage == null
                        ? "Fill unknown"
                        : percent.format(silo.fillPercentage / 100)}
                    </span>
                    <span className="dash-fill" aria-hidden="true">
                      {silo.fillPercentage != null && (
                        <span
                          className="dash-fill-value"
                          style={{ width: `${boundedPercent(silo.fillPercentage)}%` }}
                        />
                      )}
                      <span
                        className="dash-fill-mark"
                        style={{ left: `${boundedPercent((silo.minimumStock / silo.capacity) * 100)}%` }}
                      />
                      <span
                        className="dash-fill-mark"
                        style={{ left: `${boundedPercent((silo.maximumStock / silo.capacity) * 100)}%` }}
                      />
                    </span>
                    <small>
                      {silo.status === "INACTIVE"
                        ? "Inactive"
                        : silo.readingFreshness !== "FRESH"
                          ? freshness[silo.readingFreshness]
                          : condition[silo.stockCondition].label}
                    </small>
                  </span>
                  <i className="bi bi-chevron-right" aria-hidden="true" />
                </Link>
              ))}
            </div>
          ))}
        </div>
      ) : (
        <p className="dash-empty">No silos available.</p>
      )}
      <p className="dash-panel-note">
        <i className="bi bi-layout-sidebar" aria-hidden="true" /> Bar marks indicate minimum and
        operating maximum.
      </p>
    </section>
  );
}
