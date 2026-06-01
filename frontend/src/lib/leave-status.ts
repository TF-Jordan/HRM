import type { LeaveStatus } from "@/server/ksm/modules/leaves";

export type BadgeTone =
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "orange"
  | "violet"
  | "teal"
  | "gray";

export function leaveStatusTone(status: LeaveStatus): BadgeTone {
  switch (status) {
    case "APPROVED":
      return "success";
    case "PENDING":
      return "warning";
    case "REJECTED":
      return "danger";
    case "CANCELLED":
      return "gray";
    default:
      return "info";
  }
}
