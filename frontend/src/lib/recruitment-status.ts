import type {
  ApplicationStatus,
  InterviewResult,
  JobOfferStatus,
  OnboardingTaskStatus,
} from "@/server/ksm/modules/recruitment";

export type BadgeTone =
  | "success"
  | "warning"
  | "danger"
  | "info"
  | "orange"
  | "violet"
  | "teal"
  | "gray";

export function jobOfferStatusTone(status: JobOfferStatus): BadgeTone {
  switch (status) {
    case "DRAFT":
      return "gray";
    case "PUBLISHED":
      return "orange";
    case "CLOSED":
      return "success";
  }
}

export function applicationStatusTone(status: ApplicationStatus): BadgeTone {
  switch (status) {
    case "NEW":
      return "gray";
    case "SHORTLISTED":
      return "info";
    case "INTERVIEWING":
      return "orange";
    case "OFFERED":
      return "warning";
    case "REJECTED":
      return "danger";
    case "HIRED":
      return "success";
  }
}

export function interviewResultTone(r: InterviewResult): BadgeTone {
  switch (r) {
    case "PENDING":
      return "warning";
    case "PASS":
      return "success";
    case "FAIL":
      return "danger";
  }
}

export function taskStatusTone(s: OnboardingTaskStatus): BadgeTone {
  switch (s) {
    case "PENDING":
      return "gray";
    case "IN_PROGRESS":
      return "orange";
    case "COMPLETED":
      return "success";
  }
}

/** Kanban column order, matching the design. */
export const KANBAN_COLUMNS: ApplicationStatus[] = [
  "NEW",
  "SHORTLISTED",
  "INTERVIEWING",
  "OFFERED",
  "HIRED",
];
