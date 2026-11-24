import { shipmentRoute, shipmentState, shipmentWorkflows } from "../../shipmentOperations";
import type { ShipmentWorkbenchProps } from "../../types/shipments";

export default function ShipmentWorkbench({
  shipment,
  workflow,
  headingRef,
  notice,
  onWorkflowChange,
  children,
}: ShipmentWorkbenchProps) {
  return (
    <section className="inventory-panel shipment-workbench" aria-labelledby="shipment-selected">
      <div className="shipment-workbench-title">
        <div>
          <h2 id="shipment-selected" tabIndex={-1} ref={headingRef}>
            {shipment.reference}
          </h2>
          <p>{shipmentRoute(shipment)}</p>
        </div>
        <span>{shipmentState[shipment.status]}</span>
      </div>
      <nav className="shipment-workbench-modes" aria-label="Shipment workflows">
        {shipmentWorkflows.map(({ id, label }) => (
          <button
            type="button"
            key={id}
            aria-pressed={workflow === id}
            onClick={() => onWorkflowChange(id)}
          >
            {label}
          </button>
        ))}
      </nav>
      <div className="shipment-workbench-content">
        {notice && (
          <p className="shipment-notice" role="status">
            {notice}
          </p>
        )}
        {children}
      </div>
    </section>
  );
}
