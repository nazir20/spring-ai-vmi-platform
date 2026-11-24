import L from "leaflet";
import type { FeatureCollection, Geometry } from "geojson";
import "leaflet/dist/leaflet.css";
import basemapSource from "./assets/germany-basemap.geojson?raw";
import type { BasemapProperties } from "./types/inventory";

const geography: FeatureCollection<Geometry, BasemapProperties> = JSON.parse(basemapSource);

export function addGermanyBasemap(map: L.Map) {
  const terrain = map.createPane("logistics-basemap");
  terrain.style.zIndex = "250";
  terrain.style.pointerEvents = "none";
  L.geoJSON<BasemapProperties>(geography, {
    pane: "logistics-basemap",
    interactive: false,
    style: (feature) => ({
      className: `silo-geo-${feature?.properties.kind ?? "country"}`, interactive: false,
      weight: 1, fillOpacity: 1,
    }),
    pointToLayer: (feature, point) => {
      const label = document.createElement("span");
      label.className = "silo-map-city";
      label.textContent = feature.properties.name ?? "";
      return L.marker(point, {
        icon: L.divIcon({ html: label, className: "silo-map-label", iconSize: [0, 0], iconAnchor: [0, 0] }),
        interactive: false, keyboard: false, zIndexOffset: -1000,
      });
    },
  }).addTo(map);
  map.attributionControl?.setPrefix(false);
  map.attributionControl?.addAttribution('<a href="https://www.naturalearthdata.com/" target="_blank" rel="noopener noreferrer">Natural Earth</a>');
}
