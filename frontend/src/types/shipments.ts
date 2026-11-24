import type { ReactNode, RefObject } from "react";
import type { shipmentWorkflows } from "../shipmentOperations";

export type ShipmentMapProps = {
  shipments: Shipment[];
  selectedId?: number;
  onSelect: (id: number) => void;
  replayStep: number | null;
};

export type ShipmentBriefProps = {
  shipment: Shipment;
  aiAvailable: boolean;
  busy: boolean;
  disabled: boolean;
  text?: string;
  onDraft: () => void;
};

export type ShipmentNetworkMapProps = {
  board: ShipmentBoard;
  selected: Shipment;
  replayStep: number | null;
  onToggleReplay: () => void;
  onSelect: (id: number) => void;
};

export type ShipmentWorkbenchProps = {
  shipment: Shipment;
  workflow: ShipmentWorkflow;
  headingRef: RefObject<HTMLHeadingElement | null>;
  notice: string;
  onWorkflowChange: (workflow: ShipmentWorkflow) => void;
  children: ReactNode;
};

export type Checkpoint = { lat: number; lng: number; label: string };

export type ShipmentState = "PLANNED" | "DISPATCHED" | "IN_TRANSIT" | "DELIVERED" | "CANCELLED";

export type DockSlot = { id: number; dock: string; startsAt: string; endsAt: string };

export type Concern = {
  code: "LATE_ETA" | "SIGNAL" | "DISRUPTION" | "DOCK_RISK";
  title: string;
  evidence: string;
  recommendation: string;
  reviewed: boolean;
};

export type Shipment = {
  id: number;
  reference: string;
  orderNumber: string;
  status: ShipmentState;
  carrier: string | null;
  expectedDate: string | null;
  destination: string | null;
  silos: string | null;
  siloIds: number[];
  quantity: number | null;
  unit: string | null;
  tracking: {
    origin: string;
    source: "SIMULATED" | "CARRIER" | "MANUAL";
    checkpoints: Checkpoint[];
    lastCheckpoint: number | null;
    lastUpdateAt: string | null;
    promisedAt: string | null;
    etaAt: string | null;
  } | null;
  disruption: {
    lat: number;
    lng: number;
    code: string;
    title: string;
    detail: string;
    reportedAt: string;
  } | null;
  bookedSlot: DockSlot | null;
  availableSlots: DockSlot[];
  exceptions: Concern[];
};

export type ShipmentBoard = { shipments: Shipment[]; demoAsOf: string | null; aiAvailable: boolean };

export type ShipmentWorkflow = (typeof shipmentWorkflows)[number]["id"];
