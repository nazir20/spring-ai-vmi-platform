import { shipmentRoute, shipmentState, shipmentTime } from "../../shipmentOperations";
import type { Shipment, ShipmentWorkflow } from "../../types/shipments";

export default function ShipmentQueue({
  shipments,
  selected,
  onSelect,
  onOpenWorkflow,
}: {
  shipments: Shipment[];
  selected: Shipment;
  onSelect: (id: number) => void;
  onOpenWorkflow: (mode: ShipmentWorkflow) => void;
}) {
  const slotRisk = selected.exceptions.some((issue) => issue.code === "DOCK_RISK");
  const exceptionCount = selected.exceptions.filter((issue) => !issue.reviewed).length;
  return (
    <section className="inventory-panel shipment-queue" aria-labelledby="dispatch-queue">
      <div className="shipment-panel-heading">
        <h2 id="dispatch-queue">Dispatch queue</h2>
        <span>{shipments.length} shipments</span>
      </div>
      <ul>
        {shipments.map((shipment) => {
          const open = shipment.exceptions.filter((exception) => !exception.reviewed).length;
          return (
            <li key={shipment.id}>
              <button
                type="button"
                aria-pressed={shipment.id === selected.id}
                onClick={() => onSelect(shipment.id)}
              >
                <span
                  className={`shipment-queue-indicator${open ? " shipment-queue-indicator-attention" : ""}`}
                  aria-hidden="true"
                >
                  <i className={`bi bi-${open ? "exclamation-lg" : "truck"}`} />
                </span>
                <span className="shipment-queue-label">
                  <strong>{shipment.reference}</strong>
                  <small>{shipmentRoute(shipment)}</small>
                  <small>
                    {shipmentState[shipment.status]}
                    {open ? ` · ${open} to review` : ""}
                  </small>
                </span>
                <i className="bi bi-chevron-right" aria-hidden="true" />
              </button>
            </li>
          );
        })}
      </ul>
      <div className="shipment-queue-arrival">
        <span>Selected arrival · {selected.reference}</span>
        <strong>{shipmentTime(selected.tracking?.etaAt)}</strong>
        {slotRisk || exceptionCount > 0 ? (
          <button type="button" onClick={() => onOpenWorkflow(slotRisk ? "slots" : "exceptions")}>
            {slotRisk ? "Review dock window" : "Review exceptions"}
            <i className="bi bi-arrow-down-right" aria-hidden="true" />
          </button>
        ) : (
          <span>
            {selected.bookedSlot ? selected.bookedSlot.dock + " booked" : "No dock slot booked"}
          </span>
        )}
      </div>
    </section>
  );
}
