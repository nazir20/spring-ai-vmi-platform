import axios from "axios";
import { getApiErrorMessage, loginClient } from "../api/axios";
import { usesSpringLogin } from "../api/config";
import { isRoleList, readLegacyUser } from "../auth/session";
import type { SpringResponse } from "../types/api";
import type { AuthSession, LoginCredentials } from "../types/auth";

export async function login({ email, password }: LoginCredentials): Promise<AuthSession> {
  const credentials = { email: email.trim(), password };
  try {
    if (usesSpringLogin) {
      const { data } = await loginClient.post<SpringResponse<{ token?: unknown; roles?: unknown }>>(
        "/api/auth/login", credentials,
      );
      const result = data?.data;
      if (typeof result?.token === "string" && result.token && isRoleList(result.roles)) {
        return { backend: "spring", token: result.token, user: { email: credentials.email, roles: result.roles } };
      }
    } else {
      const { data } = await loginClient.post<{ token?: unknown; user?: unknown }>(
        "/api/auth/login", credentials,
      );
      const user = readLegacyUser(data?.user);
      if (typeof data?.token === "string" && data.token && user) {
        return { backend: "legacy", token: data.token, user };
      }
    }
  } catch (error) {
    if (!axios.isAxiosError(error)) throw error;
    const status = error.response?.status;
    const message = status && status < 500 ? getApiErrorMessage(error) : undefined;
    const fallback = status === 401 ? "Invalid email or password." : "Unable to sign in. Please try again.";
    throw new Error(message ?? fallback, { cause: error });
  }

  throw new Error("The server returned an invalid sign-in response.");
}
