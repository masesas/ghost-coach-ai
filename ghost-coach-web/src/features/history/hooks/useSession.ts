import { useQuery } from "@tanstack/react-query";
import { api, unwrap } from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type { SessionDetail } from "@/types/session";

export function useSession(id: number | undefined) {
  return useQuery({
    queryKey: ["sessions", id],
    enabled: !!id,
    queryFn: () =>
      unwrap<SessionDetail>(api.get<ApiResponse<SessionDetail>>(`/sessions/${id}`)),
  });
}
