import { useState } from "react";
import { Link } from "react-router-dom";
import type { ChatHistorySidebarProps } from "../../types/chat";

const shortDate = new Intl.DateTimeFormat(undefined, { dateStyle: "medium" });

export default function ChatHistorySidebar({
  selected,
  page,
  error,
  loading,
  onNewChat,
  onLoadMore,
}: ChatHistorySidebarProps) {
  const [historyOpen, setHistoryOpen] = useState(false);

  function startChat() {
    setHistoryOpen(false);
    onNewChat();
  }

  return (
    <aside className="assistant-rail" aria-label="Chat history">
      <div className="assistant-rail-header">
        <h2>Past chats</h2>
        <button
          className="inventory-button assistant-history-toggle"
          type="button"
          aria-controls="assistant-history"
          aria-expanded={historyOpen}
          onClick={() => setHistoryOpen((open) => !open)}
        >
          <i className="bi bi-clock-history" aria-hidden="true" /> Past chats
        </button>
        <button className="inventory-button assistant-new" type="button" onClick={startChat}>
          <i className="bi bi-plus-lg" aria-hidden="true" /> New chat
        </button>
      </div>
      <div id="assistant-history" className={`assistant-history${historyOpen ? " is-open" : ""}`}>
        {page.conversations.length > 0 && (
          <ul className="assistant-chat-list">
            {page.conversations.map((chat) => (
              <li key={chat.id}>
                <Link
                  to={`/chat?chat=${encodeURIComponent(chat.id)}`}
                  aria-current={selected === chat.id ? "page" : undefined}
                  title={chat.title}
                  onClick={() => setHistoryOpen(false)}
                >
                  <span>{chat.title}</span>
                  <time dateTime={chat.updatedAt}>{shortDate.format(new Date(chat.updatedAt))}</time>
                </Link>
              </li>
            ))}
          </ul>
        )}
        {error && (
          <p className="assistant-history-note" role="alert">
            {error}
          </p>
        )}
        {loading && (
          <p className="assistant-history-note" role="status">
            Loading past chats…
          </p>
        )}
        {!loading && !error && !page.conversations.length && (
          <p className="assistant-history-note">Your chats will appear here after the first reply.</p>
        )}
        {page.nextCursor && (
          <button
            className="inventory-button assistant-more"
            type="button"
            disabled={loading}
            onClick={() => {
              void onLoadMore();
            }}
          >
            Load more chats
          </button>
        )}
      </div>
    </aside>
  );
}
