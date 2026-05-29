import type { MissionOrderStatus } from "@/server/ksm/modules/missions";

export type BadgeTone =
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "orange"
  | "violet"
  | "teal"
  | "gray";

export function missionStatusTone(status: MissionOrderStatus): BadgeTone {
  switch (status) {
    case "DRAFT":
      return "gray";
    case "PENDING_ACCEPTANCE":
      return "warning";
    case "APPROVED":
      return "info";
    case "DECLINED":
      return "danger";
    case "IN_PROGRESS":
      return "orange";
    case "COMPLETED":
      return "success";
    case "CANCELLED":
      return "gray";
    default:
      return "gray";
  }
}
