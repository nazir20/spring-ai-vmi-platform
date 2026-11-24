import axios from "axios";
import { getApiErrorMessage, springClient } from "./api/axios";
import type { SpringRequestOptions, SpringResponse } from "./types/api";

export async function requestSpring<T>(path: string, options: SpringRequestOptions = {}): Promise<T> {
  if (!path.startsWith("/api/")) throw new Error("Invalid Spring API path.");

  try {
    const { data } = await springClient.request<SpringResponse<T>>({ url: path, ...options });
    if (data?.data === undefined || data.data === null) {
      throw new Error("The inventory service returned no data.");
    }
    return data.data;
  } catch (error) {
    if (axios.isCancel(error) || !axios.isAxiosError(error)) throw error;

    const status = error.response?.status;
    if (status === 401) throw new Error("Your Spring session expired. Sign in again.", { cause: error });
    if (status === 403) throw new Error("This page requires a Spring administrator account.", { cause: error });
    if (!status) throw new Error("Unable to connect to the inventory service.", { cause: error });
    const message = [400, 404, 409, 502].includes(status) ? getApiErrorMessage(error) : undefined;
    throw new Error(message ?? `The inventory service returned HTTP ${status}.`, { cause: error });
  }
}
