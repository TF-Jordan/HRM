import type { LucideIcon } from "lucide-react";
import {
  BookText,
  Briefcase,
  CalendarRange,
  ChartLine,
  ChartPie,
  CheckSquare,
  ClipboardList,
  Clock,
  Coins,
  FileText,
  Gauge,
  GraduationCap,
  HeartPulse,
  LayoutDashboard,
  Map,
  ShieldCheck,
  Sparkles,
  Stethoscope,
  Target,
  Users,
  Wallet,
} from "lucide-react";

import type { RoleSlug } from "@/lib/roles";

/**
 * Sidebar configuration — single source of truth for what each role sees.
 *
 * Hrefs are role-relative (e.g. {@code /leaves}) — the runtime prepends the active role slug.
 *
 * Items flagged {@link NavItem.comingSoon} render as a placeholder page when clicked
 * (no 404) and carry a "Bientôt" pill in the sidebar. They are listed so each role's
 * sidebar reflects the full target experience, even before every page is built.
 */
export type NavItem = {
  /** Role-relative href; runtime prepends the role slug. */
  href: string;
  /** i18n key under {@code shell.nav.<key>}. */
  labelKey: string;
  icon: LucideIcon;
  /** Optional permission gate; the item is hidden when the session lacks it. */
  permission?: string;
  /** Routes to the shared placeholder page instead of a real screen. */
  comingSoon?: boolean;
};

export type NavSection = {
  /** i18n key under {@code shell.sections.<key>}. */
  labelKey: string;
  items: NavItem[];
};

// ──────────────────────────────────────────────────────────────────────────────
// EMPLOYEE — self-service only
// ──────────────────────────────────────────────────────────────────────────────
const EMPLOYEE_SECTIONS: NavSection[] = [
  {
    labelKey: "sections.personal",
    items: [
      { href: "/dashboard", labelKey: "nav.myDashboard", icon: LayoutDashboard },
      { href: "/profile", labelKey: "nav.myProfile", icon: Users },
    ],
  },
  {
    labelKey: "sections.compensation",
    items: [
      { href: "/payslips", labelKey: "nav.myPayslips", icon: Wallet },
      {
        href: "/expenses",
        labelKey: "nav.myExpenses",
        icon: ClipboardList,
        permission: "hrm:expense:read",
      },
      { href: "/loans", labelKey: "nav.myLoans", icon: Coins, permission: "hrm:loan:read" },
    ],
  },
  {
    labelKey: "sections.activity",
    items: [
      {
        href: "/leaves",
        labelKey: "nav.myLeaves",
        icon: CalendarRange,
        permission: "hrm:leave:read",
      },
      { href: "/timesheets", labelKey: "nav.myTime", icon: Clock, permission: "hrm:timesheet:read" },
      {
        href: "/mission-orders",
        labelKey: "nav.myMissions",
        icon: Map,
        permission: "hrm:mission:read",
      },
    ],
  },
  {
    labelKey: "sections.development",
    items: [
      {
        href: "/trainings",
        labelKey: "nav.myTrainings",
        icon: GraduationCap,
        permission: "hrm:training:read",
      },
      { href: "/reviews", labelKey: "nav.myReviews", icon: Target, permission: "hrm:review:read" },
      { href: "/skills", labelKey: "nav.mySkills", icon: Sparkles, permission: "hrm:skill:read" },
    ],
  },
  {
    labelKey: "sections.health",
    items: [
      {
        href: "/medical",
        labelKey: "nav.myMedical",
        icon: Stethoscope,
        permission: "hrm:medical:read",
      },
    ],
  },
];

