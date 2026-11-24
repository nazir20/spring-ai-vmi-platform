import { useEffect, useRef, useState } from "react";
import { conversationsPath } from "../chatHistory";
import { requestSpring } from "../springApi";
import type { ChatHistoryPage, ChatSummary } from "../types/chat";

function mergeChats(current: ChatSummary[], incoming: ChatSummary[]) {
  const byId = new Map(current.map((chat) => [chat.id, chat]));
  for (const chat of incoming) {
    const existing = byId.get(chat.id);
    if (!existing || Date.parse(chat.updatedAt) >= Date.parse(existing.updatedAt))
      byId.set(chat.id, chat);
  }
  return [...byId.values()].sort(
    (left, right) => Date.parse(right.updatedAt) - Date.parse(left.updatedAt),
  );
}

export function useChatHistory() {
  const [page, setPage] = useState<ChatHistoryPage>({ conversations: [], nextCursor: null });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);
  const request = useRef<AbortController | null>(null);

  useEffect(() => {
    const controller = new AbortController();
    request.current = controller;
    requestSpring<ChatHistoryPage>(conversationsPath, { signal: controller.signal })
      .then((result) => {
        if (!controller.signal.aborted)
          setPage((current) => ({
            ...result,
            conversations: mergeChats(current.conversations, result.conversations),
          }));
      })
      .catch((reason: Error) => {
        if (!controller.signal.aborted) setError(reason.message);
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          request.current = null;
          setLoading(false);
        }
      });
    return () => request.current?.abort();
  }, []);

  async function loadMore() {
    if (!page.nextCursor || request.current) return;
    const controller = new AbortController();
    request.current = controller;
    setLoading(true);
    setError("");
    try {
      const result = await requestSpring<ChatHistoryPage>(
        `${conversationsPath}?before=${encodeURIComponent(page.nextCursor)}`,
        { signal: controller.signal },
      );
      if (!controller.signal.aborted)
        setPage((current) => ({
          ...result,
          conversations: mergeChats(current.conversations, result.conversations),
        }));
    } catch (reason) {
      if (!controller.signal.aborted)
        setError(reason instanceof Error ? reason.message : "Could not load past chats.");
    } finally {
      if (!controller.signal.aborted) {
        request.current = null;
        setLoading(false);
      }
    }
  }

  function remember(chat: ChatSummary) {
    setPage((current) => ({ ...current, conversations: mergeChats(current.conversations, [chat]) }));
  }

  return { page, error, loading, loadMore, remember };
}
