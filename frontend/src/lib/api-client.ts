import { HttpError, type ApiResponse } from "@/lib/types/api";

/**
 * Client-side fetcher for BFF routes. All BFF responses follow the
 * { success, data, message, errorCode } envelope.
 */
export async function bffFetch<T>(
  path: string,
  init: RequestInit = {},
): Promise<T> {
  const res = await fetch(path, {
    credentials: "include",
    ...init,
    headers: {
      Accept: "application/json",
      ...(init.body && !(init.body instanceof FormData)
        ? { "Content-Type": "application/json" }
        : {}),
      ...(init.headers ?? {}),
    },
  });
  let body: unknown = null;
  const text = await res.text();
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      body = text;
    }
  }
  if (!res.ok) {
    const env = body as Partial<ApiResponse<unknown>> & { errorCode?: string | null };
    throw new HttpError({
      status: res.status,
      message: env?.message ?? `HTTP ${res.status}`,
      errorCode: env?.errorCode ?? null,
      upstream: body,
    });
  }
  const env = body as ApiResponse<T>;
  return env.data as T;
}

export const queryKeys = {
  hrm: {
    dashboard: () => ["hrm", "dashboard"] as const,
    employees: () => ["hrm", "employees"] as const,
    employee: (id: string) => ["hrm", "employees", id] as const,
    contracts: (employeeId: string) =>
      ["hrm", "employees", employeeId, "contracts"] as const,
    dependents: (employeeId: string) =>
      ["hrm", "employees", employeeId, "dependents"] as const,
    leaveBalances: (employeeId: string, annee: number) =>
      ["hrm", "employees", employeeId, "leave-balances", annee] as const,
    meEmployee: () => ["hrm", "me", "employee"] as const,
    employeeLeaves: (employeeId: string) =>
      ["hrm", "employees", employeeId, "leaves"] as const,
    employeeLoans: (employeeId: string) =>
      ["hrm", "employees", employeeId, "loans"] as const,
    employeeExpenses: (employeeId: string) =>
      ["hrm", "expenses", "by-employee", employeeId] as const,
    expense: (id: string) => ["hrm", "expenses", id] as const,
    expenseLines: (id: string) => ["hrm", "expenses", id, "lines"] as const,
    employeeTimesheets: (employeeId: string, periode: string | null) =>
      ["hrm", "employees", employeeId, "timesheets", periode] as const,
    payrollRuns: () => ["hrm", "payroll", "runs"] as const,
    payrollRun: (id: string) => ["hrm", "payroll", "runs", id] as const,
    payrollEntries: (runId: string) =>
      ["hrm", "payroll", "runs", runId, "entries"] as const,
    payslipLines: (entryId: string) =>
      ["hrm", "payroll", "entries", entryId, "payslip"] as const,
    pendingLoans: () => ["hrm", "loans", "pending"] as const,
    pendingLeaves: () => ["hrm", "leaves", "pending"] as const,
    orgTimesheets: (periode: string | null) => ["hrm", "timesheets", "org", periode] as const,
    employeeMissions: (employeeId: string) =>
      ["hrm", "employees", employeeId, "missions"] as const,
    mission: (id: string) => ["hrm", "missions", id] as const,
    reviews: (periode: string | null) => ["hrm", "reviews", periode] as const,
    review: (id: string) => ["hrm", "reviews", id] as const,
    reviewObjectives: (id: string) => ["hrm", "reviews", id, "objectives"] as const,
    declarations: () => ["hrm", "declarations"] as const,
    declaration: (id: string) => ["hrm", "declarations", id] as const,
    jobOffers: () => ["hrm", "job-offers"] as const,
    jobOffer: (id: string) => ["hrm", "job-offers", id] as const,
    applications: (jobOfferId: string) =>
      ["hrm", "job-offers", jobOfferId, "applications"] as const,
    application: (id: string) => ["hrm", "applications", id] as const,
    trainings: () => ["hrm", "trainings"] as const,
    training: (id: string) => ["hrm", "trainings", id] as const,
    trainingEnrollments: (trainingId: string) =>
      ["hrm", "trainings", trainingId, "enrollments"] as const,
    employeeEnrollments: (employeeId: string) =>
      ["hrm", "trainings", "by-employee", employeeId] as const,
    trainingBudgets: (annee: number) => ["hrm", "training-budgets", annee] as const,
    trainingBudget: (id: string) => ["hrm", "training-budgets", id] as const,
    skills: () => ["hrm", "skills"] as const,
    skill: (id: string) => ["hrm", "skills", id] as const,
    employeeSkills: (employeeId: string) =>
      ["hrm", "skills", "by-employee", employeeId] as const,
    kpiSnapshots: () => ["hrm", "kpi"] as const,
    kpiSnapshot: (id: string) => ["hrm", "kpi", id] as const,
    employeeVisits: (employeeId: string) =>
      ["hrm", "medical", "visits", "by-employee", employeeId] as const,
    employeeCertificates: (employeeId: string) =>
      ["hrm", "medical", "certificates", "by-employee", employeeId] as const,
    medicalVisit: (id: string) => ["hrm", "medical", "visits", id] as const,
    medicalCertificate: (id: string) => ["hrm", "medical", "certificates", id] as const,
  },
};
