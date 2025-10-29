export interface ApiResponse<T> {
  data?: T;
  error?: string;
  message?: string;
  status?: number;
  timestamp?: string;
}

export interface ErrorResponse {
  error: string;
  message: string;
  status: number;
  timestamp: string;
  fieldErrors?: { [key: string]: string };
}