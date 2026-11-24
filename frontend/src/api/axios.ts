import axios from "axios";
import { expireSpringSession, getAuthState } from "../auth/session";
import { legacyApiUrl, springApiUrl, usesSpringLogin } from "./config";

function createClient(baseURL: string) {
  return axios.create({
    baseURL,
    allowAbsoluteUrls: false,
    adapter: "fetch",
    fetchOptions: { cache: "no-store" },
  });
}

export const loginClient = createClient(usesSpringLogin ? springApiUrl : legacyApiUrl);
export const springClient = createClient(springApiUrl);

springClient.interceptors.request.use((config) => {
  const session = getAuthState().session;
  if (session?.backend !== "spring") {
    throw new Error("Sign in with a Spring account to continue.");
  }

  config.headers.set("Authorization", `Bearer ${session.token}`);
  return config;
});

springClient.interceptors.response.use(
  (response) => response,
  (error: unknown) => {
    if (axios.isAxiosError(error) && error.response?.status === 401) {
      expireSpringSession(error.config?.headers.get("Authorization"));
    }
    return Promise.reject(error);
  },
);

export function getApiErrorMessage(error: unknown): string | undefined {
  if (!axios.isAxiosError<{ message?: unknown }>(error)) return;
  const message = error.response?.data?.message;
  if (typeof message === "string" && message.trim()) return message;
}
