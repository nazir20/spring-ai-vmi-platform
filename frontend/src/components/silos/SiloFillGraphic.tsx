import { number } from "../../siloInventory";
import type { Silo } from "../../types/inventory";

const percentage = (value: number) => Math.min(100, Math.max(0, value));

function stockAction(silo: Silo) {
  if (silo.status === "INACTIVE") {
    return {
      tone: "unknown",
      icon: "pause-circle",
      title: "Silo inactive",
      detail: "This silo is out of service.",
    };
  }
  if (silo.stockCondition === "UNKNOWN" || silo.readingFreshness === "MISSING") {
    return {
      tone: "high",
      icon: "exclamation-circle",
      title: "Stock reading needed",
      detail: "Confirm the stock level before making a stock decision.",
    };
  }
  if (silo.readingFreshness === "STALE") {
    return {
      tone: "high",
      icon: "clock-history",
      title: "Verify stock reading",
      detail: "The last reading is stale. Confirm the current level before acting.",
    };
  }
  if (silo.stockCondition === "LOW") {
    const shortage = number.format(silo.minimumStock - silo.currentStock);
    return {
      tone: "low",
      icon: "arrow-down-circle",
      title: "Action needed",
      detail: `${shortage} ${silo.unit} below minimum. Review replenishment.`,
    };
  }
  if (silo.stockCondition === "HIGH") {
    const excess = number.format(silo.currentStock - silo.maximumStock);
    return {
      tone: "high",
      icon: "arrow-up-circle",
      title: "Action needed",
      detail: `${excess} ${silo.unit} above the operating maximum. Check planned deliveries.`,
    };
  }
  return {
    tone: "normal",
    icon: "check-circle",
    title: "No stock action needed",
    detail: "Stock is within its operating limits.",
  };
}

export default function SiloFillGraphic({ silo }: { silo: Silo }) {
  const fill = silo.fillPercentage;
  const min = percentage((silo.minimumStock / silo.capacity) * 100);
  const max = percentage((silo.maximumStock / silo.capacity) * 100);
  const action = stockAction(silo);

  return (
    <figure className={`inventory-panel silo-vessel-panel stock-${action.tone}`}>
      <figcaption className="silo-vessel-caption">
        <div className="silo-vessel-material">
          <span>Stored material</span>
          <strong>{silo.productName}</strong>
          <span className="silo-vessel-on-hand">
            {silo.stockCondition === "UNKNOWN" ? (
              "Stock on hand unknown"
            ) : (
              <>
                <b>
                  {number.format(silo.currentStock)} {silo.unit}
                </b>{" "}
                on hand
              </>
            )}
          </span>
        </div>
        <div className="silo-vessel-fill-stat">
          <strong>{fill === undefined ? "—" : `${number.format(fill)}%`}</strong>
          <span>{fill === undefined ? "Fill unknown" : "Full"}</span>
        </div>
      </figcaption>
      <div className="silo-vessel-action">
        <i className={`bi bi-${action.icon}`} aria-hidden="true" />
        <div>
          <strong>{action.title}</strong>
          <p>{action.detail}</p>
        </div>
      </div>
      <div className="silo-vessel-stage" aria-hidden="true">
        <div className="silo-vessel">
          <div className="silo-vessel-cap" />
          <div className="silo-vessel-shell">
            {fill !== undefined && fill > 0 && (
              <div className="silo-vessel-fill" style={{ height: `${percentage(fill)}%` }} />
            )}
            <div className="silo-vessel-limit silo-vessel-limit-max" style={{ bottom: `${max}%` }} />
            <div className="silo-vessel-limit silo-vessel-limit-min" style={{ bottom: `${min}%` }} />
          </div>
          <div className="silo-vessel-base" />
        </div>
      </div>
      <div className="silo-vessel-key">
        <span>
          <i className="silo-line-min" /> Minimum
        </span>
        <span>
          <i className="silo-line-max" /> Operating maximum
        </span>
      </div>
    </figure>
  );
}
