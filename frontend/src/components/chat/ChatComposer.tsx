import type { FormEvent } from "react";
import { maxChatMessageLength } from "../../chatHistory";
import type { ChatComposerProps } from "../../types/chat";

export default function ChatComposer({
  messageId,
  inputRef,
  isSiloChat,
  availability,
  draft,
  sending,
  canSend,
  error,
  onDraftChange,
  onSend,
  onReload,
}: ChatComposerProps) {
  const notice = error || availability?.error;

  function submitMessage(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    void onSend();
  }

  return (
    <form className="chat-compose" onSubmit={submitMessage}>
      {!availability && (
        <p className="chat-notice" role="status">
          Checking assistant availability…
        </p>
      )}
      {availability?.data === false && (
        <p className="chat-notice" role="status">
          Add your KI:connect key to <code>spring-backend/.env</code> and restart Spring to enable chat.
        </p>
      )}
      {notice && (
        <div className="chat-notice chat-error" role="alert">
          {notice}
          {error && onReload && (
            <button
              className="inventory-button chat-reload"
              type="button"
              onClick={() => onReload(draft)}
            >
              Reload chat
            </button>
          )}
        </div>
      )}
      <label className="visually-hidden" htmlFor={messageId}>
        Message
      </label>
      <div className="chat-input-group">
        <textarea
          id={messageId}
          ref={inputRef}
          rows={2}
          maxLength={maxChatMessageLength}
          value={draft}
          placeholder={isSiloChat ? "Ask about this silo…" : "Type a message…"}
          disabled={availability?.data !== true}
          readOnly={sending}
          onChange={(event) => onDraftChange(event.target.value)}
          onKeyDown={(event) => {
            if (event.key === "Enter" && !event.shiftKey && !event.nativeEvent.isComposing) {
              event.preventDefault();
              event.currentTarget.form?.requestSubmit();
            }
          }}
        />
        <button className="chat-send" type="submit" disabled={!canSend || !draft.trim()}>
          Send <i className="bi bi-arrow-up" aria-hidden="true" />
        </button>
      </div>
      <p className="chat-compose-note">Messages and relevant records are sent to KI:connect.</p>
    </form>
  );
}
