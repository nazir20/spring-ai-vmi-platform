import { useId } from "react";

import { useChatConversation } from "../hooks/useChatConversation";

import ChatComposer from "./chat/ChatComposer";
import ChatLog from "./chat/ChatLog";
import "./chatPanel.css";
import type { ChatPanelProps } from "../types/chat";

export default function ChatPanel({
  silo,
  saved,
  onSaved,
  onReload,
  initialDraft = "",
}: ChatPanelProps) {
  const id = useId();
  const chat = useChatConversation({ siloId: silo?.id, saved, initialDraft, onSaved });
  const title = silo ? `Ask about ${silo.name}` : (saved?.conversation.title ?? "Orbit Assistant");
  const context = silo
    ? `${silo.siloCode} · Stock, readings, and inbound shipments`
    : "All sites · Read-only access to Orbit records";

  return (
    <section
      id={silo ? "silo-chat" : undefined}
      className={silo ? "chat-panel chat-panel-silo" : "chat-panel chat-panel-workspace"}
      aria-labelledby={`${id}-title`}
    >
      <header className="chat-header">
        <span className="chat-brand-icon">
          <i className="bi bi-chat-dots" aria-hidden="true" />
        </span>
        <div className="chat-title">
          <h2 id={`${id}-title`}>{title}</h2>
          <p>{context}</p>
        </div>
        {silo && (
          <button
            className="inventory-button chat-new"
            type="button"
            onClick={chat.reset}
            disabled={!!chat.pendingQuestion || (!chat.turns.length && !chat.error)}
          >
            <i className="bi bi-plus-lg" aria-hidden="true" /> New chat
          </button>
        )}
      </header>
      <ChatLog
        logRef={chat.logRef}
        isSiloChat={!!silo}
        turns={chat.turns}
        pendingQuestion={chat.pendingQuestion}
        hasEarlierMessages={chat.beforeTurn != null}
        loadingOlder={chat.loadingOlder}
        canUseSuggestions={chat.canType}
        onSuggestion={chat.applySuggestion}
        onLoadOlder={chat.loadOlder}
      />
      <ChatComposer
        messageId={`${id}-message`}
        inputRef={chat.inputRef}
        isSiloChat={!!silo}
        availability={chat.status}
        draft={chat.draft}
        sending={!!chat.pendingQuestion}
        canSend={chat.canType}
        error={chat.error}
        onDraftChange={chat.setDraft}
        onSend={chat.send}
        onReload={onReload}
      />
    </section>
  );
}
