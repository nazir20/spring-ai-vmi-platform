import { useSyncExternalStore, type ReactNode } from "react";
import { login as requestLogin } from "../services/authService";
import { getAuthState, signOut, storeSession, subscribeToAuth } from "./session";
import { AuthContext } from "./useAuth";
import type { LoginCredentials } from "../types/auth";

async function login(credentials: LoginCredentials) {
  const session = await requestLogin(credentials);
  storeSession(session, credentials.rememberMe);
}

export default function AuthProvider({ children }: { children: ReactNode }) {
  const auth = useSyncExternalStore(subscribeToAuth, getAuthState);

  return (
    <AuthContext.Provider value={{ ...auth, login, logout: signOut }}>
      {children}
    </AuthContext.Provider>
  );
}
