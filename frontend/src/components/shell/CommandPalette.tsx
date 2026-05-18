"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Search } from "lucide-react";
import { useRouter } from "@/i18n/navigation";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";

type CommandItem = {
  labelKey: string;
  href: string;
  group: "navigation";
};

const ITEMS: CommandItem[] = [
  { labelKey: "items.dashboard", href: "/dashboard", group: "navigation" },
  { labelKey: "items.analytics", href: "/analytics", group: "navigation" },
  { labelKey: "items.employees", href: "/employees", group: "navigation" },
  { labelKey: "items.skills", href: "/skills", group: "navigation" },
  { labelKey: "items.recruitment", href: "/recruitment", group: "navigation" },
  { labelKey: "items.timesheets", href: "/timesheets", group: "navigation" },
  { labelKey: "items.leaves", href: "/leaves", group: "navigation" },
  { labelKey: "items.missionOrders", href: "/mission-orders", group: "navigation" },
  { labelKey: "items.payroll", href: "/payroll", group: "navigation" },
  { labelKey: "items.loans", href: "/loans", group: "navigation" },
  { labelKey: "items.expenses", href: "/expenses", group: "navigation" },
  { labelKey: "items.reviews", href: "/reviews", group: "navigation" },
  { labelKey: "items.trainings", href: "/trainings", group: "navigation" },
  { labelKey: "items.trainingBudgets", href: "/training-budgets", group: "navigation" },
  { labelKey: "items.medical", href: "/medical", group: "navigation" },
  { labelKey: "items.declarations", href: "/declarations", group: "navigation" },
  { labelKey: "items.profile", href: "/me/profile", group: "navigation" },
];

function PaletteBody({
  onSelect,
}: {
  onSelect: (href: string) => void;
}) {
  const tNav = useTranslations("navigation");
  const tCommon = useTranslations("common");
  const [query, setQuery] = React.useState("");
  const [activeIndex, setActiveIndex] = React.useState(0);

  const filtered = React.useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return ITEMS;
    return ITEMS.filter((item) => tNav(item.labelKey).toLowerCase().includes(q));
  }, [query, tNav]);

  const clampedIndex = Math.min(activeIndex, Math.max(0, filtered.length - 1));

  const handleQueryChange = (v: string) => {
    setQuery(v);
    setActiveIndex(0);
  };

  const handleKey = (e: React.KeyboardEvent<HTMLDivElement>) => {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setActiveIndex(Math.min(filtered.length - 1, clampedIndex + 1));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setActiveIndex(Math.max(0, clampedIndex - 1));
    } else if (e.key === "Enter") {
      e.preventDefault();
      const target = filtered[clampedIndex];
      if (target) onSelect(target.href);
    }
  };

  return (
    <>
      <div className="border-b border-line px-4 py-3" onKeyDown={handleKey}>
        <div className="flex items-center gap-2">
          <Search className="size-4 text-ink-3" />
          <input
            autoFocus
            value={query}
            onChange={(e) => handleQueryChange(e.target.value)}
            placeholder={tCommon("search")}
            aria-label={tCommon("search")}
            className="grow border-none bg-transparent text-[14px] text-ink outline-none placeholder:text-ink-4"
          />
        </div>
      </div>
      <div className="max-h-[60vh] overflow-y-auto py-1" onKeyDown={handleKey} tabIndex={-1}>
        {filtered.length === 0 && (
          <div className="py-6 text-center text-sm text-ink-3">{tCommon("noResults")}</div>
        )}
        {filtered.map((item, i) => (
          <button
            key={item.href}
            type="button"
            onClick={() => onSelect(item.href)}
            onMouseEnter={() => setActiveIndex(i)}
            className={
              "block w-full px-4 py-2.5 text-left text-[13.5px] transition-colors " +
              (i === clampedIndex
                ? "bg-brand-50 text-brand-700"
                : "text-ink-2 hover:bg-cream-soft")
            }
          >
            {tNav(item.labelKey)}
          </button>
        ))}
      </div>
    </>
  );
}

export function CommandPalette({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}) {
  const tCommon = useTranslations("common");
  const router = useRouter();

  const handleSelect = React.useCallback(
    (href: string) => {
      onOpenChange(false);
      router.push(href as never);
    },
    [onOpenChange, router],
  );

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="max-w-lg p-0">
        <DialogHeader className="sr-only">
          <DialogTitle>{tCommon("search")}</DialogTitle>
        </DialogHeader>
        {open && <PaletteBody key="palette" onSelect={handleSelect} />}
      </DialogContent>
    </Dialog>
  );
}

export function useCommandPaletteHotkey(open: () => void) {
  React.useEffect(() => {
    const handler = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
        e.preventDefault();
        open();
      }
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [open]);
}
