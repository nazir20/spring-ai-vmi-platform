import type { DockSlot, Shipment, ShipmentState } from "./types/shipments";

export const operationsPath = "/api/shipments/operations";
export const shipmentState: Record<ShipmentState, string> = {
  PLANNED: "Planned", DISPATCHED: "Dispatched", IN_TRANSIT: "In transit",
  DELIVERED: "Delivered", CANCELLED: "Cancelled",
};

const timeFormat = new Intl.DateTimeFormat("en-GB", {
  timeZone: "Europe/Berlin", hour: "2-digit", minute: "2-digit",
});
const dateTimeFormat = new Intl.DateTimeFormat("en-GB", {
  timeZone: "Europe/Berlin", day: "numeric", month: "short", hour: "2-digit", minute: "2-digit",
});
const dayFormat = new Intl.DateTimeFormat("en-CA", { timeZone: "Europe/Berlin", dateStyle: "short" });
export const time = (instant: string) => timeFormat.format(new Date(instant));
export const dateTime = (instant: string) => dateTimeFormat.format(new Date(instant));
export function slotWindow(slot: DockSlot) {
  const sameDay = dayFormat.format(new Date(slot.startsAt)) === dayFormat.format(new Date(slot.endsAt));
  return `${dateTime(slot.startsAt)}–${sameDay ? time(slot.endsAt) : dateTime(slot.endsAt)}`;
}

export const shipmentWorkflows = [
  { id: "journey", label: "Journey" },
  { id: "exceptions", label: "Exceptions" },
  { id: "slots", label: "Dock slots" },
  { id: "disruptions", label: "Disruptions" },
  { id: "brief", label: "Dispatch brief" },
] as const;

export function shipmentTime(value?: string | null) {
  return value ? dateTime(value) : "Unavailable";
}

export function shipmentRoute(shipment: Shipment) {
  const origin = shipment.tracking?.origin ?? "Origin unavailable";
  const destination = shipment.destination ?? "Destination unavailable";
  return origin + " → " + destination;
}
