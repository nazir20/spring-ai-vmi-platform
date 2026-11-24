import type { Theme } from "./types/layout";

const storageKey = "orbit-theme";

export function initializeTheme() {
  try {
    if (window.localStorage.getItem(storageKey) === "dark") {
      document.documentElement.dataset.bsTheme = "dark";
    }
  } catch {
    // The toggle still works when browser storage is unavailable.
  }
}

export function getTheme(): Theme {
  return document.documentElement.dataset.bsTheme === "dark" ? "dark" : "light";
}

export function setTheme(theme: Theme) {
  document.documentElement.dataset.bsTheme = theme;
  try {
    window.localStorage.setItem(storageKey, theme);
  } catch {
    // Keep the selected theme for the current page even without storage.
  }
}
