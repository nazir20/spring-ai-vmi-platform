import { condition, date, freshness, number, sources } from "../../siloInventory";
import type { Silo } from "../../types/inventory";

function formatReadingTime(value: string) {
  return date.format(new Date(value)) + " UTC";
}

export default function SiloStockDetails({ silo }: { silo: Silo }) {
  return (
    <section className="inventory-panel" aria-labelledby="stock-title">
      <div className="inventory-panel-header">
        <h2 id="stock-title">Capacity and limits</h2>
        <span className={`inventory-status stock-${silo.stockCondition.toLowerCase()}`}>
          {condition[silo.stockCondition].label}
        </span>
      </div>
      <dl className="silo-stock-data">
        <div>
          <dt>Physical capacity</dt>
          <dd>
            {number.format(silo.capacity)} <small>{silo.unit}</small>
          </dd>
        </div>
        <div>
          <dt>Minimum stock</dt>
          <dd>
            {number.format(silo.minimumStock)} <small>{silo.unit}</small>
          </dd>
        </div>
        <div>
          <dt>Operating maximum</dt>
          <dd>
            {number.format(silo.maximumStock)} <small>{silo.unit}</small>
          </dd>
        </div>
      </dl>
      <dl className="silo-reading-data">
        <div>
          <dt>Availability</dt>
          <dd>{silo.status === "ACTIVE" ? "Active" : "Inactive"}</dd>
        </div>
        <div>
          <dt>Latest reading</dt>
          <dd>
            {freshness[silo.readingFreshness]}
            {silo.lastMeasuredAt && <small>{formatReadingTime(silo.lastMeasuredAt)}</small>}
          </dd>
        </div>
        <div>
          <dt>Stock source</dt>
          <dd>
            {silo.stockSource ? sources[silo.stockSource] : "Unknown"}
            {silo.stockObservedAt && <small>{formatReadingTime(silo.stockObservedAt)}</small>}
          </dd>
        </div>
        {silo.demoAsOf && (
          <div>
            <dt>Demo snapshot</dt>
            <dd>{formatReadingTime(silo.demoAsOf)}</dd>
          </div>
        )}
      </dl>
      {silo.description && <p className="silo-description">{silo.description}</p>}
    </section>
  );
}
