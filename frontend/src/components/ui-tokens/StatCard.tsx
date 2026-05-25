import * as React from "react";
import { cn } from "@/lib/utils";

export type StatTone = "green" | "orange" | "amber" | "blue" | "red" | "violet" | "teal" | "dark";

const DOT_CLASS: Record<StatTone, string> = {
  green: "bg-status-green-500",
  orange: "bg-brand-500",
  amber: "bg-status-amber-500",
  blue: "bg-status-blue-500",
  red: "bg-status-red-500",
  violet: "bg-status-violet-500",
  teal: "bg-status-teal-500",
  dark: "bg-ink",
};

export type StatCardProps = {
  tone?: StatTone;
  label: string;
  value: React.ReactNode;
  footer?: React.ReactNode;
  className?: string;
};

/**
 * Flat white KPI card used across HR Core dashboards: uppercase label,
 * tone-coloured dot, large numeric value and a muted footer line.
 */
export function StatCard({ tone = "orange", label, value, footer, className }: StatCardProps) {
  return (
    <div className={cn("rounded-[18px] border border-line bg-white p-4 shadow-elev-sm", className)}>
      <div className="flex items-center justify-between">
        <span className="text-[10.5px] font-semibold uppercase tracking-[0.12em] text-ink-4">{label}</span>
        <span className={cn("size-2 rounded-full", DOT_CLASS[tone])} />
      </div>
      <div className="mt-2 font-display text-[30px] font-extrabold leading-none tracking-tight text-ink tabular">
        {value}
      </div>
      {footer != null && <div className="mt-1.5 text-[11.5px] text-ink-3">{footer}</div>}
    </div>
  );
}
