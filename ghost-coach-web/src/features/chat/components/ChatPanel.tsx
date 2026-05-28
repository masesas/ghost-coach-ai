import { useEffect, useRef } from "react";
import { MessageSquare } from "lucide-react";
import { toast } from "sonner";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { MessageBubble } from "./MessageBubble";
import { ChatInput } from "./ChatInput";
import { useChatHistory, useSendMessage } from "../hooks/useChat";
import { extractError } from "@/lib/api";

export function ChatPanel({ sessionId }: { sessionId: number }) {
  const { data: messages, isLoading } = useChatHistory(sessionId);
  const sendMutation = useSendMessage(sessionId);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: "smooth" });
  }, [messages]);

  const handleSend = async (message: string) => {
    try {
      await sendMutation.mutateAsync(message);
    } catch (error) {
      toast.error(extractError(error).message);
    }
  };

  return (
    <Card className="flex h-[500px] flex-col">
      <CardHeader className="shrink-0">
        <CardTitle className="flex items-center gap-2 text-base">
          <MessageSquare className="h-4 w-4" />
          Coaching Chat
        </CardTitle>
      </CardHeader>
      <CardContent className="flex flex-1 flex-col gap-3 overflow-hidden px-4 pb-4">
        <div className="flex-1 space-y-3 overflow-y-auto">
          {isLoading ? (
            <div className="space-y-3">
              <Skeleton className="h-10 w-3/4" />
              <Skeleton className="ml-auto h-8 w-1/2" />
            </div>
          ) : !messages || messages.length === 0 ? (
            <p className="text-center text-sm text-gray-400">
              Ask a follow-up question about your session.
            </p>
          ) : (
            messages.map((msg) => <MessageBubble key={msg.id} message={msg} />)
          )}
          {sendMutation.isPending ? (
            <div className="flex items-center gap-2 text-xs text-gray-400">
              <span className="inline-block h-1.5 w-1.5 animate-pulse rounded-full bg-primary-400" />
              AI is typing…
            </div>
          ) : null}
          <div ref={bottomRef} />
        </div>
        <ChatInput onSend={handleSend} disabled={sendMutation.isPending} />
      </CardContent>
    </Card>
  );
}
