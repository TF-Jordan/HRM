/**
 * Mapping of KSM `errorCode` values to i18n message keys and UI actions.
 * See PROMPT_FRONTEND_HRM.md § 18.
 */
export type ErrorCodeAction =
  | { kind: "toast"; severity: "error" | "warning" | "info" }
  | { kind: "redirect"; to: string }
  | { kind: "highlightField"; field: string }
  | { kind: "refresh" };

export type ErrorCodeDescriptor = {
  /** i18n key, looked up under `errors.<key>`. */
  i18nKey: string;
  actions: ErrorCodeAction[];
};

export const ERROR_CODES: Record<string, ErrorCodeDescriptor> = {
  UNAUTHORIZED: { i18nKey: "unauthorized", actions: [{ kind: "redirect", to: "/login" }] },
  FORBIDDEN: { i18nKey: "forbidden", actions: [{ kind: "toast", severity: "error" }] },
  ORGANIZATION_CONTEXT_REQUIRED: {
    i18nKey: "organizationContextRequired",
    actions: [{ kind: "redirect", to: "/select-context" }],
  },
  ORGANIZATION_SERVICE_NOT_SUBSCRIBED: {
    i18nKey: "serviceNotSubscribed",
    actions: [{ kind: "toast", severity: "error" }],
  },
  DUPLICATE_EMPLOYEE: {
    i18nKey: "duplicateEmployee",
    actions: [{ kind: "highlightField", field: "actorId" }],
  },
  EMPLOYEE_NOT_FOUND: {
    i18nKey: "employeeNotFound",
    actions: [{ kind: "redirect", to: "/employees" }],
  },
  INSUFFICIENT_LEAVE_BALANCE: {
    i18nKey: "insufficientLeaveBalance",
    actions: [{ kind: "highlightField", field: "leaveType" }],
  },
  PAYROLL_ALREADY_EXISTS: {
    i18nKey: "payrollAlreadyExists",
    actions: [{ kind: "toast", severity: "warning" }],
  },
  ILLEGAL_STATE_TRANSITION: {
    i18nKey: "illegalStateTransition",
    actions: [{ kind: "toast", severity: "warning" }, { kind: "refresh" }],
  },
  LOAN_CEILING_EXCEEDED: {
    i18nKey: "loanCeilingExceeded",
    actions: [{ kind: "toast", severity: "error" }],
  },
  ACTIVE_CONTRACT_EXISTS: {
    i18nKey: "activeContractExists",
    actions: [{ kind: "toast", severity: "warning" }],
  },
  VALIDATION_ERROR: {
    i18nKey: "validationError",
    actions: [{ kind: "toast", severity: "error" }],
  },
};

export function getErrorDescriptor(code: string | null | undefined): ErrorCodeDescriptor {
  if (code && ERROR_CODES[code]) return ERROR_CODES[code];
  return { i18nKey: "unknown", actions: [{ kind: "toast", severity: "error" }] };
}
