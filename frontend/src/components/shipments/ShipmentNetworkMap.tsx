import { dateTime } from "../../shipmentOperations";
import ShipmentMap from "../ShipmentMap";
import type { ShipmentNetworkMapProps } from "../../types/shipments";

export default function ShipmentNetworkMap({
  board,
  selected,
  replayStep,
  onToggleReplay,
  onSelect,
}: ShipmentNetworkMapProps) {
  const tracking = selected.tracking;
  const canReplay = tracking?.source === "SIMULATED" && tracking.checkpoints.length > 1;
  const isReplaying = replayStep !== null;
  const hasSimulatedRoutes = board.shipments.some(
    (shipment) => shipment.tracking?.source === "SIMULATED",
  );

  return (
    <section className="inventory-panel shipment-map-panel" aria-labelledby="shipment-map-title">
      <div className="shipment-panel-heading shipment-map-heading">
        <div>
          <h2 id="shipment-map-title">Inbound route map</h2>
          {board.demoAsOf && <p>Demo snapshot · {dateTime(board.demoAsOf)} · Berlin time</p>}
        </div>
        {canReplay && (
          <button className="inventory-button" type="button" onClick={onToggleReplay}>
            <i className={isReplaying ? "bi bi-stop-fill" : "bi bi-play-fill"} aria-hidden="true" />
            {isReplaying ? "Stop replay" : "Replay route"}
          </button>
        )}
      </div>
      <ShipmentMap
        shipments={board.shipments}
        selectedId={selected.id}
        onSelect={onSelect}
        replayStep={replayStep}
      />
      <div className="shipment-map-legend">
        <span>
          <i className="bi bi-truck-front" aria-hidden="true" /> Last checkpoint
        </span>
        <span>
          <i className="bi bi-exclamation-triangle" aria-hidden="true" /> Disruption
        </span>
        {hasSimulatedRoutes && (
          <span>
            <b aria-hidden="true" /> Simulated routes
          </span>
        )}
        {replayStep !== null && <strong>Replay · {tracking?.checkpoints[replayStep]?.label}</strong>}
      </div>
    </section>
  );
}
