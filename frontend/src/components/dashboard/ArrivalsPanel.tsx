import { Link } from "react-router-dom";
import { shipmentHref } from "../../dashboardData";
import { dateTime, shipmentState, slotWindow } from "../../shipmentOperations";
import { number } from "../../siloInventory";
import { formatDeliveryDate } from "./dashboardDates";
import type { Silo } from "../../types/inventory";
import type { Shipment } from "../../types/shipments";

export default function ArrivalsPanel({ arrivals, silos }: { arrivals: Shipment[]; silos: Silo[] }) {
  const names = new Map(silos.map((silo) => [silo.id, silo.name]));
  return (
    <section className="card dash-card dash-arrivals" aria-labelledby="dashboard-arrivals">
      <div className="card-header dash-panel-header">
        <h2 className="card-title" id="dashboard-arrivals">
          Upcoming arrivals
        </h2>
        <span className="dash-count">{arrivals.length} inbound</span>
      </div>
      {arrivals.length ? (
        <ol className="dash-arrival-list">
          {arrivals.slice(0, 3).map((shipment) => {
            const target = shipment.siloIds.flatMap((id) => names.get(id) ?? []).join(", ");
            const dockRisk = shipment.exceptions.some((issue) => issue.code === "DOCK_RISK");
            return (
              <li key={shipment.id}>
                <Link to={shipmentHref(shipment, dockRisk ? "slots" : "journey")}>
                  <span className="dash-arrival-top">
                    <strong>{shipment.reference}</strong>
                    <small>
                      {shipment.tracking?.etaAt
                        ? dateTime(shipment.tracking.etaAt)
                        : shipment.expectedDate
                          ? `Due ${formatDeliveryDate(shipment.expectedDate)}`
                          : "ETA unavailable"}
                    </small>
                  </span>
                  <span>
                    {target || shipment.silos || shipment.destination || "Destination unavailable"}
                  </span>
                  <small>
                    {[
                      shipment.quantity == null
                        ? null
                        : `${number.format(shipment.quantity)} ${shipment.unit ?? ""}`,
                      shipmentState[shipment.status],
                    ]
                      .filter(Boolean)
                      .join(" · ")}
                  </small>
                  {dockRisk ? (
                    <small className="dash-arrival-risk">Dock slot at risk</small>
                  ) : (
                    <small>
                      {shipment.bookedSlot
                        ? `${shipment.bookedSlot.dock} · ${slotWindow(shipment.bookedSlot)}`
                        : "Dock slot unbooked"}
                    </small>
                  )}
                </Link>
              </li>
            );
          })}
        </ol>
      ) : (
        <p className="dash-empty">No upcoming arrivals for this site.</p>
      )}
      <Link className="dash-inline-link dash-panel-link" to="/shipments">
        Open shipments and route map <i className="bi bi-arrow-up-right" aria-hidden="true" />
      </Link>
    </section>
  );
}
