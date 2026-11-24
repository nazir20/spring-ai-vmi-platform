import { Link } from "react-router-dom";

import { formatDeliveryDate } from "./dashboardDates";
import type { DashboardScope, Filter } from "../../types/dashboard";

const alertFilters = [
  { key: "low", title: "Silos below minimum" },
  { key: "readings", title: "Readings to verify" },
  { key: "arrivals", title: "Arrivals at risk" },
  { key: "orders", title: "Replenishments to review" },
] as const;

export default function AttentionQueue({
  scope,
  filter,
  onFilter,
}: {
  scope: DashboardScope;
  filter: Filter;
  onFilter: (filter: Filter) => void;
}) {
  const visible =
    filter === "all"
      ? scope.attention
      : scope.attention.filter((item) => item.categories.includes(filter));
  return (
    <section className="card dash-card dash-attention" aria-labelledby="dashboard-attention">
      <div className="card-header dash-panel-header">
        <h2 className="card-title" id="dashboard-attention">
          Recent Alerts
        </h2>
        <select
          aria-label="Filter alerts"
          className="dash-select dash-alert-filter"
          value={filter}
          onChange={(event) => onFilter(event.target.value as Filter)}
        >
          <option value="all">All items ({scope.attention.length})</option>
          {alertFilters.map(({ key, title }) => (
            <option key={key} value={key}>
              {title} ({scope.counts[key]})
            </option>
          ))}
        </select>
      </div>
      {visible.length ? (
        <ol className="dash-attention-list" aria-label="Alerts and actions" tabIndex={0}>
          {visible.map((item) => (
            <li key={item.id}>
              <i
                className={`bi bi-${item.level === "info" ? "info-circle" : "exclamation-circle"} dash-level dash-${item.level}`}
                aria-hidden="true"
              />
              <div className="dash-issue-copy">
                <strong>{item.title}</strong>
                <p>{item.detail}</p>
                {item.secondary && (
                  <Link className="dash-inline-link" to={item.secondary.path}>
                    <i className="bi bi-truck" aria-hidden="true" /> {item.secondary.label}
                  </Link>
                )}
                {item.order && (
                  <details className="dash-order-review">
                    <summary>Review order details</summary>
                    <dl>
                      <div>
                        <dt>Supplier</dt>
                        <dd>{item.order.supplier}</dd>
                      </div>
                      <div>
                        <dt>Products</dt>
                        <dd>{item.order.products ?? "Unavailable"}</dd>
                      </div>
                      <div>
                        <dt>Requested delivery</dt>
                        <dd>
                          {item.order.expectedDate
                            ? formatDeliveryDate(item.order.expectedDate)
                            : "Unavailable"}
                        </dd>
                      </div>
                    </dl>
                  </details>
                )}
              </div>
              {item.primary && (
                <Link className="inventory-button" to={item.primary.path}>
                  {item.primary.label}
                  <i className="bi bi-arrow-up-right" aria-hidden="true" />
                </Link>
              )}
            </li>
          ))}
        </ol>
      ) : (
        <p className="dash-empty" role="status">
          No items match this view.
        </p>
      )}
    </section>
  );
}
