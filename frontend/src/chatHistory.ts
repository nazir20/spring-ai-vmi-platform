export const maxChatMessageLength = 2000;

export const conversationsPath = "/api/chat/conversations";

export function transcriptPath(id: string, before?: number) {
  return `${conversationsPath}/${encodeURIComponent(id)}${before === undefined ? "" : `?before=${before}`}`;
}
