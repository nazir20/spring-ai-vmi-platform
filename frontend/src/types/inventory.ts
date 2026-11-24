export type SiloMapProps = { sites: SiloSite[]; focusSiloId?: number; preview?: boolean };

export type BasemapProperties = {
  kind: "germany" | "country" | "state" | "river" | "city";
  name?: string;
};

export type StockCondition = "LOW" | "NORMAL" | "HIGH" | "UNKNOWN";

export type Silo = {
  id: number;
  siloCode: string;
  name: string;
  description?: string;
  outstationId: number;
  outstationName: string;
  outstationCity?: string;
  outstationLatitude?: number;
  outstationLongitude?: number;
  productId: number;
  productName: string;
  status: "ACTIVE" | "INACTIVE";
  capacity: number;
  minimumStock: number;
  maximumStock: number;
  currentStock: number;
  unit: string;
  fillPercentage?: number;
  stockCondition: StockCondition;
  readingFreshness: "FRESH" | "STALE" | "MISSING";
  stockSource?: "SYNTHETIC" | "MANUAL" | "SENSOR" | "DELIVERY";
  stockObservedAt?: string;
  lastMeasuredAt?: string;
  demoAsOf?: string;
};

export type SiloSite = {
  id: number;
  name: string;
  city?: string;
  coordinates: [number, number];
  silos: Silo[];
};
