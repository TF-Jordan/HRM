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
  Shield,
  UserCog,
  type LucideIcon,
} from "lucide-react";
import { cn } from "@/lib/utils";
import { usePermissions, hasAnyPermission } from "@/hooks/useSession";

type NavItem = {
  href: string;
  labelKey: string;
  icon: LucideIcon;
  badge?: number;
  /**
   * Permissions required to see this item (OR semantics). Empty/undefined
   * means visible to any authenticated user.
   */
  requiredAnyPerm?: string[];
};

type NavSection = {
  labelKey: string;
  items: NavItem[];
};

const NAV: NavSection[] = [
  {
    labelKey: "sections.pilotage",
    items: [
      { href: "/dashboard", labelKey: "items.dashboard", icon: LayoutDashboard },
      { href: "/analytics", labelKey: "items.analytics", icon: BarChart3, requiredAnyPerm: ["hrm:kpi:read"] },
    ],
  },
  {
    labelKey: "sections.personnel",
    items: [
      { href: "/employees", labelKey: "items.employees", icon: Users, requiredAnyPerm: ["hrm:employee:read"] },
      { href: "/contracts", labelKey: "items.contracts", icon: FileText, requiredAnyPerm: ["hrm:contract:read"] },
      { href: "/skills", labelKey: "items.skills", icon: Sparkles, requiredAnyPerm: ["hrm:skill:read"] },
      { href: "/recruitment", labelKey: "items.recruitment", icon: UserPlus, requiredAnyPerm: ["hrm:recruitment:read"] },
    ],
  },
  {
    labelKey: "sections.activity",
    items: [
      { href: "/timesheets", labelKey: "items.timesheets", icon: Clock, requiredAnyPerm: ["hrm:timesheet:validate"] },
      { href: "/leaves/pending", labelKey: "items.leaves", icon: CalendarDays, requiredAnyPerm: ["hrm:leave:approve"] },
      { href: "/mission-orders", labelKey: "items.missionOrders", icon: Plane, requiredAnyPerm: ["hrm:mission:read"] },
    ],
  },
  {
    labelKey: "sections.compensation",
    items: [
      { href: "/payroll", labelKey: "items.payroll", icon: Wallet, requiredAnyPerm: ["hrm:payroll:run", "hrm:payroll:validate"] },
      { href: "/loans/approve", labelKey: "items.loans", icon: Coins, requiredAnyPerm: ["hrm:loan:approve"] },
      { href: "/expenses/approve", labelKey: "items.expenses", icon: Receipt, requiredAnyPerm: ["hrm:expense:manage"] },
    ],
  },
  {
    labelKey: "sections.development",
    items: [
      { href: "/reviews", labelKey: "items.reviews", icon: Award, requiredAnyPerm: ["hrm:review:read"] },
      { href: "/trainings", labelKey: "items.trainings", icon: GraduationCap, requiredAnyPerm: ["hrm:training:read"] },
      { href: "/training-budgets", labelKey: "items.trainingBudgets", icon: Calculator, requiredAnyPerm: ["hrm:budget:read"] },
    ],
  },
  {
    labelKey: "sections.compliance",
    items: [
      { href: "/medical", labelKey: "items.medical", icon: Stethoscope, requiredAnyPerm: ["hrm:medical:read"] },
      { href: "/declarations", labelKey: "items.declarations", icon: ClipboardList, requiredAnyPerm: ["hrm:declaration:read"] },
    ],
  },
  {
    labelKey: "sections.selfService",
    items: [
      { href: "/me/profile", labelKey: "items.profile", icon: Settings },
      { href: "/me/payslips", labelKey: "items.payslips", icon: Wallet, requiredAnyPerm: ["hrm:payroll:read"] },
      { href: "/leaves/my", labelKey: "items.myLeaves", icon: CalendarDays, requiredAnyPerm: ["hrm:leave:create"] },
      { href: "/loans/my", labelKey: "items.myLoans", icon: Coins, requiredAnyPerm: ["hrm:loan:create"] },
      { href: "/expenses/my", labelKey: "items.myExpenses", icon: Receipt, requiredAnyPerm: ["hrm:expense:create"] },
    ],
  },
  {
    labelKey: "sections.administration",
    items: [
      { href: "/admin/users", labelKey: "items.adminUsers", icon: UserCog, requiredAnyPerm: ["iam:admin"] },
      { href: "/admin/roles", labelKey: "items.adminRoles", icon: Shield, requiredAnyPerm: ["iam:admin"] },
      { href: "/settings", labelKey: "items.settings", icon: Settings, requiredAnyPerm: ["tenant:admin"] },
    ],
  },
];

export function Sidebar() {
  const tNav = useTranslations("navigation");
  const tCommon = useTranslations("common");
  const pathname = usePathname();
  const perms = usePermissions();

  const visibleSections = NAV.map((section) => ({
    ...section,
    items: section.items.filter((item) =>
      hasAnyPermission(perms, item.requiredAnyPerm ?? []),
    ),
  })).filter((section) => section.items.length > 0);

  return (
    <aside
      className="sticky top-0 flex h-screen w-[252px] flex-col gap-1 overflow-y-auto border-r border-line bg-gradient-to-b from-white to-[#FCF8EF] px-3 py-4 dark:border-dark-line dark:from-dark dark:to-dark-2"
      aria-label="Navigation principale"
    >
      <div className="mb-3 flex items-center gap-3 border-b border-line-soft px-2 pb-5 pt-2 dark:border-dark-line">
        <div className="relative grid size-9 place-items-center rounded-xl bg-grad-orange font-display text-[19px] font-extrabold text-white shadow-brand">
          H
        </div>
        <div className="flex flex-col leading-tight">
          <span className="font-display text-[15px] font-bold tracking-tight text-ink">
            {tCommon("appName")}
          </span>
          <span className="text-[10px] font-semibold uppercase tracking-widest text-ink-3">
            {tCommon("appTagline")}
          </span>
        </div>
      </div>

      {visibleSections.map((section) => (
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
                    : "text-ink-2 hover:bg-brand-50 hover:text-brand-700 dark:hover:bg-dark-3",
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
