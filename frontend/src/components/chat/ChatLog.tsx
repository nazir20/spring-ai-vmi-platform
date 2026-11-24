import type { ChatLogProps } from "../../types/chat";

const generalQuestions = [
  "Which silos are below minimum?",
  "Which stock readings need checking?",
  "What inventory alerts are open?",
];
const siloQuestions = [
  "Does this silo need attention?",
  "When is the next shipment for this silo?",
  "What do the latest readings show?",
];

function ChatMessage({ role, message }: { role: "user" | "assistant"; message: string }) {
  return (
    <div className={"chat-message chat-message-" + role}>
      <span>{role === "user" ? "You" : "Orbit Assistant"}</span>
      <p>{message}</p>
    </div>
  );
}

export default function ChatLog({
  logRef,
  isSiloChat,
  turns,
  pendingQuestion,
  hasEarlierMessages,
  loadingOlder,
  canUseSuggestions,
  onSuggestion,
  onLoadOlder,
}: ChatLogProps) {
  const suggestions = isSiloChat ? siloQuestions : generalQuestions;
  return (
    <div className="chat-log" ref={logRef} role="log" aria-label="Conversation" tabIndex={0}>
      {!turns.length && !pendingQuestion && (
        <div className="chat-welcome">
          <h3>{isSiloChat ? "What would you like to check?" : "What needs a closer look?"}</h3>
          <div className="chat-suggestions">
            {suggestions.map((question) => (
              <button
                key={question}
                type="button"
                disabled={!canUseSuggestions}
                onClick={() => onSuggestion(question)}
              >
                {question} <i className="bi bi-arrow-up-right" aria-hidden="true" />
              </button>
            ))}
          </div>
        </div>
      )}
      <div className="chat-thread">
        {hasEarlierMessages && (
          <div className="chat-older">
            <button
              className="inventory-button"
              type="button"
              disabled={loadingOlder || !!pendingQuestion}
              onClick={() => {
                void onLoadOlder();
              }}
            >
              {loadingOlder ? "Loading…" : "Load earlier messages"}
            </button>
          </div>
        )}
        {turns.map((turn) => (
          <div className="chat-turn" key={turn.number}>
            <ChatMessage role="user" message={turn.question} />
            <ChatMessage role="assistant" message={turn.answer} />
          </div>
        ))}
        {pendingQuestion && (
          <>
            <ChatMessage role="user" message={pendingQuestion} />
            <p className="chat-thinking" role="status">
              Orbit Assistant is working on your reply…
            </p>
          </>
        )}
      </div>
    </div>
  );
}
