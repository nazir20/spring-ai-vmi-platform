import { useCallback, useEffect, useState } from "react";
import { operationsPath } from "../shipmentOperations";
import { requestSpring } from "../springApi";
import type { Concern, ShipmentBoard } from "../types/shipments";

export function useShipmentBoard() {
  const [board, setBoard] = useState<ShipmentBoard>();
  const [revision, setRevision] = useState(0);
  const [refreshing, setRefreshing] = useState(false);
  const [loadError, setLoadError] = useState("");
  const [busy, setBusy] = useState(false);
  const [notice, setNotice] = useState("");
  const [brief, setBrief] = useState<{ shipmentId: number; text: string }>();

  useEffect(() => {
    const controller = new AbortController();
    requestSpring<ShipmentBoard>(operationsPath, { signal: controller.signal })
      .then((value) => {
        if (!controller.signal.aborted) setBoard(value);
      })
      .catch((error: Error) => {
        if (!controller.signal.aborted) setLoadError(error.message);
      })
      .finally(() => {
        if (!controller.signal.aborted) setRefreshing(false);
      });
    return () => controller.abort();
  }, [revision]);

  const clearNotice = useCallback(() => setNotice(""), []);

  function refresh() {
    setRefreshing(true);
    setLoadError("");
    setNotice("");
    setBrief(undefined);
    setRevision((current) => current + 1);
  }

  async function saveShipment(path: string, successMessage: string) {
    setBusy(true);
    setLoadError("");
    setNotice("");
    try {
      const value = await requestSpring<ShipmentBoard>(operationsPath + path, { method: "POST" });
      setBoard(value);
      setBrief(undefined);
      setNotice(successMessage);
      return true;
    } catch (error) {
      setNotice(error instanceof Error ? error.message : "Could not save the change.");
      return false;
    } finally {
      setBusy(false);
    }
  }

  function acknowledgeException(shipmentId: number, code: Concern["code"]) {
    return saveShipment(`/${shipmentId}/exceptions/${code}/review`, "Exception acknowledged.");
  }

  function bookSlot(shipmentId: number, slotId: number) {
    return saveShipment(`/${shipmentId}/slot/${slotId}`, "Dock slot booked.");
  }

  async function draftBrief(shipmentId: number) {
    setBusy(true);
    setBrief(undefined);
    setNotice("");
    try {
      const text = await requestSpring<string>(`${operationsPath}/${shipmentId}/brief`, {
        method: "POST",
      });
      setBrief({ shipmentId, text });
    } catch (error) {
      setNotice(error instanceof Error ? error.message : "Could not generate the briefing.");
    } finally {
      setBusy(false);
    }
  }

  return {
    board,
    refreshing,
    loadError,
    busy,
    notice,
    brief,
    clearNotice,
    refresh,
    acknowledgeException,
    bookSlot,
    draftBrief,
  };
}
