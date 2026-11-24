import { shipmentTime } from "../../shipmentOperations";
import type { Shipment } from "../../types/shipments";

export default function ShipmentDisruptions({
  shipments,
  onSelect,
}: {
  shipments: Shipment[];
  onSelect: (id: number) => void;
}) {
  const affectedShipments = shipments.filter(
    (shipment) =>
      shipment.disruption && shipment.exceptions.some((exception) => exception.code === "DISRUPTION"),
  );
  const issues = new Map<string, NonNullable<Shipment["disruption"]>>();
  for (const shipment of affectedShipments) {
    if (shipment.disruption) {
      issues.set(shipment.disruption.code, shipment.disruption);
    }
  }
  if (!issues.size)
    return <p className="shipment-muted">No active disruptions are linked to the inbound routes.</p>;
  return (
    <ul className="shipment-task-list">
      {[...issues.values()].map((issue) => (
        <li key={issue.code}>
          <div>
            <strong>{issue.title}</strong>
            <p>{issue.detail}</p>
            <p>Reported {shipmentTime(issue.reportedAt)}</p>
          </div>
          <div className="inventory-actions">
            {affectedShipments
              .filter((shipment) => shipment.disruption?.code === issue.code)
              .map((shipment) => (
                <button
                  className="inventory-button"
                  key={shipment.id}
                  type="button"
                  onClick={() => onSelect(shipment.id)}
                >
                  Review {shipment.reference}
                </button>
              ))}
          </div>
        </li>
      ))}
    </ul>
  );
}
