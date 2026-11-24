const selectedBackend = import.meta.env.VITE_AUTH_BACKEND;

// Explicit legacy settings keep the Express login available. Spring is the default for Orbit.
export const usesSpringLogin = selectedBackend === "spring"
  || (!selectedBackend && !import.meta.env.VITE_API_URL);
export const springApiUrl = import.meta.env.VITE_SPRING_API_URL ?? "http://127.0.0.1:8000";
export const legacyApiUrl = import.meta.env.VITE_API_URL || "http://localhost:5000";
export const authBackend = usesSpringLogin ? "spring" : "legacy";
