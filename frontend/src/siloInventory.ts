import type { Silo, SiloSite } from "./types/inventory";

export const condition = {
  LOW: { label: "Below minimum", color: "danger", symbol: "↓" },
  NORMAL: { label: "Within limits", color: "success", symbol: "✓" },
  HIGH: { label: "Above maximum", color: "warning", symbol: "↑" },
  UNKNOWN: { label: "Stock unknown", color: "secondary", symbol: "?" },
};
export const sources = { SYNTHETIC: "Demo data", MANUAL: "Manual", SENSOR: "Sensor", DELIVERY: "Delivery" };
export const freshness = { FRESH: "Fresh reading", STALE: "Stale reading", MISSING: "No reading" };
export const number = new Intl.NumberFormat(undefined, { maximumFractionDigits: 2 });
export const date = new Intl.DateTimeFormat(undefined, {
  dateStyle: "medium", timeStyle: "short", timeZone: "UTC",
});
export const siloPath = (silo: Silo) => `/inventory/silos/${silo.id}`;

export function groupMappedSites(silos: Silo[]) {
  const sites = new Map<number, SiloSite>();
  for (const silo of silos) {
    const { outstationLatitude: lat, outstationLongitude: lng } = silo;
    if (lat == null || lng == null || !Number.isFinite(lat) || !Number.isFinite(lng) ||
        Math.abs(lat) > 90 || Math.abs(lng) > 180) continue;
    const site = sites.get(silo.outstationId);
    if (site) {
      site.silos.push(silo);
    } else {
      sites.set(silo.outstationId, {
        id: silo.outstationId, name: silo.outstationName, city: silo.outstationCity,
        coordinates: [lat, lng], silos: [silo],
      });
    }
  }
  return [...sites.values()];
}
