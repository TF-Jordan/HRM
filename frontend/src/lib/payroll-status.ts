import type { PayrollRunStatus } from "@/server/ksm/modules/payroll";

export function payrollStatusTone(s: PayrollRunStatus | string): "warning" | "info" | "success" | "gray" {
  switch (s) {
    case "CALCULATED":
      return "warning";
    case "VALIDATED":
      return "info";
    case "PAID":
      return "success";
    default:
      return "gray";
  }
}

export function payrollStatusProgress(s: PayrollRunStatus | string): number {
  switch (s) {
    case "CALCULATED":
      return 0.4;
    case "VALIDATED":
      return 0.8;
    case "PAID":
      return 1;
    default:
      return 0;
  }
}

export type PayrollStepKey = "variables" | "calculation" | "review" | "validation" | "payment";

export function payrollStepperState(
  status: PayrollRunStatus | string,
): Record<PayrollStepKey, "done" | "active" | "pending"> {
  if (status === "PAID") {
    return {
      variables: "done",
      calculation: "done",
      review: "done",
      validation: "done",
      payment: "done",
    };
  }
  if (status === "VALIDATED") {
    return {
      variables: "done",
      calculation: "done",
      review: "done",
      validation: "done",
      payment: "active",
    };
  }
  if (status === "CALCULATED") {
    return {
      variables: "done",
      calculation: "done",
      review: "active",
      validation: "pending",
      payment: "pending",
    };
  }
  return {
    variables: "pending",
    calculation: "pending",
    review: "pending",
    validation: "pending",
    payment: "pending",
  };
}

export function formatPeriodFr(periode: string): string {
  const [y, m] = periode.split("-").map(Number);
  if (!y || !m) return periode;
  const d = new Date(y, m - 1, 1);
  return d.toLocaleDateString("fr-FR", { month: "long", year: "numeric" });
}

export function formatPeriodShort(periode: string, locale: "fr" | "en"): string {
  const [y, m] = periode.split("-").map(Number);
  if (!y || !m) return periode;
  const d = new Date(y, m - 1, 1);
  return d.toLocaleDateString(locale === "fr" ? "fr-FR" : "en-US", {
    month: "short",
    year: "numeric",
  });
}
