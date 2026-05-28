"use client";

import {
  Bell,
  BookOpen,
  Briefcase,
  Building2,
  CalendarRange,
  ChartLine,
  ChartPie,
  CheckSquare,
  ClipboardList,
  Clock,
  Coins,
  FileBadge,
  FileText,
  GraduationCap,
  HeartPulse,
  LayoutDashboard,
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

import { Avatar } from "@/components/ui/avatar";
import { Link, usePathname } from "@/i18n/navigation";
import { cn, initials } from "@/lib/utils";

type NavItem = {
  href: string;
  labelKey: string;
  icon: React.ComponentType<{ className?: string }>;
  badge?: string;
};

type NavSection = {
  labelKey: string;
  items: NavItem[];
};

const SECTIONS: NavSection[] = [
  {
    labelKey: "sections.pilotage",
    items: [
      { href: "/dashboard", labelKey: "nav.dashboard", icon: LayoutDashboard },
      { href: "/analytics", labelKey: "nav.analytics", icon: ChartLine },
    ],
  },
  {
    labelKey: "sections.personnel",
    items: [
      { href: "/employees", labelKey: "nav.employees", icon: Users },
      { href: "/contracts", labelKey: "nav.contracts", icon: FileText },
      { href: "/skills", labelKey: "nav.skills", icon: Sparkles },
      { href: "/recruitment", labelKey: "nav.recruitment", icon: Briefcase },
    ],
  },
  {
    labelKey: "sections.activity",
    items: [
      { href: "/timesheets", labelKey: "nav.time", icon: Clock },
      { href: "/leaves", labelKey: "nav.leaves", icon: CalendarRange },
      { href: "/mission-orders", labelKey: "nav.missions", icon: Map },
    ],
  },
  {
    labelKey: "sections.compensation",
    items: [
      { href: "/payroll", labelKey: "nav.payroll", icon: Wallet },
      { href: "/loans", labelKey: "nav.loans", icon: Coins },
      { href: "/expenses", labelKey: "nav.expenses", icon: ClipboardList },
    ],
  },
  {
    labelKey: "sections.development",
    items: [
      { href: "/reviews", labelKey: "nav.reviews", icon: Target },
      { href: "/trainings", labelKey: "nav.trainings", icon: GraduationCap },
      { href: "/training-budgets", labelKey: "nav.budget", icon: ChartPie },
    ],
  },
  {
    labelKey: "sections.compliance",
    items: [
      { href: "/medical", labelKey: "nav.medical", icon: Stethoscope },
      { href: "/declarations", labelKey: "nav.declarations", icon: ShieldCheck },
    ],
  },
  {
    labelKey: "sections.system",
    items: [{ href: "/settings", labelKey: "nav.settings", icon: Settings }],
  },
];

export function Sidebar({ user }: { user?: { name: string; role: string } }) {
  const t = useTranslations("shell");
  const tCommon = useTranslations("common");
  const pathname = usePathname();

  return (
    <aside
      className={cn(
        "sticky top-0 z-30 flex h-screen w-[252px] flex-col gap-1 overflow-y-auto",
        "border-r border-line bg-[linear-gradient(180deg,#FFFFFF_0%,#FCF8EF_100%)]",
        "px-3 py-[18px] shadow-[1px_0_0_rgba(15,11,5,0.02)]",
      )}
    >
      {/* Brand */}
      <Link
        href="/dashboard"
        className="mb-3.5 flex items-center gap-3 border-b border-line-soft px-2 pb-5 pt-2"
      >
        <span
          className={cn(
            "relative grid h-[38px] w-[38px] place-items-center rounded-xl bg-grad-orange",
            "font-display text-[19px] font-extrabold text-white",
            "shadow-[0_0_0_1px_rgba(255,255,255,0.2)_inset,0_6px_16px_-4px_rgba(242,107,15,0.45)]",
            "before:absolute before:inset-px before:rounded-[11px]",
            "before:bg-[linear-gradient(180deg,rgba(255,255,255,0.25),transparent_50%)] before:pointer-events-none",
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

      {/* Nav */}
      <nav className="flex flex-col gap-0.5">
        {SECTIONS.map((section) => (
          <div key={section.labelKey} className="flex flex-col gap-0.5">
            <p className="px-3 pb-1.5 pt-3.5 text-[10px] font-semibold uppercase tracking-[0.14em] text-ink-4">
              {t(section.labelKey)}
            </p>
            {section.items.map((item) => {
              const isActive = pathname === item.href || pathname.startsWith(`${item.href}/`);
              const Icon = item.icon;
              return (
                <Link
                  key={item.href}
                  href={item.href}
                  className={cn(
                    "relative flex w-full items-center gap-[11px] rounded-[10px] px-3 py-2.5 text-[13.5px] font-medium tracking-tight transition-colors duration-150",
                    !isActive && "text-ink-2 hover:bg-orange-50 hover:text-orange-700",
                    isActive &&
                      "bg-grad-orange font-semibold text-white shadow-[0_0_0_1px_rgba(255,255,255,0.08)_inset,0_8px_24px_-6px_rgba(242,107,15,0.55),0_0_32px_-8px_rgba(242,107,15,0.7)] before:absolute before:inset-px before:rounded-[9px] before:bg-[linear-gradient(180deg,rgba(255,255,255,0.18),transparent_60%)] before:pointer-events-none",
                  )}
                >
                  <Icon className="relative h-[18px] w-[18px] shrink-0" />
                  <span className="relative">{t(item.labelKey)}</span>
                  {item.badge && (
                    <span
                      className={cn(
                        "relative ml-auto min-w-5 rounded-full px-1.5 text-center text-[10px] font-semibold",
                        !isActive && "bg-bg-soft text-ink-3",
                        isActive && "bg-black/20 text-white",
                      )}
                    >
                      {item.badge}
                    </span>
                  )}
                </Link>
              );
            })}
          </div>
        ))}
      </nav>

      {/* User card */}
      {user && (
        <div className="mt-auto flex items-center gap-2.5 rounded-xl border border-line-soft bg-bg-dim p-3">
          <Avatar name={user.name} size="md" initials={initials(user.name)} />
          <div className="flex min-w-0 flex-col">
            <span className="truncate text-[13px] font-semibold text-ink">{user.name}</span>
            <span className="truncate text-[11px] text-ink-3">{user.role}</span>
          </div>
          <Bell className="ml-auto h-4 w-4 shrink-0 text-ink-3" aria-hidden="true" />
        </div>
      )}
    </aside>
  );
}
