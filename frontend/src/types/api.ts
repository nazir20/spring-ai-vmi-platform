import type { AxiosRequestConfig } from "axios";

export type SpringResponse<T> = {
  statusCode: number;
  message?: string;
  data?: T | null;
};

export type SpringRequestOptions = Pick<AxiosRequestConfig, "method" | "data" | "signal">;
