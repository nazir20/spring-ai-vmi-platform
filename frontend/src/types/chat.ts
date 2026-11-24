import type { RefObject } from "react";
import type { Silo } from "./inventory";

export type ChatSummary = { id: string; title: string; updatedAt: string };

export type ChatHistoryPage = { conversations: ChatSummary[]; nextCursor: string | null };

export type ChatTurn = { number: number; question: string; answer: string };

export type ChatTranscript = { conversation: ChatSummary; turns: ChatTurn[]; nextBefore: number | null };

export type ChatComposerProps = {
  messageId: string;
  inputRef: RefObject<HTMLTextAreaElement | null>;
  isSiloChat: boolean;
  availability?: { data?: boolean; error?: string };
  draft: string;
  sending: boolean;
  canSend: boolean;
  error: string;
  onDraftChange: (draft: string) => void;
  onSend: () => Promise<void>;
  onReload?: (draft: string) => void;
};

export type ChatHistorySidebarProps = {
  selected: string | null;
  page: ChatHistoryPage;
  error: string;
  loading: boolean;
  onNewChat: () => void;
  onLoadMore: () => Promise<void>;
};

export type ChatLogProps = {
  logRef: RefObject<HTMLDivElement | null>;
  isSiloChat: boolean;
  turns: ChatTurn[];
  pendingQuestion: string;
  hasEarlierMessages: boolean;
  loadingOlder: boolean;
  canUseSuggestions: boolean;
  onSuggestion: (question: string) => void;
  onLoadOlder: () => Promise<void>;
};

export type ChatPanelProps = {
  silo?: Pick<Silo, "id" | "name" | "siloCode">;
  saved?: ChatTranscript;
  onSaved?: (summary: ChatSummary) => void;
  onReload?: (draft: string) => void;
  initialDraft?: string;
};

export type ChatReply = { conversationId: string; message: string; conversation?: ChatSummary };

export type ChatConversationOptions = {
  siloId?: number;
  saved?: ChatTranscript;
  initialDraft: string;
  onSaved?: (summary: ChatSummary) => void;
};
