import type { Concern, Shipment } from "../../types/shipments";

export default function ShipmentExceptions({
  shipment,
  busy,
  review,
}: {
  shipment: Shipment;
  busy: boolean;
  review: (code: Concern["code"]) => void;
}) {
  if (!shipment.exceptions.length)
    return <p className="shipment-muted">No exceptions flagged for this shipment.</p>;
  return (
    <ul className="shipment-task-list">
      {shipment.exceptions.map((exception) => (
        <li key={exception.code}>
          <div>
            <strong>{exception.title}</strong>
            <p>{exception.evidence}</p>
            <p>{exception.recommendation}</p>
          </div>
          {exception.reviewed ? (
            <span className="shipment-reviewed">
              <i className="bi bi-check2" aria-hidden="true" /> Reviewed
            </span>
          ) : (
            <button
              className="inventory-button"
              type="button"
              disabled={busy}
              onClick={() => review(exception.code)}
              aria-label={`Acknowledge ${exception.title}`}
            >
              Acknowledge
            </button>
          )}
        </li>
      ))}
    </ul>
  );
}
