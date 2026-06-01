import type { EmployeeStatus } from "@/server/ksm/modules/employees";

export type BadgeTone =
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "orange"
  | "violet"
  | "teal"
  | "gray";

export function employeeStatusTone(status: EmployeeStatus): BadgeTone {
  switch (status) {
    case "ACTIVE":
      return "success";
    case "ON_LEAVE":
      return "info";
    case "SUSPENDED":
      return "warning";
    case "TERMINATED":
      return "danger";
    default:
      return "gray";
  }
}
