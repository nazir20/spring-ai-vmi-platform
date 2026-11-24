import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import ChatPanel from "../components/ChatPanel";
import ChatHistorySidebar from "../components/chat/ChatHistorySidebar";
import SavedChat from "../components/chat/SavedChat";
import PageHeader from "../components/layout/PageHeader";
import { useChatHistory } from "../hooks/useChatHistory";

export default function Assistant() {
  const [params, setParams] = useSearchParams();
  const selected = params.get("chat");
  const [newChat, setNewChat] = useState(0);
  const history = useChatHistory();
  useEffect(() => {
    document.title = "AI Assistant | Orbit Logistics";
  }, []);

  function startChat() {
    setNewChat((revision) => revision + 1);
    setParams({});
  }

  return (
    <div className="silo-inventory-view assistant-page">
      <PageHeader
        title="AI Assistant"
        subtitle="Ask about stock, readings, shipments, and inventory alerts."
      />
      <div className="assistant-workspace">
        <ChatHistorySidebar
          selected={selected}
          page={history.page}
          error={history.error}
          loading={history.loading}
          onNewChat={startChat}
          onLoadMore={history.loadMore}
        />
        {selected ? (
          <SavedChat key={selected} id={selected} onSaved={history.remember} />
        ) : (
          <ChatPanel
            key={newChat}
            onSaved={(chat) => {
              history.remember(chat);
              setParams({ chat: chat.id }, { replace: true });
            }}
          />
        )}
      </div>
    </div>
  );
}
