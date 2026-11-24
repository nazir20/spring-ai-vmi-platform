import type { ReactNode } from "react";

export interface NavbarProps {
  user: {
    name: string;
    avatar?: string;
  };
}

export interface PageHeaderProps {
  title: string;
  subtitle?: string;
  actions?: ReactNode;
}

export interface SidebarProps {
  user: {
    name: string;
    email: string;
    avatar?: string;
  };
}

export type ThemeToggleProps = { onLoginPage?: boolean };

export type Theme = "light" | "dark";
