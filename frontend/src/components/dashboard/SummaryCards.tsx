import { number } from "../../siloInventory";
import type { DashboardScope } from "../../types/dashboard";

export default function SummaryCards({ scope }: { scope?: DashboardScope | null }) {
  const cards = [
    {
      title: "Total Products",
      value: scope && new Set(scope.silos.map((silo) => silo.productId)).size,
      note: "Assigned to silos",
    },
    {
      title: "Active Shipments",
      value: scope?.arrivals.filter((shipment) => shipment.status !== "PLANNED").length,
    },
    { title: "Low Stock Alerts", value: scope?.counts.low },
    { title: "Pending Orders", value: scope?.counts.orders },
  ];
  return (
    <section className="dash-summary mb-4" aria-label="Dashboard summary">
      {cards.map(({ title, value, note }) => (
        <div className="card dash-card" key={title}>
          <div className="card-body">
            <h2>{title}</h2>
            <p className="h3 mb-0">{value == null ? "—" : number.format(value)}</p>
            {note && <small>{note}</small>}
          </div>
        </div>
      ))}
    </section>
  );
}
