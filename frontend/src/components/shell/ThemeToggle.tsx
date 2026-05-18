"use client";

import { Moon, Sun } from "lucide-react";
import { useTranslations } from "next-intl";
import { useTheme } from "@/components/providers/ThemeProvider";

export function ThemeToggle() {
  const { theme, toggle } = useTheme();
  const tCommon = useTranslations("common");
  return (
    <button
      type="button"
      onClick={toggle}
      aria-label={tCommon("toggleTheme")}
      title={tCommon("toggleTheme")}
      className="grid size-9 place-items-center rounded-xl border border-line bg-white text-ink-2 shadow-elev-sm transition-all hover:-translate-y-px hover:border-line-strong hover:text-ink dark:border-dark-line dark:bg-dark-2 dark:text-ink dark:hover:bg-dark-3"
    >
      {theme === "dark" ? <Sun className="size-4" /> : <Moon className="size-4" />}
    </button>
  );
}
