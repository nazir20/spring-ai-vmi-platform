import { useState } from "react";
import { getTheme, setTheme } from "../../theme";
import type { ThemeToggleProps } from "../../types/layout";

export default function ThemeToggle({ onLoginPage = false }: ThemeToggleProps) {
  const [theme, setSelectedTheme] = useState(getTheme);
  const dark = theme === "dark";

  function toggleTheme() {
    const nextTheme = dark ? "light" : "dark";
    setTheme(nextTheme);
    setSelectedTheme(nextTheme);
  }

  return (
    <button
      className={`navbar-action-btn theme-toggle${onLoginPage ? " theme-toggle-login" : ""}`}
      type="button"
      aria-label="Dark mode"
      aria-pressed={dark}
      title={dark ? "Switch to light mode" : "Switch to dark mode"}
      onClick={toggleTheme}
    >
      <i className={dark ? "bi bi-sun" : "bi bi-moon-stars"} aria-hidden="true" />
    </button>
  );
}
