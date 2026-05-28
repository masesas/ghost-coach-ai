import { useQuery } from "@tanstack/react-query";
import { api, unwrap } from "@/lib/api";
import type { ApiResponse } from "@/types/api";
import type { SystemVarItem } from "@/types/systemVar";

export function useSystemVars(category: string, scope?: string) {
  return useQuery({
    queryKey: ["system-vars", category, scope ?? null],
    queryFn: () =>
      unwrap<SystemVarItem[]>(
        api.get<ApiResponse<SystemVarItem[]>>(`/system-vars/${category}`, {
          params: scope ? { scope } : undefined,
        }),
      ),
    staleTime: Infinity,
    gcTime: Infinity,
  });
}
