import { slotWindow } from "../../shipmentOperations";
import type { ShipmentBriefProps } from "../../types/shipments";

export default function ShipmentBrief({
  shipment,
  aiAvailable,
  busy,
  disabled,
  text,
  onDraft,
}: ShipmentBriefProps) {
  const firstSlot = shipment.availableSlots[0];
  return (
    <div className="shipment-brief">
      <div>
        <h3>Rules summary</h3>
        {shipment.exceptions.length ? (
          <ul>
            {shipment.exceptions.map((exception) => (
              <li key={exception.code}>
                <strong>{exception.title}</strong> · {exception.recommendation}
              </li>
            ))}
          </ul>
        ) : (
          <p>No shipment exceptions were flagged.</p>
        )}
        {firstSlot && <p>First compatible window · {slotWindow(firstSlot)}</p>}
      </div>
      <div>
        <h3>KI:connect briefing</h3>
        {aiAvailable ? (
          <button className="inventory-button" type="button" disabled={disabled} onClick={onDraft}>
            {busy ? "Working…" : "Draft briefing"}
          </button>
        ) : (
          <p>Configure the backend’s KI:connect key to generate an AI briefing.</p>
        )}
        {text !== undefined && <p className="shipment-ai-answer">{text}</p>}
      </div>
    </div>
  );
}
