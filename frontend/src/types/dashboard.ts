import type { Silo } from "./inventory";
import type { Shipment, ShipmentBoard } from "./shipments";

export type ShiftBriefProps = {
  snapshotTime: string | null;
  aiAvailable: boolean;
  topItem?: Attention;
  brief?: { text?: string; error?: string };
  drafting: boolean;
  onDraft: () => Promise<void>;
};

type Alert = {
  id: number;
  alertType: string;
  severity: "INFO" | "WARNING" | "CRITICAL";
  title: string;
  message: string;
  siloId?: number;
  shipmentId?: number;
  purchaseOrderId?: number;
};

export type Replenishment = {
  id: number;
  reference: string;
  status: "DRAFT" | "SUBMITTED";
  supplier: string;
  products: string | null;
  expectedDate: string | null;
  siteIds: number[];
};

export type DashboardSnapshot = {
  silos: Silo[];
  logistics: ShipmentBoard;
  alerts: Alert[];
  replenishments: Replenishment[];
  performance: {
    from: string;
    through: string;
    stock: StockPerformance[];
    deliveries: DeliveryPerformance[];
    previousStock: StockPerformance[];
    previousDeliveries: DeliveryPerformance[];
  };
};

type StockPerformance = {
  siteId: number;
  observedHours: number;
  withinLimitsHours: number;
  stockoutHours: number;
};

type DeliveryPerformance = {
  siteId: number | null;
  deliveriesWithDate: number;
  comparableDeliveries: number;
  onTimeDeliveries: number;
};

export type Category = "low" | "readings" | "arrivals" | "orders";

export type Filter = Category | "all";

type AttentionLink = { label: string; path: string };

export type Attention = {
  id: string;
  title: string;
  detail: string;
  level: "urgent" | "review" | "info";
  priority: number;
  categories: Category[];
  primary?: AttentionLink;
  secondary?: AttentionLink;
  order?: Replenishment;
};

export type DashboardScope = {
  silos: Silo[];
  active: Silo[];
  arrivals: Shipment[];
  attention: Attention[];
  counts: Record<Category, number>;
};

export type BriefResult = { text: string; error?: never } | { text?: never; error: string };
