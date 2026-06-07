"use client";

import { Check, ChevronsUpDown, LogOut } from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { useSession } from "@/components/providers/session-provider";
import { withRolePrefix } from "@/components/ui/app-link";
import { Avatar } from "@/components/ui/avatar";
import { useCan } from "@/hooks/use-can";
import { Link, usePathname, useRouter } from "@/i18n/navigation";
import { apiFetch } from "@/lib/api-client";
import { activeSlugFromPath, entitledSlugs, isMigratedRole, type RoleSlug } from "@/lib/roles";
import { type NavItem, type NavSection, sidebarForRole } from "@/lib/sidebar";
import { cn } from "@/lib/utils";

export function Sidebar() {
  const t = useTranslations("shell");
  const tCommon = useTranslations("common");
  const tTopbar = useTranslations("shell.topbar");
  const pathname = usePathname();
  const router = useRouter();
  const { session, setSession } = useSession();

  // One account = one role. The role's slug namespaces every link the user can
  // reach and selects the sidebar config in {@link sidebarForRole}. Hrefs are
  // prefixed with the role slug for migrated roles, and left flat (legacy
  // routes) for the rest. Per-item permissions provide an additional gate.
  const entitled = React.useMemo(
    () => entitledSlugs(session?.user.roles, session?.user.permissions),
    [session?.user.roles, session?.user.permissions],
  );
  // The active workspace is the namespace we are currently browsing — this is
  // what lets a multi-role user switch between, say, Employee and Payroll.
  const slug = activeSlugFromPath(pathname, entitled);
  const isEmployee = slug === "employee";
  const prefix = isMigratedRole(slug) ? `/${slug}` : "";
  const rawSections = React.useMemo(() => sidebarForRole(slug), [slug]);
  const sections = React.useMemo(
    () =>
      rawSections.map((section) => ({
        ...section,
        items: section.items.map((item) => ({
          ...item,
          href: withRolePrefix(prefix, item.href),
        })),
      })),
    [rawSections, prefix],
  );
  const homeHref = withRolePrefix(prefix, "/dashboard");
  const firstName = session?.user.firstName ?? session?.user.fullName.split(" ")[0] ?? "";

  async function logout() {
    try {
      await apiFetch("/api/auth/logout", { method: "POST" });
    } catch {
      // ignore — we still clear local state
    }
    setSession(null);
    router.replace("/login");
  }

  return (
    <aside
      className={cn(
        "sticky top-0 z-30 flex h-screen w-[252px] flex-col gap-1 overflow-y-auto",
        "border-r border-line bg-[linear-gradient(180deg,#FFFFFF_0%,#FCF8EF_100%)]",
        "px-3 py-[18px] shadow-[1px_0_0_rgba(15,11,5,0.02)]",
      )}
    >
      <Link
        href={homeHref}
        className="mb-3.5 flex items-center gap-3 border-b border-line-soft px-2 pb-5 pt-2"
      >
        <span
          className={cn(
            "relative grid h-[38px] w-[38px] place-items-center rounded-xl bg-grad-orange",
            "font-display text-[19px] font-extrabold text-white",
            "shadow-[0_0_0_1px_rgba(255,255,255,0.2)_inset,0_6px_16px_-4px_rgba(242,107,15,0.45)]",
          )}
        >
          H
        </span>
        <div className="flex flex-col">
          <span className="font-display text-[15px] font-bold tracking-tight text-ink">
            {tCommon("appName")}
          </span>
          <span className="text-[10px] font-semibold uppercase tracking-widest text-ink-3">
            {tCommon("tagline")}
          </span>
        </div>
      </Link>

      {/* Workspace switcher — only when the user holds more than one space */}
      {entitled.length > 1 && (
        <WorkspaceSwitcher
          entitled={entitled}
          active={slug}
          onSwitch={(target) => router.push(`/${target}/dashboard`)}
          t={t}
        />
      )}

      {/* Employee mini-profile chip */}
      {isEmployee && session?.user && (
        <div className="mb-1 flex items-center gap-2.5 rounded-[14px] border border-orange-100 bg-[linear-gradient(135deg,#FFF4EB_0%,#fff_100%)] px-3 py-2.5">
          <Avatar name={session.user.fullName} size="md" tone="orange" />
          <div className="min-w-0 flex-1">
            <div className="truncate text-[13px] font-bold text-ink">
              {firstName} 👋
            </div>
            <div className="font-mono-tabular truncate text-[11px] text-ink-3">
              {t(`workspaces.${slug}`)}
            </div>
          </div>
        </div>
      )}

      <nav className="flex flex-col gap-0.5">
        {sections.map((section) => (
          <SidebarSection key={section.labelKey} section={section} t={t} pathname={pathname} />
        ))}
      </nav>

      {session?.user && (
        <div className="mt-auto flex items-center gap-2.5 rounded-xl border border-line-soft bg-bg-dim p-3">
          <Avatar name={session.user.fullName} size="md" />
          <div className="flex min-w-0 flex-col">
            <span className="truncate text-[13px] font-semibold text-ink">
              {session.user.fullName}
            </span>
            <span className="truncate text-[11px] text-ink-3">
              {t(`workspaces.${slug}`)}
            </span>
          </div>
          <button
            type="button"
            onClick={logout}
            aria-label={tTopbar("logout")}
            className="ml-auto grid h-7 w-7 place-items-center rounded-md text-ink-3 hover:bg-white hover:text-orange-600"
          >
            <LogOut className="h-4 w-4" />
          </button>
        </div>
      )}
    </aside>
  );
}

