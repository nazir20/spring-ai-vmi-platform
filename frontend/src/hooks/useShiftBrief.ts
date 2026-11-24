import { useState } from "react";
import { requestSpring } from "../springApi";
import type { BriefResult } from "../types/dashboard";

export function useShiftBrief(siteId: number | null, revision: number) {
  const [draft, setDraft] = useState<{ siteId: number | null; revision: number; result: BriefResult }>();
  const [drafting, setDrafting] = useState(false);
  const brief = draft?.siteId === siteId && draft.revision === revision ? draft.result : undefined;

  async function draftBrief() {
    setDrafting(true);
    setDraft(undefined);
    try {
      const query = siteId === null ? "" : `?siteId=${siteId}`;
      const text = await requestSpring<string>(`/api/dashboard/brief${query}`, { method: "POST" });
      setDraft({ siteId, revision, result: { text } });
    } catch (error) {
      const message = error instanceof Error ? error.message : "Could not draft the brief.";
      setDraft({ siteId, revision, result: { error: message } });
    } finally {
      setDrafting(false);
    }
  }

  return { brief, drafting, draftBrief };
}
