import type { AptitudeResult } from "@/server/ksm/modules/medical";

export type BadgeTone =
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "orange"
  | "violet"
  | "teal"
  | "gray";

export function aptitudeTone(r: AptitudeResult): BadgeTone {
  switch (r) {
    case "APTE":
      return "success";
    case "APTE_AVEC_RESTRICTIONS":
      return "warning";
    case "INAPTE_TEMPORAIRE":
      return "danger";
  }
}

/**
 * Certificate / next-visit alert bucket based on time-to-expiry. Used by the
 * dashboard tiles + per-row badge on the medical overview.
 */
export type AlertLevel = "OK" | "DUE_SOON" | "OVERDUE";

export function alertLevel(dateISO: string, soonDays = 30): AlertLevel {
  const now = new Date();
  const target = new Date(dateISO);
  const diffDays = Math.floor((target.getTime() - now.getTime()) / 86_400_000);
  if (diffDays < 0) return "OVERDUE";
  if (diffDays <= soonDays) return "DUE_SOON";
  return "OK";
}

export function alertTone(level: AlertLevel): BadgeTone {
  switch (level) {
    case "OK":
      return "success";
    case "DUE_SOON":
      return "warning";
    case "OVERDUE":
      return "danger";
  }
}
