import { shipmentTime } from "../../shipmentOperations";
import { number } from "../../siloInventory";
import type { Shipment } from "../../types/shipments";

export default function ShipmentJourney({ shipment }: { shipment: Shipment }) {
  const track = shipment.tracking;
  const last = track?.lastCheckpoint == null ? undefined : track.checkpoints[track.lastCheckpoint];
  return (
    <div className="shipment-journey">
      <dl>
        <div>
          <dt>ETA</dt>
          <dd>{shipmentTime(track?.etaAt)}</dd>
        </div>
        <div>
          <dt>Promised arrival</dt>
          <dd>
            {track?.promisedAt
              ? shipmentTime(track.promisedAt)
              : (shipment.expectedDate ?? "Unavailable")}
          </dd>
        </div>
        <div>
          <dt>Last checkpoint</dt>
          <dd>{last?.label ?? "Unavailable"}</dd>
        </div>
        <div>
          <dt>Position updated</dt>
          <dd>{shipmentTime(track?.lastUpdateAt)}</dd>
        </div>
        <div>
          <dt>Carrier</dt>
          <dd>{shipment.carrier ?? "Unavailable"}</dd>
        </div>
        <div>
          <dt>Purchase order</dt>
          <dd>{shipment.orderNumber}</dd>
        </div>
        <div>
          <dt>Destination silos</dt>
          <dd>{shipment.silos ?? "Unavailable"}</dd>
        </div>
        <div>
          <dt>Cargo</dt>
          <dd>
            {shipment.quantity == null
              ? "Unavailable"
              : `${number.format(shipment.quantity)} ${shipment.unit ?? ""}`}
          </dd>
        </div>
      </dl>
      {!!track?.checkpoints.length && (
        <ol className="shipment-checkpoints" aria-label="Route checkpoints">
          {track.checkpoints.map((point, index) => (
            <li
              key={`${point.label}-${index}`}
              aria-current={track.lastCheckpoint === index ? "location" : undefined}
            >
              {point.label}
            </li>
          ))}
        </ol>
      )}
    </div>
  );
}
