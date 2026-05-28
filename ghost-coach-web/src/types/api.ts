export interface ApiResponse<T> {
  data: T | null;
  message: string;
  error: ErrorResponse | null;
  timestamp: string;
}

export interface ErrorResponse {
  code: string;
  message: string;
  fieldErrors?: FieldError[];
}

export interface FieldError {
  field: string;
  message: string;
}

export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
