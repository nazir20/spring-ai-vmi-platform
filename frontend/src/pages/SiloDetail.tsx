import { useEffect } from "react";
import { Link, useParams } from "react-router-dom";
import ChatPanel from "../components/ChatPanel";
import PageHeader from "../components/layout/PageHeader";
import SiloFillGraphic from "../components/silos/SiloFillGraphic";
import SiloLocation from "../components/silos/SiloLocation";
import SiloStockDetails from "../components/silos/SiloStockDetails";
import { useSpringRequest } from "../hooks/useSpringRequest";
import type { Silo } from "../types/inventory";

export default function SiloDetail() {
  const { siloId } = useParams();
  const result = useSpringRequest<Silo>(`/api/silos/${encodeURIComponent(siloId ?? "")}`);
  const silo = result?.data;

  useEffect(() => {
    document.title = `${silo?.name ?? "Silo"} | Orbit Logistics`;
  }, [silo]);

  return (
    <div className="silo-inventory-view">
      <Link className="inventory-back" to="/inventory/silos">
        <i className="bi bi-arrow-left" aria-hidden="true" /> Silos &amp; Tanks
      </Link>
      <PageHeader
        title={silo?.name ?? "Silo details"}
        subtitle={silo ? [silo.siloCode, silo.outstationName].join(" · ") : undefined}
        actions={
          silo && (
            <a className="inventory-button" href="#silo-chat">
              <i className="bi bi-chat-dots" aria-hidden="true" /> Ask about this silo
            </a>
          )
        }
      />
      {result?.error && (
        <div className="alert alert-warning" role="alert">
          {result.error}
        </div>
      )}
      {!result && <p role="status">Loading silo…</p>}
      {silo && (
        <div className="silo-detail-grid">
          <SiloFillGraphic silo={silo} />
          <div className="silo-detail-information">
            <SiloStockDetails silo={silo} />
            <SiloLocation silo={silo} />
          </div>
        </div>
      )}
      {silo && <ChatPanel key={silo.id} silo={silo} />}
    </div>
  );
}
