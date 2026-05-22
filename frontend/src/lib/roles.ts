export const ROLE_CODES = [
  "PLATFORM_ADMIN",
  "HRM_ADMIN",
  "DRH",
  "RESP_PAIE",
  "COMPTABLE",
  "RECRUTEUR",
  "MANAGER",
  "MEDECIN",
  "EMPLOYE",
] as const;

export type RoleCode = (typeof ROLE_CODES)[number];

const has = (perms: string[], p: string) => perms.includes(p);

export function inferRoleCode(permissions: string[]): RoleCode {
  if (has(permissions, "iam:admin")) return "PLATFORM_ADMIN";
  // HRM_ADMIN (seed V67) cumule l'ensemble des hrm:* perms — détection par signature large.
  if (
    has(permissions, "hrm:payroll:run") &&
    has(permissions, "hrm:medical:create") &&
    has(permissions, "hrm:recruitment:manage") &&
    has(permissions, "hrm:training:manage")
  ) {
    return "HRM_ADMIN";
  }
  if (has(permissions, "hrm:payroll:run")) return "RESP_PAIE";
  if (has(permissions, "hrm:loan:approve")) return "COMPTABLE";
  if (has(permissions, "hrm:training:manage") || has(permissions, "hrm:kpi:read")) return "DRH";
  if (has(permissions, "hrm:recruitment:manage")) return "RECRUTEUR";
  if (has(permissions, "hrm:medical:create")) return "MEDECIN";
  if (has(permissions, "hrm:leave:approve")) return "MANAGER";
  return "EMPLOYE";
}
