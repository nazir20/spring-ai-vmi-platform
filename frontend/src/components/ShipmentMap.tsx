import { useCallback, useEffect, useRef } from "react";
import L from "leaflet";
import { addGermanyBasemap } from "../logisticsBasemap";
import type { Checkpoint, ShipmentMapProps } from "../types/shipments";

const position = (point: Checkpoint): L.LatLngTuple => [point.lat, point.lng];
const worldBounds = L.latLngBounds([-84.8, -180], [84.8, 180]);

function mapIcon(type: "truck" | "end" | "issue" | "origin", label: string) {
  const icon = document.createElement("span");
  icon.className = `shipment-map-symbol shipment-map-symbol-${type}`;
  const symbols = { truck: "truck-front", end: "geo-alt-fill", issue: "exclamation-triangle-fill", origin: "circle-fill" };
  const glyph = document.createElement("i");
  glyph.className = `bi bi-${symbols[type]}`;
  glyph.setAttribute("aria-hidden", "true");
  icon.append(glyph);
  icon.title = label;
  const anchor: L.PointExpression = type === "issue" ? [17, 44] : [17, 17];
  return L.divIcon({ html: icon, className: "shipment-map-icon", iconSize: [34, 34], iconAnchor: anchor, tooltipAnchor: [0, -16] });
}

function routeTooltip(label: string) {
  const text = document.createElement("span");
  text.textContent = label;
  return text;
}

