"use client";

import * as React from "react";
import { useTranslations } from "next-intl";
import { Badge, type BadgeProps } from "@/components/ui/badge";

/**
 * Maps a domain status to a tone color. Mirrors the HR Core conventions.
 * Falls back to gray if unknown.
 */
type BadgeTone = NonNullable<BadgeProps["tone"]>;
const STATUS_TONE_MAP: Record<string, Record<string, BadgeTone>> = {
  employee: { ACTIVE: "green", ON_LEAVE: "blue", SUSPENDED: "amber", TERMINATED: "red" },
  contract: { ACTIVE: "green", EXPIRED: "amber", TERMINATED: "red", RENEWED: "blue", TRIAL: "teal", ENDING_SOON: "amber" },
  leave: { PENDING: "amber", APPROVED: "green", REJECTED: "red", CANCELLED: "gray" },
  loan: {
    PENDING: "amber",
    REJECTED: "red",
    IN_REPAYMENT: "blue",
    FULLY_REPAID: "green",
  },
  payrollRun: { CALCULATED: "amber", VALIDATED: "blue", PAID: "green" },
  expense: {
    DRAFT: "gray",
    SUBMITTED: "amber",
    APPROVED: "blue",
    REJECTED: "red",
    REIMBURSED: "green",
  },
  mission: {
    DRAFT: "gray",
    APPROVED: "blue",
    IN_PROGRESS: "amber",
    COMPLETED: "green",
    CANCELLED: "red",
  },
  application: {
    NEW: "blue",
    SHORTLISTED: "violet",
    INTERVIEWING: "amber",
    OFFERED: "teal",
    HIRED: "green",
    REJECTED: "red",
  },
  interview: { PENDING: "amber", PASS: "green", FAIL: "red" },
  onboardingTask: { PENDING: "amber", IN_PROGRESS: "blue", COMPLETED: "green" },
  jobOffer: { DRAFT: "gray", PUBLISHED: "green", CLOSED: "red" },
  training: { PLANNED: "blue", IN_PROGRESS: "amber", COMPLETED: "green", CANCELLED: "red" },
  trainingEnrollment: { ENROLLED: "blue", COMPLETED: "green", CANCELLED: "red" },
  review: { DRAFT: "gray", SUBMITTED: "amber", ACKNOWLEDGED: "blue", FINALIZED: "green" },
  timesheet: { DRAFT: "gray", SUBMITTED: "amber", VALIDATED: "green" },
  socialDeclaration: {
    DRAFT: "gray",
    GENERATED: "blue",
    SUBMITTED: "amber",
    ACKNOWLEDGED: "green",
  },
};

export type StatusKind = keyof typeof STATUS_TONE_MAP;

export type StatusBadgeProps = {
  kind: StatusKind;
  status: string;
  className?: string;
};

export function StatusBadge({ kind, status, className }: StatusBadgeProps) {
  const t = useTranslations("statuses");
  const tone = STATUS_TONE_MAP[kind]?.[status] ?? "gray";
  const label = safeT(t, `${kind}.${status}` as never, status);
  return (
    <Badge tone={tone} className={className}>
      {label}
    </Badge>
  );
}

function safeT(
  t: (key: never) => string,
  key: string,
  fallback: string,
): string {
  try {
    const value = t(key as never);
    return value || fallback;
  } catch {
    return fallback;
  }
}
