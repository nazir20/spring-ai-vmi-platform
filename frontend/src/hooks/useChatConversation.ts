import { useEffect, useLayoutEffect, useRef, useState } from "react";
import { maxChatMessageLength, transcriptPath } from "../chatHistory";
import { requestSpring } from "../springApi";
import { useSpringRequest } from "./useSpringRequest";
import type { ChatConversationOptions, ChatReply, ChatTranscript, ChatTurn } from "../types/chat";

export function useChatConversation({ siloId, saved, initialDraft, onSaved }: ChatConversationOptions) {
  const [turns, setTurns] = useState<ChatTurn[]>(saved?.turns ?? []);
  const [conversationId, setConversationId] = useState(saved?.conversation.id);
  const [beforeTurn, setBeforeTurn] = useState(saved?.nextBefore ?? null);
  const [loadingOlder, setLoadingOlder] = useState(false);
  const [draft, setDraft] = useState(initialDraft);
  const [pendingQuestion, setPending] = useState("");
  const [error, setError] = useState("");
  const status = useSpringRequest<boolean>("/api/chat/status");
  const inputRef = useRef<HTMLTextAreaElement>(null);
  const logRef = useRef<HTMLDivElement>(null);
  const request = useRef<AbortController | null>(null);
  const olderRequest = useRef<AbortController | null>(null);
  const previousScroll = useRef<{ height: number; top: number } | null>(null);
  const canType = status?.data === true && !pendingQuestion && !loadingOlder;

  useEffect(
    () => () => {
      request.current?.abort();
      olderRequest.current?.abort();
    },
    [],
  );
  useLayoutEffect(() => {
    if (logRef.current) {
      const scroll = previousScroll.current;
      const height = logRef.current.scrollHeight;
      logRef.current.scrollTop = scroll ? scroll.top + height - scroll.height : height;
    }
    previousScroll.current = null;
  }, [turns, pendingQuestion]);

  function applySuggestion(question: string) {
    setDraft(question);
    inputRef.current?.focus();
  }

  function reset() {
    setTurns([]);
    setConversationId(undefined);
    setDraft("");
    setError("");
    inputRef.current?.focus();
  }

  async function loadOlder() {
    if (!conversationId || beforeTurn == null || olderRequest.current || request.current) return;
    const controller = new AbortController();
    olderRequest.current = controller;
    setLoadingOlder(true);
    setError("");
    try {
      const page = await requestSpring<ChatTranscript>(transcriptPath(conversationId, beforeTurn), {
        signal: controller.signal,
      });
      if (controller.signal.aborted) return;
      if (logRef.current)
        previousScroll.current = { height: logRef.current.scrollHeight, top: logRef.current.scrollTop };
      setTurns((current) => [...page.turns, ...current]);
      setBeforeTurn(page.nextBefore);
    } catch (err) {
      if (!controller.signal.aborted)
        setError(err instanceof Error ? err.message : "Could not load older messages.");
    } finally {
      olderRequest.current = null;
      if (!controller.signal.aborted) setLoadingOlder(false);
    }
  }

  async function send() {
    const question = draft.trim();
    if (!question || question.length > maxChatMessageLength || !canType || request.current) return;

    const controller = new AbortController();
    request.current = controller;
    setPending(question);
    setError("");
    try {
      const reply = await requestSpring<ChatReply>("/api/chat", {
        method: "POST",
        data: {
          message: question,
          conversationId,
          siloId,
          afterTurn: saved ? turns.at(-1)?.number : undefined,
        },
        signal: controller.signal,
      });
      if (controller.signal.aborted) return;
      setConversationId(reply.conversationId);
      setTurns((previous) => [
        ...previous,
        { number: (previous.at(-1)?.number ?? 0) + 1, question, answer: reply.message },
      ]);
      setDraft("");
      if (reply.conversation) onSaved?.(reply.conversation);
    } catch (err) {
      if (!controller.signal.aborted)
        setError(err instanceof Error ? err.message : "Could not send this message.");
    } finally {
      request.current = null;
      if (!controller.signal.aborted) {
        setPending("");
        inputRef.current?.focus();
      }
    }
  }

  return {
    turns,
    beforeTurn,
    loadingOlder,
    draft,
    pendingQuestion,
    error,
    status,
    canType,
    inputRef,
    logRef,
    setDraft,
    applySuggestion,
    reset,
    loadOlder,
    send,
  };
}
