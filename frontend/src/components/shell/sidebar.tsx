"use client";

import {
  Bell,
  Briefcase,
  Building2,
  CalendarRange,
  ChartLine,
  ChartPie,
  ClipboardList,
  Clock,
  Coins,
  FileSearch,
  FileText,
  GraduationCap,
  HeartPulse,
  LayoutDashboard,
  LogOut,
  Map,
  Settings,
  ShieldCheck,
  Sparkles,
  Stethoscope,
  Target,
  Users,
  Wallet,
} from "lucide-react";
import { useTranslations } from "next-intl";
import * as React from "react";

import { useSession } from "@/components/providers/session-provider";
import { Avatar } from "@/components/ui/avatar";
import { useCan } from "@/hooks/use-can";
import { Link, usePathname, useRouter } from "@/i18n/navigation";
import { apiFetch } from "@/lib/api-client";
import { cn } from "@/lib/utils";

type NavItem = {
  href: string;
  labelKey: string;
  icon: React.ComponentType<{ className?: string }>;
  permission?: string;
};

type NavSection = {
  labelKey: string;
  items: NavItem[];
};

// ── Employee self-service navigation (matches pages-employee design) ─────────
const EMP_SECTIONS: NavSection[] = [
  {
    labelKey: "sections.personal",
    items: [
      { href: "/dashboard",  labelKey: "nav.myDashboard",  icon: LayoutDashboard },
      { href: "/profile",    labelKey: "nav.myProfile",    icon: Users },
      { href: "/documents",  labelKey: "nav.myDocuments",  icon: FileText },
    ],
  },
  {
    labelKey: "sections.compensation",
    items: [
      { href: "/payslips",      labelKey: "nav.myPayslips", icon: Wallet },
      { href: "/expenses",      labelKey: "nav.myExpenses", icon: ClipboardList, permission: "hrm:expense:read" },
      { href: "/loans",         labelKey: "nav.myLoans",    icon: Coins },
    ],
  },
  {
    labelKey: "sections.activity",
    items: [
      { href: "/leaves",         labelKey: "nav.myLeaves",   icon: CalendarRange, permission: "hrm:leave:read" },
      { href: "/timesheets",     labelKey: "nav.myTime",     icon: Clock,         permission: "hrm:timesheet:read" },
      { href: "/mission-orders", labelKey: "nav.myMissions", icon: Map,           permission: "hrm:mission:read" },
    ],
  },
  {
    labelKey: "sections.development",
    items: [
      { href: "/trainings", labelKey: "nav.myTrainings", icon: GraduationCap, permission: "hrm:training:read" },
      { href: "/reviews",   labelKey: "nav.myReviews",   icon: Target,        permission: "hrm:review:read" },
      { href: "/skills",    labelKey: "nav.mySkills",    icon: Sparkles,      permission: "hrm:skill:read" },
    ],
  },
  {
    labelKey: "sections.health",
    items: [
      { href: "/medical", labelKey: "nav.myMedical", icon: Stethoscope, permission: "hrm:medical:read" },
    ],
  },
  {
    labelKey: "sections.company",
    items: [
      { href: "/employees", labelKey: "nav.directory", icon: Users, permission: "hrm:employee:read" },
    ],
  },
];

