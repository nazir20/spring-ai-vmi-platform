import { Link } from "react-router-dom";

import { dateTime } from "../../shipmentOperations";
import type { ShiftBriefProps } from "../../types/dashboard";

export default function ShiftBrief({
  snapshotTime,
  aiAvailable,
  topItem,
  brief,
  drafting,
  onDraft,
}: ShiftBriefProps) {
  return (
    <section className="card dash-card dash-brief" aria-labelledby="dashboard-brief">
      <div className="card-header dash-panel-header">
        <h2 className="card-title" id="dashboard-brief">
          Shift brief
        </h2>
        {snapshotTime && (
          <span className="dash-count">Demo snapshot · {dateTime(snapshotTime)} · Berlin time</span>
        )}
      </div>
      <div className="dash-brief-body">
        <strong>{topItem?.title ?? "No items need attention"}</strong>
        <p>
          {topItem?.detail ?? "No stock, reading, or logistics exceptions were found for this view."}
        </p>
        {topItem?.primary && (
          <Link className="dash-inline-link" to={topItem.primary.path}>
            {topItem.primary.label}
            <i className="bi bi-arrow-up-right" aria-hidden="true" />
          </Link>
        )}
      </div>
      {aiAvailable && (
        <div className="dash-ai">
          <button
            type="button"
            className="inventory-button"
            disabled={drafting}
            onClick={() => {
              void onDraft();
            }}
          >
            <i className="bi bi-stars" aria-hidden="true" /> {drafting ? "Drafting…" : "Draft with AI"}
          </button>
          {brief?.text && (
            <p role="status">
              <strong>AI draft</strong> · {brief.text}
            </p>
          )}
          {brief?.error && <p role="alert">{brief.error}</p>}
        </div>
      )}
    </section>
  );
}
