import { useMutation, useQueryClient } from "@tanstack/react-query";
import { api, unwrap } from "@/lib/api";
import type { SessionDetail } from "@/types/session";
import type { ApiResponse } from "@/types/api";

export function useUploadSession() {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: (file: File) => {
      const formData = new FormData();
      formData.append("image", file);
      return unwrap<SessionDetail>(
        api.post<ApiResponse<SessionDetail>>("/sessions", formData, {
          headers: { "Content-Type": "multipart/form-data" },
          timeout: 60_000,
        }),
      );
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["sessions"] });
    },
  });
}
