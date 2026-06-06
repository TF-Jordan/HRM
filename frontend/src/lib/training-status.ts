import type {
  EnrollmentStatus,
  TrainingRequestStatus,
  TrainingStatus,
} from "@/server/ksm/modules/trainings";
import type { ReviewStatus } from "@/server/ksm/modules/reviews";

export type BadgeTone =
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "orange"
  | "violet"
  | "teal"
  | "gray";

export function trainingStatusTone(status: TrainingStatus): BadgeTone {
  switch (status) {
    case "PLANNED":
      return "info";
    case "IN_PROGRESS":
      return "orange";
    case "COMPLETED":
      return "success";
    case "CANCELLED":
      return "gray";
  }
}

export function enrollmentStatusTone(status: EnrollmentStatus): BadgeTone {
  switch (status) {
    case "ENROLLED":
      return "info";
    case "COMPLETED":
      return "success";
    case "CANCELLED":
      return "gray";
  }
}

export function trainingRequestStatusTone(status: TrainingRequestStatus): BadgeTone {
  switch (status) {
    case "PENDING":
      return "warning";
    case "APPROVED":
      return "success";
    case "REJECTED":
      return "danger";
    case "CANCELLED":
      return "gray";
  }
}

export function reviewStatusTone(status: ReviewStatus): BadgeTone {
  switch (status) {
    case "DRAFT":
      return "gray";
    case "SUBMITTED":
      return "warning";
    case "ACKNOWLEDGED":
      return "info";
    case "FINALIZED":
      return "success";
  }
}

/**
 * Pick a deterministic gradient for a training catalog card so that the same
 * training id always gets the same color (no fabricated category coloring).
 */
const CARD_GRADS = [
  "linear-gradient(135deg, #FB923C, #EA580C)",
  "linear-gradient(135deg, #60A5FA, #2563EB)",
  "linear-gradient(135deg, #34D399, #059669)",
  "linear-gradient(135deg, #A78BFA, #7C3AED)",
  "linear-gradient(135deg, #FCD34D, #D97706)",
  "linear-gradient(135deg, #2DD4BF, #0D9488)",
] as const;

export function gradientForId(id: string): string {
  let hash = 0;
  for (let i = 0; i < id.length; i++) hash = (hash * 31 + id.charCodeAt(i)) | 0;
  return CARD_GRADS[Math.abs(hash) % CARD_GRADS.length];
}
