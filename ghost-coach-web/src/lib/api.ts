import axios, { AxiosError } from "axios";
import type { ApiResponse, ErrorResponse } from "@/types/api";

const baseURL = import.meta.env.VITE_API_BASE_URL ?? "/api/v1";

export const api = axios.create({
  baseURL,
  headers: { "Content-Type": "application/json" },
});

const TOKEN_KEY = "ghostcoach.token";

export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

api.interceptors.request.use((config) => {
  const token = tokenStorage.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

let onUnauthorized: (() => void) | null = null;

export function setUnauthorizedHandler(handler: () => void) {
  onUnauthorized = handler;
}

api.interceptors.response.use(
  (res) => res,
  (error: AxiosError<ApiResponse<unknown>>) => {
    if (error.response?.status === 401) {
      tokenStorage.clear();
      onUnauthorized?.();
    }
    return Promise.reject(error);
  },
);

export function extractError(error: unknown): ErrorResponse {
  if (axios.isAxiosError(error)) {
    const data = error.response?.data as ApiResponse<unknown> | undefined;
    return {
      code: "NETWORK_ERROR",
      message: data?.message ?? error.message ?? "Network error",
    };
  }
  return {
    code: "UNKNOWN",
    message: error instanceof Error ? error.message : "Unknown error",
  };
}

export async function unwrap<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise;
  const body = response.data;
  if (body.error) {
    throw new Error(body.error.message);
  }
  return body.data as T;
}
