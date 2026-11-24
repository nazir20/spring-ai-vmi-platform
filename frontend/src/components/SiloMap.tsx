import { useEffect, useRef } from "react";
import { useNavigate } from "react-router-dom";
import L from "leaflet";
import { addGermanyBasemap } from "../logisticsBasemap";
import { condition, freshness, number, siloPath } from "../siloInventory";
import type { SiloMapProps, SiloSite, StockCondition } from "../types/inventory";

const germanyBounds = L.latLngBounds([47.15, 5.8], [55.05, 15.1]);
const order: StockCondition[] = ["LOW", "HIGH", "UNKNOWN", "NORMAL"];

function node(tag: string, className: string, text?: string) {
  const element = document.createElement(tag);
  element.className = className;
  if (text !== undefined) element.textContent = text;
  return element;
}

function siteIcon(site: SiloSite, selected: boolean, preview: boolean) {
  const statuses = order.filter((status) => site.silos.some((silo) => silo.stockCondition === status));
  const pin = node("span", `map-pin stock-${statuses[0].toLowerCase()}${selected ? " map-pin-selected" : ""}`);
  pin.append(preview
    ? node("i", "bi bi-database")
    : node("span", "map-pin-count", String(site.silos.length)));
  const strip = node("span", "map-pin-strip");
  for (const status of statuses) {
    const indicator = node("span", `stock-${status.toLowerCase()}`, condition[status].symbol);
    indicator.title = condition[status].label;
    strip.append(indicator);
  }
  pin.append(strip);
  if (preview && site.city) pin.append(node("span", "map-pin-city", site.city));
  return L.divIcon({ html: pin, className: "silo-map-marker", iconSize: [36, 40], iconAnchor: [18, 36],
    popupAnchor: [0, -37], tooltipAnchor: [0, -37] });
}

function siteDetails(site: SiloSite, openSilo?: (path: string) => void) {
  const content = node("div", "silo-map-site-details");
  content.append(node("strong", "silo-map-site-title", site.name));
  if (site.city) content.append(node("div", "silo-map-site-city", site.city));
  const list = node("ul", "silo-map-site-silos");
  for (const silo of site.silos) {
    const item = node("li", "");
    const title = node(openSilo ? "a" : "span", "silo-map-silo-name", silo.name);
    if (openSilo && title instanceof HTMLAnchorElement) {
      const path = siloPath(silo);
      title.href = path;
      title.addEventListener("click", (event) => {
        if (event.button !== 0 || event.ctrlKey || event.metaKey || event.shiftKey || event.altKey) return;
        event.preventDefault();
        openSilo(path);
      });
    }
    const status = condition[silo.stockCondition];
    const fill = silo.fillPercentage === undefined ? "" : ` · ${number.format(silo.fillPercentage)}%`;
    const lifecycle = silo.status === "INACTIVE" ? " · Inactive" : "";
    const age = silo.readingFreshness === "FRESH" ? "" : ` · ${freshness[silo.readingFreshness]}`;
    item.append(title, node("small", `stock-${silo.stockCondition.toLowerCase()}`,
      `${status.label}${fill}${age}${lifecycle}`));
    list.append(item);
  }
  content.append(list);
  return content;
}

export default function SiloMap({ sites, focusSiloId, preview = false }: SiloMapProps) {
  const surface = useRef<HTMLDivElement>(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!surface.current) return;
    const map = L.map(surface.current, {
      zoomControl: !preview, attributionControl: !preview, scrollWheelZoom: !preview,
      dragging: !preview, keyboard: !preview, doubleClickZoom: !preview,
      touchZoom: !preview, boxZoom: !preview, zoomSnap: 0.25, minZoom: 4, maxZoom: 11,
    });
    const focusSite = sites.find((site) => site.silos.some((silo) => silo.id === focusSiloId));
    if (focusSite) {
      map.setView(focusSite.coordinates, preview ? 8.25 : 8.75);
    } else {
      const bounds = L.latLngBounds(germanyBounds.getSouthWest(), germanyBounds.getNorthEast());
      sites.forEach((site) => bounds.extend(site.coordinates));
      map.fitBounds(bounds, { padding: [16, 16], animate: false });
    }
    addGermanyBasemap(map);

    for (const site of sites) {
      const marker = L.marker(site.coordinates, {
        icon: siteIcon(site, site === focusSite, preview), keyboard: !preview, interactive: !preview,
      }).addTo(map);
      if (!preview) {
        const statuses = site.silos.map((silo) => `${silo.name}, ${condition[silo.stockCondition].label}`);
        marker.getElement()?.setAttribute("aria-label", `${site.name}: ${statuses.join("; ")}`);
        marker.bindTooltip(siteDetails(site), { direction: "top", opacity: 1, className: "silo-map-tooltip" });
        marker.bindPopup(siteDetails(site, navigate), { maxWidth: 245, minWidth: 175 });
        marker.on("popupopen", () => marker.closeTooltip());
        if (site === focusSite) marker.openPopup();
      }
    }

    const observer = new ResizeObserver(() => map.invalidateSize({ debounceMoveend: true }));
    observer.observe(surface.current);
    return () => {
      observer.disconnect();
      map.remove();
    };
  }, [sites, focusSiloId, preview, navigate]);

  return (
    <div className={`silo-map-frame${preview ? " silo-map-frame-mini" : ""}`} aria-hidden={preview || undefined}>
      <div className="silo-map-surface" ref={surface} role={preview ? undefined : "region"}
        aria-label={preview ? undefined : "Interactive map of silo sites in Germany"} />
    </div>
  );
}