function SidebarSection({
  section,
  t,
  pathname,
}: {
  section: NavSection;
  t: ReturnType<typeof useTranslations<"shell">>;
  pathname: string;
}) {
  // Filter the section's items by the current user's permissions
  const visibleItems = section.items.filter((item) => {
    if (!item.permission) return true;
    // We cannot call useCan in a loop here — instead inline-check via a helper component
    return true;
  });
  if (visibleItems.length === 0) return null;
  return (
    <div className="flex flex-col gap-0.5">
      <p className="px-3 pb-1.5 pt-3.5 text-[10px] font-semibold uppercase tracking-[0.14em] text-ink-4">
        {t(section.labelKey)}
      </p>
      {visibleItems.map((item) => (
        <SidebarItem key={item.href} item={item} active={isActive(pathname, item.href)} t={t} />
      ))}
    </div>
  );
}

function SidebarItem({
  item,
  active,
  t,
}: {
  item: NavItem;
  active: boolean;
  t: ReturnType<typeof useTranslations<"shell">>;
}) {
  const tCommon = useTranslations("common.comingSoon");
  const can = useCan(item.permission ?? "");
  if (item.permission && !can) return null;
  const Icon = item.icon;
  return (
    <Link
      href={item.href}
      className={cn(
        "relative flex w-full items-center gap-[11px] rounded-[10px] px-3 py-2.5 text-[13.5px] font-medium tracking-tight transition-colors duration-150",
        !active && "text-ink-2 hover:bg-orange-50 hover:text-orange-700",
        active &&
          "bg-grad-orange font-semibold text-white shadow-[0_0_0_1px_rgba(255,255,255,0.08)_inset,0_8px_24px_-6px_rgba(242,107,15,0.55),0_0_32px_-8px_rgba(242,107,15,0.7)] before:absolute before:inset-px before:rounded-[9px] before:bg-[linear-gradient(180deg,rgba(255,255,255,0.18),transparent_60%)] before:pointer-events-none",
      )}
    >
      <Icon className="relative h-[18px] w-[18px] shrink-0" />
      <span className="relative">{t(item.labelKey)}</span>
      {item.comingSoon && (
        <span
          className={cn(
            "relative ml-auto rounded-full px-1.5 py-0.5 text-[9px] font-semibold uppercase tracking-wider",
            active ? "bg-white/25 text-white" : "bg-orange-100 text-orange-700",
          )}
        >
          {tCommon("pill")}
        </span>
      )}
    </Link>
  );
}

function isActive(pathname: string, href: string): boolean {
  return pathname === href || pathname.startsWith(`${href}/`);
}

/**
 * Lets a multi-role user jump between their workspaces (e.g. Employee ⇄ Payroll
 * Manager). Hidden for plain employees, who hold a single space.
 */
function WorkspaceSwitcher({
  entitled,
  active,
  onSwitch,
  t,
}: {
  entitled: RoleSlug[];
  active: RoleSlug;
  onSwitch: (target: RoleSlug) => void;
  t: ReturnType<typeof useTranslations<"shell">>;
}) {
  const [open, setOpen] = React.useState(false);
  const ref = React.useRef<HTMLDivElement>(null);

  React.useEffect(() => {
    if (!open) return;
    function onDocClick(e: MouseEvent) {
      if (ref.current && !ref.current.contains(e.target as Node)) setOpen(false);
    }
    document.addEventListener("mousedown", onDocClick);
    return () => document.removeEventListener("mousedown", onDocClick);
  }, [open]);

  return (
    <div ref={ref} className="relative mb-1.5">
      <button
        type="button"
        onClick={() => setOpen((v) => !v)}
        className={cn(
          "flex w-full items-center gap-2.5 rounded-[12px] border border-line bg-white px-3 py-2.5",
          "text-left transition-colors hover:border-line-strong",
        )}
      >
        <span className="grid h-7 w-7 shrink-0 place-items-center rounded-[8px] bg-grad-orange text-white">
          <ChevronsUpDown className="h-3.5 w-3.5" />
        </span>
        <span className="min-w-0 flex-1">
          <span className="block text-[9px] font-semibold uppercase tracking-[0.1em] text-ink-4">
            {t("switcher.label")}
          </span>
          <span className="block truncate text-[12.5px] font-semibold text-ink">
            {t(`workspaces.${active}`)}
          </span>
        </span>
      </button>

      {open && (
        <div
          className={cn(
            "absolute left-0 right-0 top-[calc(100%+4px)] z-40 overflow-hidden rounded-[12px]",
            "border border-line bg-white p-1 shadow-md-brand",
          )}
        >
          {entitled.map((s) => (
            <button
              key={s}
              type="button"
              onClick={() => {
                setOpen(false);
                if (s !== active) onSwitch(s);
              }}
              className={cn(
                "flex w-full items-center gap-2 rounded-[9px] px-2.5 py-2 text-[12.5px] transition-colors",
                s === active
                  ? "bg-orange-50 font-semibold text-orange-700"
                  : "text-ink-2 hover:bg-bg-soft",
              )}
            >
              <span className="truncate">{t(`workspaces.${s}`)}</span>
              {s === active && <Check className="ml-auto h-3.5 w-3.5 shrink-0" />}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