// ── HR manager navigation ─────────────────────────────────────────────────────
const SECTIONS: NavSection[] = [
  {
    labelKey: "sections.pilotage",
    items: [
      { href: "/dashboard", labelKey: "nav.dashboard", icon: LayoutDashboard },
      { href: "/analytics", labelKey: "nav.analytics", icon: ChartLine, permission: "hrm:kpi:read" },
    ],
  },
  {
    labelKey: "sections.personnel",
    items: [
      { href: "/employees", labelKey: "nav.employees", icon: Users, permission: "hrm:employee:read" },
      { href: "/contracts", labelKey: "nav.contracts", icon: FileText, permission: "hrm:contract:read" },
      { href: "/skills", labelKey: "nav.skills", icon: Sparkles, permission: "hrm:skill:read" },
      { href: "/recruitment", labelKey: "nav.recruitment", icon: Briefcase, permission: "hrm:recruitment:read" },
    ],
  },
  {
    labelKey: "sections.activity",
    items: [
      { href: "/timesheets", labelKey: "nav.time", icon: Clock, permission: "hrm:timesheet:read" },
      { href: "/leaves", labelKey: "nav.leaves", icon: CalendarRange, permission: "hrm:leave:read" },
      { href: "/mission-orders", labelKey: "nav.missions", icon: Map, permission: "hrm:mission:read" },
    ],
  },
  {
    labelKey: "sections.compensation",
    items: [
      { href: "/payroll", labelKey: "nav.payroll", icon: Wallet, permission: "hrm:payroll:read" },
      { href: "/loans", labelKey: "nav.loans", icon: Coins, permission: "hrm:loan:read" },
      { href: "/expenses", labelKey: "nav.expenses", icon: ClipboardList, permission: "hrm:expense:read" },
    ],
  },
  {
    labelKey: "sections.development",
    items: [
      { href: "/reviews", labelKey: "nav.reviews", icon: Target, permission: "hrm:review:read" },
      { href: "/trainings", labelKey: "nav.trainings", icon: GraduationCap, permission: "hrm:training:read" },
      { href: "/training-budgets", labelKey: "nav.budget", icon: ChartPie, permission: "hrm:budget:read" },
    ],
  },
  {
    labelKey: "sections.compliance",
    items: [
      { href: "/medical", labelKey: "nav.medical", icon: Stethoscope, permission: "hrm:medical:read" },
      { href: "/declarations", labelKey: "nav.declarations", icon: ShieldCheck, permission: "hrm:declaration:read" },
    ],
  },
  {
    labelKey: "sections.system",
    items: [{ href: "/settings", labelKey: "nav.settings", icon: Settings }],
  },
  {
    labelKey: "sections.administration",
    items: [
      { href: "/admin", labelKey: "nav.administration", icon: Building2, permission: "tenant:admin" },
      { href: "/admin/users", labelKey: "nav.users", icon: Users, permission: "tenant:admin" },
      {
        href: "/admin/roles",
        labelKey: "nav.roles",
        icon: ShieldCheck,
        permission: "administration:roles:read",
      },
      {
        href: "/admin/audit",
        labelKey: "nav.audit",
        icon: FileSearch,
        permission: "administration:audit:read",
      },
    ],
  },
];

export function Sidebar() {
  const t = useTranslations("shell");
  const tCommon = useTranslations("common");
  const tTopbar = useTranslations("shell.topbar");
  const pathname = usePathname();
  const router = useRouter();
  const { session, setSession } = useSession();
  const isHrManager = useCan(["hrm:leave:approve", "hrm:employee:create"]);

  const sections = isHrManager ? SECTIONS : EMP_SECTIONS;
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
        href="/dashboard"
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

      {/* Employee mini-profile chip */}
      {!isHrManager && session?.user && (
        <div className="mb-1 flex items-center gap-2.5 rounded-[14px] border border-orange-100 bg-[linear-gradient(135deg,#FFF4EB_0%,#fff_100%)] px-3 py-2.5">
          <Avatar name={session.user.fullName} size="md" tone="orange" />
          <div className="min-w-0 flex-1">
            <div className="truncate text-[13px] font-bold text-ink">
              {firstName} 👋
            </div>
            <div className="font-mono-tabular truncate text-[11px] text-ink-3">
              {session.user.roles[0] ?? "Employé"}
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
              {session.user.roles[0] ?? "—"}
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
    </Link>
  );
}

function isActive(pathname: string, href: string): boolean {
  return pathname === href || pathname.startsWith(`${href}/`);
}
