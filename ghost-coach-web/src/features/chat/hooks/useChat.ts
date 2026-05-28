import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { api, unwrap } from "@/lib/api";
import type { ChatMessage } from "@/types/chat";
import type { ApiResponse, PageResponse } from "@/types/api";

export function useChatHistory(sessionId: number) {
  return useQuery({
    queryKey: ["chat", sessionId],
    queryFn: async () => {
      const page = await unwrap<PageResponse<ChatMessage>>(
        api.get<ApiResponse<PageResponse<ChatMessage>>>(
          `/sessions/${sessionId}/chat`,
        ),
      );
      return page.content;
    },
  });
}

export function useSendMessage(sessionId: number) {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (message: string) =>
      unwrap<ChatMessage[]>(
        api.post<ApiResponse<ChatMessage[]>>(`/sessions/${sessionId}/chat`, {
          message,
        }),
      ),
    onSuccess: (newMessages) => {
      queryClient.setQueryData<ChatMessage[]>(["chat", sessionId], (old) => [
        ...(old ?? []),
        ...newMessages,
      ]);
    },
  });
}
