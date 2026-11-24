export type AuthUser = {
  email: string;
  name?: string;
  roles: string[];
};

export type AuthSession = {
  backend: "spring" | "legacy";
  token: string;
  user: AuthUser;
};

export type AuthState = {
  session: AuthSession | null;
  expired: boolean;
};

export type AuthContextValue = {
  session: AuthSession | null;
  expired: boolean;
  login: (credentials: LoginCredentials) => Promise<void>;
  logout: () => void;
};

export type LoginCredentials = {
  email: string;
  password: string;
  rememberMe: boolean;
};
