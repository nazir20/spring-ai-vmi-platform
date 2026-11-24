import { authBackend } from "../api/config";
import type { AuthSession, AuthState, AuthUser } from "../types/auth";

const sessionKey = "orbit-session";
const previousKeys = ["orbit-spring-token", "token", "user"];
const storageNames = ["sessionStorage", "localStorage"] as const;
const listeners = new Set<() => void>();

export function isRoleList(value: unknown): value is string[] {
  return Array.isArray(value) && value.every((role: unknown) => typeof role === "string");
}

export function readLegacyUser(value: unknown): AuthUser | null {
  if (!value || typeof value !== "object" || !("email" in value)
      || typeof value.email !== "string" || !value.email) {
    return null;
  }

  return {
    email: value.email,
    name: "name" in value && typeof value.name === "string" ? value.name : undefined,
    roles: "role" in value && typeof value.role === "string" ? [value.role] : [],
  };
}

function readSession(storage: Storage): AuthSession | null {
  const saved = storage.getItem(sessionKey);
  if (saved) {
    const session = JSON.parse(saved) as Partial<AuthSession> | null;
    if (session?.backend === authBackend && typeof session.token === "string" && session.token
        && typeof session.user?.email === "string" && session.user.email
        && (session.user.name === undefined || typeof session.user.name === "string")
        && isRoleList(session.user.roles)) {
      return session as AuthSession;
    }
  }

  if (authBackend === "legacy") {
    const token = storage.getItem("token");
    const user = readLegacyUser(JSON.parse(storage.getItem("user") ?? "null"));
    return token && user ? { backend: "legacy", token, user } : null;
  }

  const token = storage.getItem("orbit-spring-token");
  if (!token) return null;

  // The old Spring session stored only a token. Read the subject for display;
  // the backend validates the token and enforces permissions on each request.
  const payload = token.split(".")[1]?.replace(/-/g, "+").replace(/_/g, "/");
  if (!payload) return null;

  const bytes = Uint8Array.from(atob(payload), (character) => character.charCodeAt(0));
  const claims = JSON.parse(new TextDecoder().decode(bytes)) as { sub?: unknown } | null;
  if (typeof claims?.sub !== "string" || !claims.sub) return null;
  return { backend: "spring", token, user: { email: claims.sub, roles: [] } };
}

function restoreSession(): AuthSession | null {
  for (const storageName of storageNames) {
    try {
      const session = readSession(window[storageName]);
      if (session) return session;
    } catch {
      // A blocked storage area or an invalid record should not prevent sign-in.
    }
  }

  return null;
}

let state: AuthState = { session: restoreSession(), expired: false };

export function getAuthState(): AuthState {
  return state;
}

export function subscribeToAuth(listener: () => void) {
  listeners.add(listener);
  return () => { listeners.delete(listener); };
}

function updateAuthState(nextState: AuthState) {
  state = nextState;
  listeners.forEach((listener) => listener());
}

function clearSavedSessions(expectedToken?: string) {
  for (const storageName of storageNames) {
    try {
      const storage = window[storageName];
      if (expectedToken && readSession(storage)?.token !== expectedToken) continue;
      storage.removeItem(sessionKey);
      previousKeys.forEach((key) => storage.removeItem(key));
    } catch {
      // Signing out still clears the current page's session.
    }
  }
}

export function storeSession(session: AuthSession, rememberMe: boolean) {
  clearSavedSessions();
  try {
    const storage = rememberMe ? window.localStorage : window.sessionStorage;
    storage.setItem(sessionKey, JSON.stringify(session));
  } catch {
    // The current page can still use the session when persistence is unavailable.
  }
  updateAuthState({ session, expired: false });
}

export function signOut() {
  clearSavedSessions();
  updateAuthState({ session: null, expired: false });
}

export function expireSpringSession(authorization: unknown) {
  const session = state.session;
  if (session?.backend !== "spring" || authorization !== `Bearer ${session.token}`) return;

  clearSavedSessions(session.token);
  updateAuthState({ session: null, expired: true });
}
