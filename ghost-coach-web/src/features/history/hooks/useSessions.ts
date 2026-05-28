import { useQuery } from "@tanstack/react-query";
import { api, unwrap } from "@/lib/api";
import type { PageResponse, ApiResponse } from "@/types/api";
import type { SessionSummary } from "@/types/session";

export function useSessions(page = 0, size = 20) {
  return useQuery({
    queryKey: ["sessions", { page, size }],
    queryFn: () =>
      unwrap<PageResponse<SessionSummary>>(
        api.get<ApiResponse<PageResponse<SessionSummary>>>("/sessions", {
          params: { page, size },
        }),
      ),
  });
}
