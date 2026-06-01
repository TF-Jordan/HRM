import * as React from "react";

import { cn } from "@/lib/utils";

export type StatDotTone = "green" | "orange" | "amber" | "blue" | "violet" | "red" | "teal" | "gray";

const DOT_BG: Record<StatDotTone, string> = {
  green: "bg-success-500",
  orange: "bg-orange-500",
  amber: "bg-warning-500",
  blue: "bg-info-500",
  violet: "bg-violet-500",
  red: "bg-danger-500",
  teal: "bg-teal-500",
  gray: "bg-ink-4",
};

const DOT_GLOW: Record<StatDotTone, string> = {
  green: "shadow-[0_0_8px_rgba(16,185,129,0.5)]",
  orange: "shadow-[0_0_8px_rgba(242,107,15,0.5)]",
  amber: "shadow-[0_0_8px_rgba(245,158,11,0.5)]",
  blue: "shadow-[0_0_8px_rgba(59,130,246,0.5)]",
  violet: "shadow-[0_0_8px_rgba(139,92,246,0.5)]",
  red: "shadow-[0_0_8px_rgba(239,68,68,0.5)]",
  teal: "shadow-[0_0_8px_rgba(20,184,166,0.5)]",
  gray: "",
};

export interface StatCardProps {
  label: string;
  value: React.ReactNode;
  sub?: React.ReactNode;
  tone?: StatDotTone;
  className?: string;
}

/**
 * The white "kpi-mini" stat tile from the HR Core design: an uppercase label
 * with a glowing status dot top-right, a big tabular value, and a small
 * sub-text line. Used in the 4-up row at the top of every list page.
 */
export function StatCard({ label, value, sub, tone = "orange", className }: StatCardProps) {
  return (
    <div
      className={cn(
        "relative flex flex-col gap-1 overflow-hidden rounded-[16px] border border-line bg-white px-[18px] py-4 shadow-xs-brand",
        "before:absolute before:inset-0 before:bg-[linear-gradient(180deg,rgba(255,255,255,0.5),transparent_40%)] before:pointer-events-none",
        className,
      )}
    >
      <div className="relative flex items-center justify-between">
        <span className="text-[11px] font-semibold uppercase tracking-[0.06em] text-ink-3">
          {label}
        </span>
        <span
          className={cn("inline-block h-2 w-2 rounded-full", DOT_BG[tone], DOT_GLOW[tone])}
          aria-hidden="true"
        />
      </div>
      <div className="relative font-display text-[24px] font-extrabold tracking-tight text-ink font-mono-tabular">
        {value}
      </div>
      {sub && <div className="relative text-[11px] text-ink-3">{sub}</div>}
    </div>
  );
}

export function StatCardGrid({ children, className }: { children: React.ReactNode; className?: string }) {
  return <div className={cn("mb-5 grid grid-cols-4 gap-4", className)}>{children}</div>;
}
