import { useState } from "react";
import { transcriptPath } from "../../chatHistory";
import { useSpringRequest } from "../../hooks/useSpringRequest";
import ChatPanel from "../ChatPanel";
import type { ChatSummary, ChatTranscript } from "../../types/chat";

export default function SavedChat({
  id,
  onSaved,
}: {
  id: string;
  onSaved: (chat: ChatSummary) => void;
}) {
  const [revision, refresh] = useState(0);
  const [draft, setDraft] = useState("");
  const result = useSpringRequest<ChatTranscript>(transcriptPath(id), revision);
  if (result?.data)
    return (
      <ChatPanel
        saved={result.data}
        onSaved={onSaved}
        initialDraft={draft}
        onReload={(current) => {
          setDraft(current);
          refresh((currentRevision) => currentRevision + 1);
        }}
      />
    );
  return (
    <section className="chat-panel chat-panel-workspace assistant-placeholder">
      {result?.error ? (
        <p role="alert">{result.error}</p>
      ) : (
        <p role="status">Opening conversation…</p>
      )}
    </section>
  );
}