export default function ShipmentMap({ shipments, selectedId, onSelect, replayStep }: ShipmentMapProps) {
  const surface = useRef<HTMLDivElement>(null);
  const mapRef = useRef<L.Map | null>(null);
  const boundsRef = useRef<L.LatLngBounds | null>(null);
  const overview = useRef(true);
  const fitting = useRef(false);
  const selectedTruck = useRef<{ marker: L.Marker; layer: L.LayerGroup } | null>(null);

  const fitOverview = useCallback(() => {
    const map = mapRef.current;
    if (!map || !surface.current?.clientWidth || !surface.current.clientHeight) return;
    overview.current = true;
    fitting.current = true;
    try {
      map.invalidateSize({ debounceMoveend: true, animate: false });
      if (boundsRef.current) {
        map.fitBounds(boundsRef.current, { maxZoom: 8, padding: [28, 28], animate: false });
      }
    } finally {
      fitting.current = false;
    }
  }, []);

  useEffect(() => {
    const element = surface.current;
    if (!element) return;
    const map = L.map(element, {
      zoomSnap: .25, minZoom: 4, maxZoom: 12, scrollWheelZoom: true, trackResize: false,
      maxBounds: worldBounds, maxBoundsViscosity: 1,
    });
    map.setView([51.2, 10.2], 5.5);
    mapRef.current = map;
    addGermanyBasemap(map);
    map.on("dragstart zoomstart", () => { if (!fitting.current) overview.current = false; });
    let frame = 0;
    const observer = new ResizeObserver(() => {
      cancelAnimationFrame(frame);
      frame = requestAnimationFrame(() => {
        if (!element.clientWidth || !element.clientHeight) return;
        if (overview.current) fitOverview();
        else map.invalidateSize({ debounceMoveend: true, animate: false });
      });
    });
    observer.observe(element);
    return () => {
      cancelAnimationFrame(frame);
      observer.disconnect();
      map.remove();
      mapRef.current = null;
      boundsRef.current = null;
      overview.current = true;
    };
  }, [fitOverview]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map) return;
    const layer = L.layerGroup().addTo(map);
    const bounds = L.latLngBounds([]);
    const plottedDisruptions = new Set<string>();
    const ordered = [...shipments].sort((a, b) => Number(a.id === selectedId) - Number(b.id === selectedId));
    for (const shipment of ordered) {
      const tracking = shipment.tracking;
      if (!tracking?.checkpoints.length) continue;
      const selected = shipment.id === selectedId;
      const points = tracking.checkpoints;
      const hasConcern = shipment.exceptions.length > 0;
      const coordinates = points.map(position);
      coordinates.forEach((point) => bounds.extend(point));
      const route = L.polyline(coordinates, {
        className: `shipment-map-route${selected ? " route-selected" : ""}${hasConcern ? " route-concern" : ""}`,
        weight: selected ? 5 : 3, lineCap: "round",
        dashArray: tracking.source === "SIMULATED" || shipment.status === "PLANNED" ? "8 7" : undefined,
      }).addTo(layer);
      route.bindTooltip(routeTooltip(`${shipment.reference} · ${tracking.origin} → ${shipment.destination ?? "Destination unavailable"}`));
      route.on("click", () => onSelect(shipment.id));

      const lastCheckpoint = tracking.lastCheckpoint == null ? undefined : points[tracking.lastCheckpoint];
      if (selected || lastCheckpoint) {
        const checkpoint = lastCheckpoint ?? points[0];
        const label = `${shipment.reference} · Last checkpoint: ${checkpoint.label}`;
        const truck = L.marker(position(checkpoint), { icon: mapIcon("truck", label), zIndexOffset: selected ? 600 : 300 });
        truck.bindTooltip(routeTooltip(label), { direction: "top" }).on("click", () => onSelect(shipment.id));
        if (lastCheckpoint) {
          layer.addLayer(truck);
          truck.getElement()?.setAttribute("aria-label", label);
        }
        if (selected) selectedTruck.current = { marker: truck, layer };
      }
      if (selected) {
        const start = points[0];
        const end = points[points.length - 1];
        L.marker(position(start), { icon: mapIcon("origin", start.label), keyboard: false, interactive: false }).addTo(layer);
        L.marker(position(end), { icon: mapIcon("end", end.label), keyboard: false, interactive: false }).addTo(layer);
      }
      const issue = shipment.disruption;
      if (issue && !plottedDisruptions.has(issue.code)) {
        plottedDisruptions.add(issue.code);
        const marker = L.marker([issue.lat, issue.lng], { icon: mapIcon("issue", issue.title), zIndexOffset: 650 }).addTo(layer);
        marker.getElement()?.setAttribute("aria-label", issue.title);
        marker.bindTooltip(routeTooltip(issue.title), { direction: "top" }).on("click", () => onSelect(shipment.id));
      }
    }
    if (bounds.isValid()) {
      const padded = bounds.pad(.16);
      const changed = !boundsRef.current?.equals(padded);
      boundsRef.current = padded;
      if (changed && overview.current) fitOverview();
    } else {
      boundsRef.current = null;
    }
    return () => { selectedTruck.current = null; layer.remove(); };
  }, [shipments, selectedId, onSelect, fitOverview]);

  useEffect(() => {
    const current = selectedTruck.current;
    const shipment = shipments.find((item) => item.id === selectedId);
    const tracking = shipment?.tracking;
    if (!current || !shipment || !tracking) return;
    const index = replayStep ?? tracking.lastCheckpoint;
    const checkpoint = index == null ? undefined : tracking.checkpoints[index];
    if (!checkpoint) {
      current.layer.removeLayer(current.marker);
      return;
    }
    const label = `${shipment.reference} · ${replayStep !== null ? "Replay" : "Last checkpoint"}: ${checkpoint.label}`;
    current.marker.setLatLng(position(checkpoint)).setIcon(mapIcon("truck", label)).setTooltipContent(routeTooltip(label));
    if (!current.layer.hasLayer(current.marker)) current.layer.addLayer(current.marker);
    current.marker.getElement()?.setAttribute("aria-label", label);
  }, [shipments, selectedId, replayStep]);

  return (
    <div className="silo-map-frame shipment-map-frame">
      <div className="silo-map-surface shipment-map-surface" ref={surface} role="region" aria-label="Shipment route map" />
      {shipments.some((item) => item.tracking?.checkpoints.length) &&
        <button className="inventory-button shipment-map-fit" type="button" onClick={fitOverview}>
          <i className="bi bi-arrows-fullscreen" aria-hidden="true" /> Fit routes
        </button>}
    </div>
  );
}
