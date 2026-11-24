import { useMemo } from "react";
import { Link } from "react-router-dom";
import { groupMappedSites } from "../../siloInventory";
import SiloMap from "../SiloMap";
import type { Silo } from "../../types/inventory";

export default function SiloLocation({ silo }: { silo: Silo }) {
  const sites = useMemo(() => groupMappedSites([silo]), [silo]);

  return (
    <section className="inventory-panel inventory-location" aria-labelledby="location-title">
      <div className="inventory-panel-header">
        <div>
          <h2 id="location-title">Location</h2>
          <p>
            {silo.outstationName}
            {silo.outstationCity ? ` · ${silo.outstationCity}` : ""}
          </p>
        </div>
      </div>
      <Link className="silo-map-preview-link" to={`/inventory/map?silo=${silo.id}`}>
        {sites.length ? (
          <SiloMap sites={sites} focusSiloId={silo.id} preview />
        ) : (
          <span className="inventory-location-empty">No coordinates saved for this site.</span>
        )}
        <span className="silo-map-preview-action">
          <i className="bi bi-map" aria-hidden="true" /> Open full map{" "}
          <i className="bi bi-arrow-up-right" aria-hidden="true" />
        </span>
      </Link>
    </section>
  );
}