// ──────────────────────────────────────────────────────────────────────────────
// HR ADMIN — full operational scope (includes former DRH, Manager, Recruiter)
// ──────────────────────────────────────────────────────────────────────────────
const HR_ADMIN_SECTIONS: NavSection[] = [
  {
    labelKey: "sections.pilotage",
    items: [
      { href: "/dashboard", labelKey: "nav.dashboard", icon: LayoutDashboard },
      { href: "/analytics", labelKey: "nav.analytics", icon: ChartLine, comingSoon: true },
      { href: "/kpi", labelKey: "nav.kpi", icon: Gauge, comingSoon: true },
    ],
  },
  {
    labelKey: "sections.personnel",
    items: [
      {
        href: "/employees",
        labelKey: "nav.employees",
        icon: Users,
        permission: "hrm:employee:read",
      },
      {
        href: "/contracts",
        labelKey: "nav.contracts",
        icon: FileText,
        permission: "hrm:contract:read",
      },
      { href: "/skills", labelKey: "nav.skills", icon: Sparkles, permission: "hrm:skill:read" },
      {
        href: "/recruitment",
        labelKey: "nav.recruitment",
        icon: Briefcase,
        permission: "hrm:recruitment:read",
      },
      {
        href: "/recruitment/onboarding",
        labelKey: "nav.onboarding",
        icon: CheckSquare,
        permission: "hrm:onboarding:read",
      },
    ],
  },
  {
    labelKey: "sections.activity",
    items: [
      {
        href: "/leaves",
        labelKey: "nav.leaves",
        icon: CalendarRange,
        permission: "hrm:leave:read",
      },
      {
        href: "/timesheets",
        labelKey: "nav.time",
        icon: Clock,
        permission: "hrm:timesheet:read",
      },
      {
        href: "/mission-orders",
        labelKey: "nav.missions",
        icon: Map,
        permission: "hrm:mission:read",
      },
    ],
  },
  {
    labelKey: "sections.compensation",
    items: [
      {
        href: "/payroll",
        labelKey: "nav.payrollOverview",
        icon: Wallet,
        permission: "hrm:payroll:read",
      },
      { href: "/loans", labelKey: "nav.loans", icon: Coins, permission: "hrm:loan:read" },
      {
        href: "/expenses",
        labelKey: "nav.expenses",
        icon: ClipboardList,
        permission: "hrm:expense:read",
      },
    ],
  },
  {
    labelKey: "sections.development",
    items: [
      { href: "/reviews", labelKey: "nav.reviews", icon: Target, permission: "hrm:review:read" },
      {
        href: "/trainings",
        labelKey: "nav.trainings",
        icon: GraduationCap,
        permission: "hrm:training:read",
      },
      {
        href: "/training-budgets",
        labelKey: "nav.budget",
        icon: ChartPie,
        permission: "hrm:budget:read",
      },
    ],
  },
  {
    labelKey: "sections.compliance",
    items: [
      {
        href: "/medical",
        labelKey: "nav.medical",
        icon: Stethoscope,
        permission: "hrm:medical:read",
      },
      {
        href: "/declarations",
        labelKey: "nav.declarations",
        icon: ShieldCheck,
        permission: "hrm:declaration:read",
      },
    ],
  },
];

// ──────────────────────────────────────────────────────────────────────────────
// PAYROLL MANAGER — pay-centric workspace
// ──────────────────────────────────────────────────────────────────────────────
const PAYROLL_MANAGER_SECTIONS: NavSection[] = [
  {
    labelKey: "sections.pilotage",
    items: [{ href: "/dashboard", labelKey: "nav.dashboard", icon: LayoutDashboard }],
  },
  {
    labelKey: "sections.payrollCycle",
    items: [
      { href: "/payroll", labelKey: "nav.runs", icon: Wallet, permission: "hrm:payroll:read" },
      {
        href: "/payroll-employees",
        labelKey: "nav.payrollEmployees",
        icon: Users,
        permission: "hrm:payroll:read",
      },
      {
        href: "/payroll-variables",
        labelKey: "nav.variables",
        icon: ClipboardList,
        permission: "hrm:payroll:run",
      },
      {
        href: "/retroactive",
        labelKey: "nav.retroactive",
        icon: Target,
        permission: "hrm:payroll:run",
      },
    ],
  },
  {
    labelKey: "sections.payrollConfig",
    items: [
      {
        href: "/pay-elements",
        labelKey: "nav.payElements",
        icon: Target,
        permission: "hrm:payroll:run",
      },
      {
        href: "/tax-brackets",
        labelKey: "nav.taxBrackets",
        icon: Target,
        permission: "hrm:payroll:run",
      },
    ],
  },
  {
    labelKey: "sections.payrollOutput",
    items: [
      {
        href: "/documents",
        labelKey: "nav.documents",
        icon: FileText,
        permission: "hrm:payroll:read",
      },
      {
        href: "/declarations-payroll",
        labelKey: "nav.declarations",
        icon: ShieldCheck,
        permission: "hrm:payroll:read",
      },
    ],
  },
  {
    labelKey: "sections.payrollSpecial",
    items: [
      {
        href: "/final-settlements",
        labelKey: "nav.finalSettlements",
        icon: FileText,
        permission: "hrm:payroll:run",
      },
      {
        href: "/garnishments",
        labelKey: "nav.garnishments",
        icon: Target,
        permission: "hrm:payroll:run",
      },
    ],
  },
];

// ──────────────────────────────────────────────────────────────────────────────
// DOCTOR — medical only
// ──────────────────────────────────────────────────────────────────────────────
const DOCTOR_SECTIONS: NavSection[] = [
  {
    labelKey: "sections.pilotage",
    items: [{ href: "/dashboard", labelKey: "nav.dashboard", icon: LayoutDashboard }],
  },
  {
    labelKey: "sections.health",
    items: [
      {
        href: "/medical",
        labelKey: "nav.medical",
        icon: Stethoscope,
        permission: "hrm:medical:read",
      },
      {
        href: "/medical/visits/new",
        labelKey: "nav.newVisit",
        icon: HeartPulse,
        permission: "hrm:medical:create",
      },
      {
        href: "/medical/certificates/new",
        labelKey: "nav.newCertificate",
        icon: BookText,
        permission: "hrm:medical:create",
      },
    ],
  },
  {
    labelKey: "sections.personnel",
    items: [
      {
        href: "/employees",
        labelKey: "nav.directory",
        icon: Users,
        permission: "hrm:employee:read",
      },
    ],
  },
];

