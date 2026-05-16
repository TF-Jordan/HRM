"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Search, Bell, HelpCircle } from "lucide-react";
import { LocaleSwitcher } from "./LocaleSwitcher";
import { UserChip } from "./UserChip";

export type TopbarProps = {
  user?: {
    displayName: string;
    email?: string;
    role?: string;
  };
};

export function Topbar({ user }: TopbarProps) {
  const tCommon = useTranslations("common");
  return (
    <header
      className="sticky top-0 z-20 flex items-center gap-3 border-b border-line bg-cream/70 px-8 py-3 backdrop-blur-xl backdrop-saturate-150"
      role="banner"
    >
      <div className="flex max-w-[540px] grow items-center gap-2.5 rounded-xl border border-line bg-white px-4 py-2.5 text-ink-3 shadow-elev-sm transition-colors focus-within:border-brand-400 focus-within:shadow-[0_0_0_4px_rgba(242,107,15,0.12)] hover:border-line-strong">
        <Search className="size-4" aria-hidden />
        <input
          type="search"
          placeholder={tCommon("search")}
          aria-label={tCommon("search")}
          className="grow border-none bg-transparent text-[13.5px] text-ink outline-none placeholder:text-ink-4"
        />
        <kbd className="rounded-md border border-line-soft bg-cream-soft px-1.5 py-0.5 font-mono text-[10px] font-semibold text-ink-3">
          ⌘K
        </kbd>
      </div>

      <button
        type="button"
        aria-label={tCommon("help")}
        className="grid size-9 place-items-center rounded-xl border border-line bg-white text-ink-2 shadow-elev-sm transition-all hover:-translate-y-px hover:border-line-strong hover:text-ink"
      >
        <HelpCircle className="size-4" />
      </button>

      <button
        type="button"
        aria-label="Notifications"
        className="relative grid size-9 place-items-center rounded-xl border border-line bg-white text-ink-2 shadow-elev-sm transition-all hover:-translate-y-px hover:border-line-strong hover:text-ink"
      >
        <Bell className="size-4" />
        <span
          aria-hidden
          className="absolute right-2 top-2 size-2 rounded-full bg-brand-500 ring-2 ring-white"
        />
      </button>

      <LocaleSwitcher />

      {user && <UserChip displayName={user.displayName} role={user.role} />}
    </header>
  );
}
