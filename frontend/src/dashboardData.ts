import { dateTime, shipmentState } from "./shipmentOperations";
import { freshness, number, siloPath } from "./siloInventory";
import type { Attention, Category, DashboardScope, DashboardSnapshot } from "./types/dashboard";
import type { Silo } from "./types/inventory";
import type { Shipment } from "./types/shipments";

export const needsReading = (silo: Silo) => silo.readingFreshness !== "FRESH" || silo.stockCondition === "UNKNOWN";
export const shipmentHref = (shipment: Shipment, workflow = "journey") =>
  `/shipments?shipment=${shipment.id}&workflow=${workflow}`;

function shipmentSortTime(shipment: Shipment) {
  if (shipment.tracking?.etaAt) return new Date(shipment.tracking.etaAt).getTime();
  if (shipment.expectedDate) return new Date(`${shipment.expectedDate}T23:59:59Z`).getTime();
  return Infinity;
}

export function dashboardScope(data: DashboardSnapshot, siteId: number | null): DashboardScope {
  const silos = data.silos.filter((silo) => siteId === null || silo.outstationId === siteId);
  const active = silos.filter((silo) => silo.status === "ACTIVE");
  const siloIds = new Set(silos.map((silo) => silo.id));
  const arrivals = data.logistics.shipments
    .filter((shipment) => shipment.status !== "DELIVERED" && shipment.status !== "CANCELLED")
    .filter((shipment) => siteId === null || shipment.siloIds.some((id) => siloIds.has(id)))
    .sort((left, right) => shipmentSortTime(left) - shipmentSortTime(right) || left.reference.localeCompare(right.reference));
  const arrivalIds = new Set(arrivals.map((shipment) => shipment.id));
  const orders = data.replenishments.filter((order) => siteId === null || order.siteIds.includes(siteId));
  const orderIds = new Set(orders.map((order) => order.id));
  const attention: Attention[] = [];
  const siloIssues = new Set<number>();

  for (const silo of active) {
    const reading = needsReading(silo);
    const belowMin = silo.stockCondition === "LOW";
    const aboveMax = silo.stockCondition === "HIGH";
    if (!reading && !belowMin && !aboveMax) continue;
    const expected = arrivals.find((shipment) => shipment.siloIds.includes(silo.id)
      && shipment.tracking?.etaAt && shipment.status !== "PLANNED");
    const incoming = expected ?? arrivals.find((shipment) => shipment.siloIds.includes(silo.id));
    const quantity = (amount: number) => `${number.format(amount)} ${silo.unit}`;
    const shortage = belowMin ? `${quantity(silo.minimumStock - silo.currentStock)} below minimum` : undefined;
    const excess = aboveMax ? `${quantity(silo.currentStock - silo.maximumStock)} above maximum` : undefined;
    const empty = belowMin && !reading && silo.currentStock === 0;
    const detail = reading
      ? `${silo.stockCondition === "UNKNOWN" ? "Stock is unknown." : `${freshness[silo.readingFreshness]}. ${shortage ?? excess ?? "Verify the level before planning replenishment."}`}`
      : `${shortage ?? excess}. ${belowMin && incoming ? `${incoming.reference} · ${incoming.tracking?.etaAt
        ? `${shipmentState[incoming.status]} · ETA ${dateTime(incoming.tracking.etaAt)}` : shipmentState[incoming.status]}.` : belowMin
        ? "Review replenishment." : "Check planned deliveries."}`;
    const categories: Category[] = [];
    if (belowMin) categories.push("low");
    if (reading) categories.push("readings");
    const reason = empty ? "Out of stock" : reading ? "Verify reading" : belowMin ? "Below minimum" : "Above maximum";
    attention.push({
      id: `silo-${silo.id}`, title: `${silo.name} · ${reason}`, detail,
      level: belowMin && !reading ? "urgent" : "review",
      priority: empty ? 0 : belowMin && !reading ? 1 : reading && belowMin ? 2 : reading ? 4 : 5,
      categories, primary: { label: "View silo", path: siloPath(silo) },
      secondary: belowMin && incoming ? { label: incoming.reference, path: shipmentHref(incoming) } : undefined,
    });
    siloIssues.add(silo.id);
  }

  for (const shipment of arrivals) {
    const checks = shipment.exceptions.filter((exception) => !exception.reviewed);
    if (!checks.length) continue;
    const dockRisk = checks.find((issue) => issue.code === "DOCK_RISK");
    const lead = dockRisk ?? checks[0];
    attention.push({
      id: `shipment-${shipment.id}`, title: `${shipment.reference} · ${lead.title}`,
      detail: `${lead.evidence} ${lead.recommendation}`,
      priority: dockRisk ? 2 : 3, level: dockRisk ? "urgent" : "review", categories: ["arrivals"],
      primary: { label: dockRisk ? "Review dock slot" : "Review shipment", path: shipmentHref(shipment, dockRisk ? "slots" : "exceptions") },
    });
  }

  for (const alert of data.alerts) {
    if (siteId !== null && !siloIds.has(alert.siloId ?? -1) && !arrivalIds.has(alert.shipmentId ?? -1)
        && !orderIds.has(alert.purchaseOrderId ?? -1)) continue;
    const silo = silos.find((item) => item.id === alert.siloId);
    const shipment = arrivals.find((item) => item.id === alert.shipmentId);
    if (silo && siloIssues.has(silo.id) && ["LOW_STOCK", "HIGH_STOCK"].includes(alert.alertType)) continue;
    if (shipment && ["UPCOMING_DELIVERY", "SHIPMENT_DELAYED"].includes(alert.alertType)) continue;
    attention.push({
      id: `alert-${alert.id}`, title: alert.title, detail: alert.message,
      level: alert.severity === "CRITICAL" ? "urgent" : alert.severity === "WARNING" ? "review" : "info",
      priority: alert.severity === "CRITICAL" ? 1 : 6, categories: [],
      primary: silo ? { label: "View silo", path: siloPath(silo) }
        : shipment ? { label: "View shipment", path: shipmentHref(shipment, "exceptions") } : undefined,
    });
  }

  for (const order of orders) {
    attention.push({
      id: `order-${order.id}`, title: `${order.reference} · ${order.status === "DRAFT" ? "Draft replenishment" : "Awaiting confirmation"}`,
      detail: [order.products, order.supplier, order.siteIds.length ? null : "Site unassigned"].filter(Boolean).join(" · "),
      level: "info", priority: 7, categories: ["orders"], order,
    });
  }

  return {
    silos,
    active,
    arrivals,
    attention: attention.sort((a, b) => a.priority - b.priority || a.title.localeCompare(b.title)),
    counts: {
      low: active.filter((silo) => silo.stockCondition === "LOW").length,
      readings: active.filter(needsReading).length,
      arrivals: arrivals.filter((shipment) => shipment.exceptions.some((issue) => !issue.reviewed)).length,
      orders: orders.length,
    },
  };
}
