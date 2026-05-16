"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Link, usePathname } from "@/i18n/navigation";
import {
  LayoutDashboard,
  BarChart3,
  Users,
  FileText,
  Sparkles,
  UserPlus,
  Clock,
  CalendarDays,
  Plane,
  Wallet,
  Coins,
  Receipt,
  Award,
  GraduationCap,
  Calculator,
  Stethoscope,
  ClipboardList,
  Settings,
  type LucideIcon,
} from "lucide-react";
import { cn } from "@/lib/utils";

type NavItem = {
  href: string;
  labelKey: string;
  icon: LucideIcon;
  badge?: number;
};

type NavSection = {
  labelKey: string;
  items: NavItem[];
};

// Section navigation for an Admin RH (default view for Phase 0).
// Future phases will dynamically filter based on user permissions.
const ADMIN_NAV: NavSection[] = [
  {
    labelKey: "sections.pilotage",
    items: [
      { href: "/dashboard", labelKey: "items.dashboard", icon: LayoutDashboard },
      { href: "/analytics", labelKey: "items.analytics", icon: BarChart3 },
    ],
  },
  {
    labelKey: "sections.personnel",
    items: [
      { href: "/employees", labelKey: "items.employees", icon: Users },
      { href: "/contracts", labelKey: "items.contracts", icon: FileText },
      { href: "/skills", labelKey: "items.skills", icon: Sparkles },
      { href: "/recruitment", labelKey: "items.recruitment", icon: UserPlus },
    ],
  },
  {
    labelKey: "sections.activity",
    items: [
      { href: "/timesheets", labelKey: "items.timesheets", icon: Clock },
      { href: "/leaves", labelKey: "items.leaves", icon: CalendarDays },
      { href: "/mission-orders", labelKey: "items.missionOrders", icon: Plane },
    ],
  },
  {
    labelKey: "sections.compensation",
    items: [
      { href: "/payroll", labelKey: "items.payroll", icon: Wallet },
      { href: "/loans", labelKey: "items.loans", icon: Coins },
      { href: "/expenses", labelKey: "items.expenses", icon: Receipt },
    ],
  },
  {
    labelKey: "sections.development",
    items: [
      { href: "/reviews", labelKey: "items.reviews", icon: Award },
      { href: "/trainings", labelKey: "items.trainings", icon: GraduationCap },
      { href: "/training-budgets", labelKey: "items.trainingBudgets", icon: Calculator },
    ],
  },
  {
    labelKey: "sections.compliance",
    items: [
      { href: "/medical", labelKey: "items.medical", icon: Stethoscope },
      { href: "/declarations", labelKey: "items.declarations", icon: ClipboardList },
    ],
  },
  {
    labelKey: "sections.system",
    items: [{ href: "/settings", labelKey: "items.settings", icon: Settings }],
  },
];

export function Sidebar() {
  const tNav = useTranslations("navigation");
  const tCommon = useTranslations("common");
  const pathname = usePathname();

  return (
    <aside
      className="sticky top-0 flex h-screen w-[252px] flex-col gap-1 overflow-y-auto border-r border-line bg-gradient-to-b from-white to-[#FCF8EF] px-3 py-4"
      aria-label="Navigation principale"
    >
      <div className="mb-3 flex items-center gap-3 border-b border-line-soft px-2 pb-5 pt-2">
        <div className="relative grid size-9 place-items-center rounded-xl bg-grad-orange font-display text-[19px] font-extrabold text-white shadow-brand">
          H
        </div>
        <div className="flex flex-col leading-tight">
          <span className="font-display text-[15px] font-bold tracking-tight text-ink">
            {tCommon("settings") /* placeholder, real app name from common.appName */}
          </span>
          <span className="text-[10px] font-semibold uppercase tracking-widest text-ink-3">
            HR Core
          </span>
        </div>
      </div>

      {ADMIN_NAV.map((section) => (
        <div key={section.labelKey} className="flex flex-col gap-0.5">
          <div className="px-3 pb-1.5 pt-3 text-[10px] font-semibold uppercase tracking-[0.14em] text-ink-4">
            {tNav(section.labelKey)}
          </div>
          {section.items.map((item) => {
            const Icon = item.icon;
            const isActive =
              pathname === item.href || pathname.startsWith(item.href + "/");
            return (
              <Link
                key={item.href}
                href={item.href as never}
                className={cn(
                  "relative flex w-full items-center gap-2.5 rounded-[10px] px-3 py-2.5 text-[13.5px] font-medium tracking-tight transition-colors",
                  isActive
                    ? "bg-grad-orange text-white shadow-brand"
                    : "text-ink-2 hover:bg-brand-50 hover:text-brand-700",
                )}
              >
                <Icon className="size-[18px] shrink-0" aria-hidden />
                <span className="grow">{tNav(item.labelKey)}</span>
                {item.badge !== undefined && (
                  <span
                    className={cn(
                      "rounded-full px-2 py-0.5 text-[10px] font-semibold",
                      isActive ? "bg-black/25 text-white" : "bg-cream-soft text-ink-3",
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
    </aside>
  );
}
