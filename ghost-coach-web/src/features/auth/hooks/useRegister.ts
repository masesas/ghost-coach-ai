import { useMutation } from "@tanstack/react-query";
import { api, extractError, unwrap } from "@/lib/api";
import type { AuthResponse, RegisterPayload } from "@/types/user";
import type { ApiResponse } from "@/types/api";

export function useRegister() {
  return useMutation({
    mutationFn: (payload: RegisterPayload) =>
      unwrap<AuthResponse>(api.post<ApiResponse<AuthResponse>>("/auth/register", payload)),
    onError: (error) => extractError(error),
  });
}
