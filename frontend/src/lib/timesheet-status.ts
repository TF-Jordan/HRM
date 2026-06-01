import type { TimesheetStatus } from "@/server/ksm/modules/timesheets";

export type BadgeTone =
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "orange"
  | "violet"
  | "teal"
  | "gray";

export function timesheetStatusTone(status: TimesheetStatus): BadgeTone {
  switch (status) {
    case "VALIDATED":
      return "success";
    case "SUBMITTED":
      return "info";
    case "DRAFT":
      return "gray";
    default:
      return "gray";
  }
}

export function timesheetTotalHours(t: {
  heuresNormales: number | string;
  heuresSupplementaires: number | string;
  heuresNuit: number | string;
  heuresWeekend: number | string;
}): number {
  return (
    Number(t.heuresNormales) +
    Number(t.heuresSupplementaires) +
    Number(t.heuresNuit) +
    Number(t.heuresWeekend)
  );
}
