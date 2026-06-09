"use client";

import { useQuery } from "@tanstack/react-query";
import { CornerDownLeft, Loader2, Search } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { useSession } from "@/components/providers/session-provider";
import { useAppRouter } from "@/components/ui/app-link";
import { Avatar } from "@/components/ui/avatar";
import { usePathname } from "@/i18n/navigation";
import { apiFetch } from "@/lib/api-client";
import { activeSlugFromPath, entitledSlugs } from "@/lib/roles";
import { sidebarForRole } from "@/lib/sidebar";
import { cn, initials } from "@/lib/utils";
import type { EmployeeResponse } from "@/server/ksm/modules/employees";

type NavResult = {
  kind: "nav";
  href: string;
  label: string;
  icon: React.ComponentType<{ className?: string }>;
};

type EmployeeResult = {
  kind: "employee";
  href: string;
  employee: EmployeeResponse;
};

type Result = NavResult | EmployeeResult;

const NAV_LIMIT = 8;
const EMPLOYEE_LIMIT = 8;

export function GlobalSearch({ open, onClose }: { open: boolean; onClose: () => void }) {
  const t = useTranslations("shell");
  const { session } = useSession();
  const pathname = usePathname();
  const router = useAppRouter();

  const [query, setQuery] = React.useState("");
  const [activeIndex, setActiveIndex] = React.useState(0);
  const inputRef = React.useRef<HTMLInputElement>(null);

  // Resolve the active workspace so navigation entries mirror the sidebar.
  const entitled = React.useMemo(
    () => entitledSlugs(session?.user.roles, session?.user.permissions),
    [session?.user.roles, session?.user.permissions],
  );
  const slug = activeSlugFromPath(pathname, entitled);

  const ownedPermissions = React.useMemo(
    () => new Set((session?.user.permissions ?? []).map((p) => p.split("#")[0] ?? p)),
    [session?.user.permissions],
  );

  const canSearchEmployees = ownedPermissions.has("hrm:employee:read");

  // Flatten the role's sidebar into a permission-filtered, deduped list.
  const navEntries = React.useMemo<NavResult[]>(() => {
    const seen = new Set<string>();
    return sidebarForRole(slug)
      .flatMap((section) => section.items)
      .filter((item) => !item.permission || ownedPermissions.has(item.permission))
      .filter((item) => {
        if (seen.has(item.href)) return false;
        seen.add(item.href);
        return true;
      })
      .map((item) => ({
        kind: "nav" as const,
        href: item.href,
        label: t(item.labelKey),
        icon: item.icon,
      }));
  }, [slug, ownedPermissions, t]);

  const employeesQuery = useQuery({
    queryKey: ["hrm", "employees"],
    queryFn: () => apiFetch<EmployeeResponse[]>("/api/hrm/employees"),
    enabled: open && canSearchEmployees,
    staleTime: 120_000,
  });

  const q = query.trim().toLowerCase();

  const navResults = React.useMemo(() => {
    if (!q) return navEntries.slice(0, NAV_LIMIT);
    return navEntries.filter((n) => n.label.toLowerCase().includes(q)).slice(0, NAV_LIMIT);
  }, [navEntries, q]);

  const employeeResults = React.useMemo<EmployeeResult[]>(() => {
    if (!q) return [];
    return (employeesQuery.data ?? [])
      .filter((e) => {
        const haystack = `${e.actorDisplayName ?? ""} ${e.matricule} ${e.departmentCode ?? ""}`.toLowerCase();
        return haystack.includes(q);
      })
      .slice(0, EMPLOYEE_LIMIT)
      .map((employee) => ({ kind: "employee" as const, href: `/employees/${employee.id}`, employee }));
  }, [employeesQuery.data, q]);

  const flatResults = React.useMemo<Result[]>(
    () => [...navResults, ...employeeResults],
    [navResults, employeeResults],
  );

  // Reset state whenever the palette opens, and focus the input.
  React.useEffect(() => {
    if (open) {
      setQuery("");
      setActiveIndex(0);
      const id = window.setTimeout(() => inputRef.current?.focus(), 20);
      return () => window.clearTimeout(id);
    }
  }, [open]);

  // Keep the highlighted row within bounds as results change.
  React.useEffect(() => {
    setActiveIndex((i) => (i >= flatResults.length ? 0 : i));
  }, [flatResults.length]);

  // Lock background scroll while open.
  React.useEffect(() => {
    if (!open) return;
    const previous = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = previous;
    };
  }, [open]);

  const go = React.useCallback(
    (result: Result) => {
      onClose();
      router.push(result.href);
    },
    [onClose, router],
  );

  function onKeyDown(e: React.KeyboardEvent<HTMLInputElement>) {
    if (e.key === "ArrowDown") {
      e.preventDefault();
      setActiveIndex((i) => (flatResults.length === 0 ? 0 : (i + 1) % flatResults.length));
    } else if (e.key === "ArrowUp") {
      e.preventDefault();
      setActiveIndex((i) =>
        flatResults.length === 0 ? 0 : (i - 1 + flatResults.length) % flatResults.length,
      );
    } else if (e.key === "Enter") {
      e.preventDefault();
      const target = flatResults[activeIndex];
      if (target) go(target);
    } else if (e.key === "Escape") {
      e.preventDefault();
      onClose();
    }
  }

  if (!open) return null;

  const isLoading = canSearchEmployees && employeesQuery.isLoading && !!q;
  const hasResults = flatResults.length > 0;

  return (
    <div
      className="fixed inset-0 z-50 flex items-start justify-center px-4 pt-[12vh]"
      role="dialog"
      aria-modal="true"
    >
      <button
        type="button"
        aria-label={t("search.hintClose")}
        onClick={onClose}
        className="absolute inset-0 cursor-default bg-ink/30 backdrop-blur-sm"
      />

      <div className="relative w-full max-w-[600px] overflow-hidden rounded-[16px] border border-line bg-white shadow-lg-brand">
        {/* Search input */}
        <div className="flex items-center gap-2.5 border-b border-line-soft px-4 py-3 text-ink-3">
          <Search className="h-4 w-4 shrink-0" />
          <input
            ref={inputRef}
            type="text"
            value={query}
            onChange={(e) => setQuery(e.target.value)}
            onKeyDown={onKeyDown}
            placeholder={t("search.placeholder")}
            className="flex-1 border-none bg-transparent text-[14px] text-ink outline-none placeholder:text-ink-4"
          />
          {isLoading && <Loader2 className="h-4 w-4 shrink-0 animate-spin text-orange-500" />}
        </div>

        {/* Results */}
        <div className="max-h-[52vh] overflow-y-auto p-2">
          {!hasResults && !isLoading && (
            <p className="px-3 py-8 text-center text-[13px] text-ink-3">
              {t("search.noResults")}
            </p>
          )}

          {navResults.length > 0 && (
            <Section label={t("search.navigation")}>
              {navResults.map((result, i) => {
                const Icon = result.icon;
                return (
                  <Row
                    key={result.href}
                    active={activeIndex === i}
                    onMouseEnter={() => setActiveIndex(i)}
                    onClick={() => go(result)}
                  >
                    <span className="grid h-8 w-8 shrink-0 place-items-center rounded-[9px] bg-bg-soft text-ink-2">
                      <Icon className="h-4 w-4" />
                    </span>
                    <span className="truncate text-[13.5px] font-medium text-ink">{result.label}</span>
                  </Row>
                );
              })}
            </Section>
          )}

          {employeeResults.length > 0 && (
            <Section label={t("search.employees")}>
              {employeeResults.map((result, i) => {
                const index = navResults.length + i;
                const e = result.employee;
                return (
                  <Row
                    key={e.id}
                    active={activeIndex === index}
                    onMouseEnter={() => setActiveIndex(index)}
                    onClick={() => go(result)}
                  >
                    <Avatar
                      name={e.actorDisplayName ?? e.matricule}
                      initials={initials(e.actorDisplayName ?? e.matricule, 2)}
                      size="sm"
                    />
                    <span className="min-w-0 flex-1">
                      <span className="block truncate text-[13.5px] font-medium text-ink">
                        {e.actorDisplayName ?? "—"}
                      </span>
                      <span className="block truncate font-mono-tabular text-[11px] text-ink-3">
                        {e.matricule}
                        {e.departmentCode ? ` · ${e.departmentCode}` : ""}
                      </span>
                    </span>
                  </Row>
                );
              })}
            </Section>
          )}
        </div>

        {/* Footer hints */}
        <div className="flex items-center gap-4 border-t border-line-soft px-4 py-2 text-[11px] text-ink-3">
          <span className="flex items-center gap-1.5">
            <Kbd>↑</Kbd>
            <Kbd>↓</Kbd>
            {t("search.hintNavigate")}
          </span>
          <span className="flex items-center gap-1.5">
            <Kbd>
              <CornerDownLeft className="h-3 w-3" />
            </Kbd>
            {t("search.hintSelect")}
          </span>
          <span className="flex items-center gap-1.5">
            <Kbd>Esc</Kbd>
            {t("search.hintClose")}
          </span>
        </div>
      </div>
    </div>
  );
}

function Section({ label, children }: { label: string; children: React.ReactNode }) {
  return (
    <div className="mb-1">
      <p className="px-3 pb-1 pt-2 text-[10px] font-semibold uppercase tracking-[0.12em] text-ink-4">
        {label}
      </p>
      <div className="flex flex-col gap-0.5">{children}</div>
    </div>
  );
}

function Row({
  active,
  onClick,
  onMouseEnter,
  children,
}: {
  active: boolean;
  onClick: () => void;
  onMouseEnter: () => void;
  children: React.ReactNode;
}) {
  return (
    <button
      type="button"
      onClick={onClick}
      onMouseEnter={onMouseEnter}
      className={cn(
        "flex w-full items-center gap-3 rounded-[10px] px-3 py-2 text-left transition-colors",
        active ? "bg-orange-50" : "hover:bg-bg-soft",
      )}
    >
      {children}
    </button>
  );
}

function Kbd({ children }: { children: React.ReactNode }) {
  return (
    <kbd className="inline-flex h-5 min-w-5 items-center justify-center rounded-md border border-line bg-bg-soft px-1 font-mono-tabular text-[10px] font-semibold text-ink-3">
      {children}
    </kbd>
  );
}
