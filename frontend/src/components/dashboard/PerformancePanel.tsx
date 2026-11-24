import { number } from "../../siloInventory";
import { dashboardDates } from "./dashboardDates";
import type { DashboardSnapshot } from "../../types/dashboard";

const percent = new Intl.NumberFormat(undefined, { style: "percent", maximumFractionDigits: 1 });

export default function PerformancePanel({
  performance,
  siteId,
}: {
  performance: DashboardSnapshot["performance"];
  siteId: number | null;
}) {
  const stockTotals = (rows: DashboardSnapshot["performance"]["stock"]) =>
    rows
      .filter((row) => siteId === null || row.siteId === siteId)
      .reduce(
        (total, row) => ({
          hours: total.hours + row.observedHours,
          inRange: total.inRange + row.withinLimitsHours,
          stockout: total.stockout + row.stockoutHours,
        }),
        { hours: 0, inRange: 0, stockout: 0 },
      );
  const { hours, inRange, stockout } = stockTotals(performance.stock);
  const previous = stockTotals(performance.previousStock);
  const delivery = performance.deliveries.find((row) => row.siteId === siteId);
  const previousDelivery = performance.previousDeliveries.find((row) => row.siteId === siteId);
  const comparable = delivery?.comparableDeliveries ?? 0;
  const observationNote = hours
    ? `${number.format(hours)} silo-hours observed`
    : "No measured intervals in range";
  return (
    <section className="card dash-card dash-performance" aria-labelledby="dashboard-performance">
      <div className="card-header dash-panel-header">
        <div>
          <h2 className="card-title" id="dashboard-performance">
            Performance
          </h2>
          <p>
            {dashboardDates.format(new Date(performance.from))} –{" "}
            {dashboardDates.format(new Date(performance.through))}
          </p>
        </div>
        <span className="dash-count">30 days</span>
      </div>
      <dl className="dash-performance-grid">
        <div>
          <dt>Stock within limits</dt>
          <dd>{hours ? percent.format(inRange / hours) : "—"}</dd>
          <small>{observationNote}</small>
          {previous.hours > 0 && (
            <small>
              Previous 30 days · {percent.format(previous.inRange / previous.hours)} across{" "}
              {number.format(previous.hours)} silo-hours
            </small>
          )}
        </div>
        <div>
          <dt>Stockout hours</dt>
          <dd>{hours ? `${number.format(stockout)} h` : "—"}</dd>
          <small>{observationNote}</small>
          {previous.hours > 0 && (
            <small>
              Previous 30 days · {number.format(previous.stockout)} h across{" "}
              {number.format(previous.hours)} silo-hours
            </small>
          )}
        </div>
        <div>
          <dt>On-time deliveries</dt>
          <dd>
            {delivery && comparable ? percent.format(delivery.onTimeDeliveries / comparable) : "—"}
          </dd>
          <small>
            {delivery
              ? `${comparable} of ${delivery.deliveriesWithDate} dated deliveries comparable`
              : "No completed deliveries in range"}
          </small>
          {previousDelivery && previousDelivery.comparableDeliveries > 0 && (
            <small>
              Previous 30 days ·{" "}
              {percent.format(previousDelivery.onTimeDeliveries / previousDelivery.comparableDeliveries)}{" "}
              across {previousDelivery.comparableDeliveries} deliveries
            </small>
          )}
        </div>
      </dl>
      <p className="dash-panel-note">
        Stock estimates use the last level between readings up to 24 hours apart and today's limits.
        Delivery compares actual and promised calendar dates.
      </p>
    </section>
  );
}
