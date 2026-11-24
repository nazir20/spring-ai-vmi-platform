import { useCallback, useEffect, useRef, useState } from "react";
import { useSearchParams } from "react-router-dom";
import PageHeader from "../components/layout/PageHeader";
import ShipmentBrief from "../components/shipments/ShipmentBrief";
import ShipmentDisruptions from "../components/shipments/ShipmentDisruptions";
import ShipmentExceptions from "../components/shipments/ShipmentExceptions";
import ShipmentJourney from "../components/shipments/ShipmentJourney";
import ShipmentNetworkMap from "../components/shipments/ShipmentNetworkMap";
import ShipmentQueue from "../components/shipments/ShipmentQueue";
import ShipmentSlots from "../components/shipments/ShipmentSlots";
import ShipmentWorkbench from "../components/shipments/ShipmentWorkbench";
import { useShipmentBoard } from "../hooks/useShipmentBoard";
import { shipmentWorkflows } from "../shipmentOperations";
import "../shipments.css";
import type { Concern, DockSlot, ShipmentWorkflow } from "../types/shipments";

export default function Shipments() {
  const [params] = useSearchParams();
  const operations = useShipmentBoard();
  const { board, refreshing, loadError, busy, notice, brief, clearNotice } = operations;
  const [selectedId, setSelectedId] = useState<number | undefined>(
    () => Number(params.get("shipment")) || undefined,
  );
  const [workflow, setWorkflow] = useState<ShipmentWorkflow>(
    () => shipmentWorkflows.find(({ id }) => id === params.get("workflow"))?.id ?? "journey",
  );
  const [selectedSlot, chooseSlot] = useState<DockSlot | null>(null);
  const [replayStep, setReplayStep] = useState<number | null>(null);
  const workbenchHeading = useRef<HTMLHeadingElement>(null);

  const shipments = board?.shipments ?? [];
  const selected =
    shipments.find((shipment) => shipment.id === selectedId) ??
    shipments.find((shipment) =>
      shipment.exceptions.some((item) => item.code === "DOCK_RISK" && !item.reviewed),
    ) ??
    shipments[0];
  const replayLength = selected?.tracking?.checkpoints.length ?? 0;
  const actionsDisabled = busy || refreshing;

  useEffect(() => {
    document.title = "Shipments | Orbit Logistics";
  }, []);

  useEffect(() => {
    if (replayStep === null) return;
    const timer = window.setTimeout(() => {
      const nextStep = replayStep + 1;
      setReplayStep(nextStep < replayLength ? nextStep : null);
    }, 850);
    return () => window.clearTimeout(timer);
  }, [replayStep, replayLength]);

  const selectShipment = useCallback(
    (id: number) => {
      setSelectedId(id);
      setReplayStep(null);
      chooseSlot(null);
      clearNotice();
    },
    [clearNotice],
  );

  function changeWorkflow(next: ShipmentWorkflow) {
    setWorkflow(next);
    chooseSlot(null);
    clearNotice();
  }

  function openWorkflow(next: ShipmentWorkflow) {
    changeWorkflow(next);
    workbenchHeading.current?.focus();
  }

  function refreshBoard() {
    chooseSlot(null);
    operations.refresh();
  }

  async function reviewException(code: Concern["code"]) {
    if (!selected) return;
    setSelectedId(selected.id);
    if (await operations.acknowledgeException(selected.id, code)) chooseSlot(null);
  }

  async function confirmSlot(slot: DockSlot) {
    if (!selected) return;
    setSelectedId(selected.id);
    if (await operations.bookSlot(selected.id, slot.id)) chooseSlot(null);
  }

  return (
    <div className="silo-inventory-view shipment-ops">
      <PageHeader
        title="Shipments"
        subtitle="Track inbound loads and coordinate arrivals."
        actions={
          <button
            className="inventory-button"
            type="button"
            disabled={actionsDisabled}
            onClick={refreshBoard}
          >
            <i className="bi bi-arrow-clockwise" aria-hidden="true" /> Refresh
          </button>
        }
      />
      {loadError && (
        <div className="alert alert-warning" role="alert">
          {loadError}
        </div>
      )}
      {!board && !loadError && <p role="status">Loading shipment operations…</p>}
      {board && !selected && <div className="inventory-panel">No shipment records are available.</div>}
      {board && selected && (
        <>
          <div className="shipment-network">
            <ShipmentNetworkMap
              board={board}
              selected={selected}
              replayStep={replayStep}
              onSelect={selectShipment}
              onToggleReplay={() => setReplayStep((step) => (step === null ? 0 : null))}
            />
            <ShipmentQueue
              shipments={board.shipments}
              selected={selected}
              onSelect={selectShipment}
              onOpenWorkflow={openWorkflow}
            />
          </div>
          <ShipmentWorkbench
            shipment={selected}
            workflow={workflow}
            headingRef={workbenchHeading}
            notice={notice}
            onWorkflowChange={changeWorkflow}
          >
            {workflow === "journey" && <ShipmentJourney shipment={selected} />}
            {workflow === "exceptions" && (
              <ShipmentExceptions
                shipment={selected}
                busy={actionsDisabled}
                review={(code) => {
                  void reviewException(code);
                }}
              />
            )}
            {workflow === "slots" && (
              <ShipmentSlots
                shipment={selected}
                selectedSlot={selectedSlot}
                busy={actionsDisabled}
                chooseSlot={chooseSlot}
                book={(slot) => {
                  void confirmSlot(slot);
                }}
              />
            )}
            {workflow === "disruptions" && (
              <ShipmentDisruptions
                shipments={board.shipments}
                onSelect={(id) => {
                  selectShipment(id);
                  changeWorkflow("exceptions");
                }}
              />
            )}
            {workflow === "brief" && (
              <ShipmentBrief
                shipment={selected}
                busy={busy}
                disabled={actionsDisabled}
                aiAvailable={board.aiAvailable}
                text={brief?.shipmentId === selected.id ? brief.text : undefined}
                onDraft={() => {
                  void operations.draftBrief(selected.id);
                }}
              />
            )}
          </ShipmentWorkbench>
        </>
      )}
    </div>
  );
}