// ──────────────────────────────────────────────────────────────────────────────
// CONTROLLER — broad reads, KPI focus
// ──────────────────────────────────────────────────────────────────────────────
const CONTROLLER_SECTIONS: NavSection[] = [
  {
    labelKey: "sections.pilotage",
    items: [
      { href: "/dashboard", labelKey: "nav.dashboard", icon: LayoutDashboard },
      {
        href: "/analytics",
        labelKey: "nav.analytics",
        icon: ChartLine,
        permission: "hrm:kpi:read",
        comingSoon: true,
      },
      { href: "/kpi", labelKey: "nav.kpi", icon: Gauge, permission: "hrm:kpi:read", comingSoon: true },
    ],
  },
  {
    labelKey: "sections.personnel",
    items: [
      {
        href: "/employees",
        labelKey: "nav.employees",
        icon: Users,
        permission: "hrm:employee:read",
      },
      {
        href: "/contracts",
        labelKey: "nav.contracts",
        icon: FileText,
        permission: "hrm:contract:read",
      },
      { href: "/skills", labelKey: "nav.skills", icon: Sparkles, permission: "hrm:skill:read" },
      {
        href: "/recruitment",
        labelKey: "nav.recruitment",
        icon: Briefcase,
        permission: "hrm:recruitment:read",
      },
    ],
  },
  {
    labelKey: "sections.activity",
    items: [
      {
        href: "/leaves",
        labelKey: "nav.leaves",
        icon: CalendarRange,
        permission: "hrm:leave:read",
      },
      {
        href: "/timesheets",
        labelKey: "nav.time",
        icon: Clock,
        permission: "hrm:timesheet:read",
      },
      {
        href: "/mission-orders",
        labelKey: "nav.missions",
        icon: Map,
        permission: "hrm:mission:read",
      },
    ],
  },
  {
    labelKey: "sections.compensation",
    items: [
      {
        href: "/payroll",
        labelKey: "nav.payrollOverview",
        icon: Wallet,
        permission: "hrm:payroll:read",
      },
      { href: "/loans", labelKey: "nav.loans", icon: Coins, permission: "hrm:loan:read" },
      {
        href: "/expenses",
        labelKey: "nav.expenses",
        icon: ClipboardList,
        permission: "hrm:expense:read",
      },
    ],
  },
  {
    labelKey: "sections.development",
    items: [
      { href: "/reviews", labelKey: "nav.reviews", icon: Target, permission: "hrm:review:read" },
      {
        href: "/trainings",
        labelKey: "nav.trainings",
        icon: GraduationCap,
        permission: "hrm:training:read",
      },
      {
        href: "/training-budgets",
        labelKey: "nav.budget",
        icon: ChartPie,
        permission: "hrm:budget:read",
      },
    ],
  },
  {
    labelKey: "sections.compliance",
    items: [
      {
        href: "/medical",
        labelKey: "nav.medical",
        icon: Stethoscope,
        permission: "hrm:medical:read",
      },
      {
        href: "/declarations",
        labelKey: "nav.declarations",
        icon: ShieldCheck,
        permission: "hrm:declaration:read",
      },
    ],
  },
];

// ──────────────────────────────────────────────────────────────────────────────
// SUPER ADMIN / ORG ADMIN — company administration (system, not day-to-day HR)
// ──────────────────────────────────────────────────────────────────────────────
const ADMIN_SECTIONS: NavSection[] = [
  {
    labelKey: "sections.pilotage",
    items: [{ href: "/dashboard", labelKey: "nav.dashboard", icon: LayoutDashboard }],
  },
  {
    labelKey: "sections.access",
    items: [
      { href: "/role-assignments", labelKey: "nav.roleAssignments", icon: Users },
      { href: "/users", labelKey: "nav.users", icon: Users },
      { href: "/roles", labelKey: "nav.roles", icon: ShieldCheck },
    ],
  },
  {
    labelKey: "sections.compliance",
    items: [{ href: "/audit", labelKey: "nav.audit", icon: Target }],
  },
];

const SECTIONS_BY_ROLE: Record<RoleSlug, NavSection[]> = {
  employee: EMPLOYEE_SECTIONS,
  "hr-admin": HR_ADMIN_SECTIONS,
  "payroll-manager": PAYROLL_MANAGER_SECTIONS,
  doctor: DOCTOR_SECTIONS,
  controller: CONTROLLER_SECTIONS,
  admin: ADMIN_SECTIONS,
};

/** Returns the navigation sections for a role. */
export function sidebarForRole(slug: RoleSlug): NavSection[] {
  return SECTIONS_BY_ROLE[slug] ?? EMPLOYEE_SECTIONS;
}
