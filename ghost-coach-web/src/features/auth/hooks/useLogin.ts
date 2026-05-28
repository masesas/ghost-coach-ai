import { useMutation } from "@tanstack/react-query";
import { api, extractError, unwrap } from "@/lib/api";
import type { AuthResponse, LoginPayload } from "@/types/user";
import type { ApiResponse } from "@/types/api";

export function useLogin() {
  return useMutation({
    mutationFn: (payload: LoginPayload) =>
      unwrap<AuthResponse>(api.post<ApiResponse<AuthResponse>>("/auth/login", payload)),
    onError: (error) => extractError(error),
  });
}
