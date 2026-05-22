"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Search, Bell, HelpCircle } from "lucide-react";
import { LocaleSwitcher } from "./LocaleSwitcher";
import { UserChip } from "./UserChip";
import { ThemeToggle } from "./ThemeToggle";
import { MobileSidebar } from "./MobileSidebar";
import { CommandPalette, useCommandPaletteHotkey } from "./CommandPalette";

export type TopbarProps = {
  user?: {
    displayName: string;
    email?: string;
    matricule?: string | null;
    role?: string;
  };
};

export function Topbar({ user }: TopbarProps) {
  const tCommon = useTranslations("common");
  const [paletteOpen, setPaletteOpen] = React.useState(false);
  useCommandPaletteHotkey(() => setPaletteOpen(true));

  return (
    <header
      className="sticky top-0 z-20 flex items-center gap-3 border-b border-line bg-cream/70 px-4 py-3 backdrop-blur-xl backdrop-saturate-150 md:px-8"
      role="banner"
    >
      <MobileSidebar />

      <button
        type="button"
        onClick={() => setPaletteOpen(true)}
        aria-label={tCommon("search")}
        className="flex max-w-[540px] grow items-center gap-2.5 rounded-xl border border-line bg-white px-4 py-2.5 text-left text-ink-3 shadow-elev-sm transition-colors hover:border-line-strong dark:border-dark-line dark:bg-dark-2"
      >
        <Search className="size-4" aria-hidden />
        <span className="grow text-[13.5px] text-ink-4">{tCommon("search")}</span>
        <kbd className="hidden rounded-md border border-line-soft bg-cream-soft px-1.5 py-0.5 font-mono text-[10px] font-semibold text-ink-3 sm:inline dark:border-dark-line dark:bg-dark-3">
          ⌘K
        </kbd>
      </button>

      <button
        type="button"
        aria-label={tCommon("help")}
        className="hidden size-9 place-items-center rounded-xl border border-line bg-white text-ink-2 shadow-elev-sm transition-all hover:-translate-y-px hover:border-line-strong hover:text-ink md:grid dark:border-dark-line dark:bg-dark-2 dark:text-ink"
      >
        <HelpCircle className="size-4" />
      </button>

      <button
        type="button"
        aria-label="Notifications"
        className="relative hidden size-9 place-items-center rounded-xl border border-line bg-white text-ink-2 shadow-elev-sm transition-all hover:-translate-y-px hover:border-line-strong hover:text-ink md:grid dark:border-dark-line dark:bg-dark-2 dark:text-ink"
      >
        <Bell className="size-4" />
        <span
          aria-hidden
          className="absolute right-2 top-2 size-2 rounded-full bg-brand-500 ring-2 ring-white"
        />
      </button>

      <ThemeToggle />

      <LocaleSwitcher />

      {user && (
        <UserChip
          displayName={user.displayName}
          matricule={user.matricule ?? null}
          role={user.role}
        />
      )}

      <CommandPalette open={paletteOpen} onOpenChange={setPaletteOpen} />
    </header>
  );
}
