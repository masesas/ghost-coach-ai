import { useQuery } from "@tanstack/react-query";
import { api, unwrap } from "@/lib/api";
import type { PageResponse, ApiResponse } from "@/types/api";
import type { SessionSummary } from "@/types/session";

const CHART_PAGE_SIZE = 50;

export function useSessionsForChart() {
  return useQuery({
    queryKey: ["sessions", "chart", { page: 0, size: CHART_PAGE_SIZE }],
    queryFn: () =>
      unwrap<PageResponse<SessionSummary>>(
        api.get<ApiResponse<PageResponse<SessionSummary>>>("/sessions", {
          params: { page: 0, size: CHART_PAGE_SIZE },
        }),
      ),
  });
}
