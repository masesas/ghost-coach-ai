export type ChatRole = "USER" | "ASSISTANT";

export interface ChatMessage {
  id: number;
  role: ChatRole;
  content: string;
  createdAt: string;
}
