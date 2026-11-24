import { slotWindow } from "../../shipmentOperations";
import type { DockSlot, Shipment } from "../../types/shipments";

export default function ShipmentSlots({
  shipment,
  selectedSlot,
  chooseSlot,
  book,
  busy,
}: {
  shipment: Shipment;
  selectedSlot: DockSlot | null;
  chooseSlot: (slot: DockSlot | null) => void;
  book: (slot: DockSlot) => void;
  busy: boolean;
}) {
  const atRisk = shipment.exceptions.some((exception) => exception.code === "DOCK_RISK");
  return (
    <>
      <div className={`shipment-current-slot${atRisk ? " shipment-current-slot-risk" : ""}`}>
        <i className="bi bi-calendar2-week" aria-hidden="true" />
        <div>
          <strong>
            {shipment.bookedSlot
              ? `${shipment.bookedSlot.dock} · ${slotWindow(shipment.bookedSlot)}`
              : "No dock slot booked"}
          </strong>
          <p>
            {atRisk
              ? "The estimated arrival falls outside the current window."
              : (shipment.destination ?? "Destination unavailable")}
          </p>
        </div>
      </div>
      {shipment.availableSlots.length ? (
        <ul className="shipment-task-list">
          {shipment.availableSlots.map((slot, index) => (
            <li key={slot.id}>
              <div>
                <strong>
                  {slot.dock} · {slotWindow(slot)}
                </strong>
                <p>{index === 0 ? "First compatible window" : "Available window"}</p>
              </div>
              <button
                type="button"
                className="inventory-button"
                disabled={busy}
                onClick={() => chooseSlot(slot)}
                aria-label={`Choose ${slot.dock} at ${slotWindow(slot)}`}
              >
                Choose slot
              </button>
            </li>
          ))}
        </ul>
      ) : (
        <p className="shipment-muted">No compatible dock slots are currently available.</p>
      )}
      {selectedSlot && (
        <div className="shipment-slot-confirm" role="group" aria-label="Confirm dock booking">
          <p>
            Book{" "}
            <strong>
              {selectedSlot.dock} · {slotWindow(selectedSlot)}
            </strong>{" "}
            for {shipment.reference}?{shipment.bookedSlot && " This releases the current booking."}
          </p>
          <div className="inventory-actions">
            <button
              className="shipment-primary-button"
              type="button"
              disabled={busy}
              onClick={() => book(selectedSlot)}
            >
              Confirm booking
            </button>
            <button
              className="inventory-button"
              type="button"
              disabled={busy}
              onClick={() => chooseSlot(null)}
            >
              Cancel
            </button>
          </div>
        </div>
      )}
    </>
  );
}
