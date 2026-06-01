import { cva, type VariantProps } from "class-variance-authority";
import type { LucideIcon } from "lucide-react";
import * as React from "react";

import { cn } from "@/lib/utils";

const kpiVariants = cva(
  "relative isolate flex min-h-[132px] flex-col gap-1.5 overflow-hidden rounded-[20px] px-[22px] pt-[22px] pb-5 text-white shadow-md-brand transition-all duration-200 ease-[var(--ease-brand)] hover:-translate-y-0.5 hover:shadow-lg-brand",
  {
    variants: {
      tone: {
        orange: "bg-grad-orange",
        amber: "bg-grad-amber",
        green: "bg-grad-green",
        blue: "bg-grad-blue",
        violet: "bg-grad-violet",
        teal: "bg-grad-teal",
        dark: "bg-grad-dark",
      },
    },
    defaultVariants: { tone: "orange" },
  },
);

export interface KpiCardProps extends VariantProps<typeof kpiVariants> {
  label: string;
  value: React.ReactNode;
  foot?: React.ReactNode;
  icon?: LucideIcon;
  className?: string;
}

export function KpiCard({ tone, label, value, foot, icon: Icon, className }: KpiCardProps) {
  return (
    <div className={cn(kpiVariants({ tone }), className)}>
      {/* glass overlay */}
      <span
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 bg-[radial-gradient(120%_80%_at_100%_0%,rgba(255,255,255,0.25),transparent_50%),linear-gradient(180deg,rgba(255,255,255,0.12),transparent_30%)]"
      />
      <span
        aria-hidden="true"
        className="pointer-events-none absolute -bottom-2/5 -right-1/5 h-full w-3/4 rounded-full bg-[radial-gradient(circle,rgba(255,255,255,0.18),transparent_60%)] blur-xl"
      />
      <div className="relative z-10 flex items-start justify-between">
        {Icon && (
          <span className="grid h-[38px] w-[38px] place-items-center rounded-[11px] bg-white/20 backdrop-blur-sm shadow-inset-top">
            <Icon className="h-5 w-5" />
          </span>
        )}
      </div>
      <div className="relative z-10 mt-auto">
        <p className="text-[12.5px] font-medium opacity-90">{label}</p>
        <p className="font-display text-[32px] font-extrabold leading-none tracking-tight">
          {value}
        </p>
        {foot && <p className="mt-1 flex items-center gap-1.5 text-[11.5px] opacity-80">{foot}</p>}
      </div>
    </div>
  );
}

export interface KpiMiniProps {
  label: string;
  value: React.ReactNode;
  delta?: React.ReactNode;
  icon?: LucideIcon;
  className?: string;
}

export function KpiMini({ label, value, delta, icon: Icon, className }: KpiMiniProps) {
  return (
    <div
      className={cn(
        "relative flex flex-col gap-1 overflow-hidden rounded-[16px] border border-line bg-white px-[18px] py-4 shadow-xs-brand transition-all duration-200 ease-[var(--ease-brand)] before:absolute before:inset-0 before:bg-[linear-gradient(180deg,rgba(255,255,255,0.5),transparent_40%)] before:pointer-events-none hover:-translate-y-0.5 hover:border-orange-200 hover:shadow-md-brand",
        className,
      )}
    >
      <div className="relative flex items-center justify-between">
        <p className="text-[11px] font-semibold uppercase tracking-wider text-ink-3">{label}</p>
        {Icon && <Icon className="h-4 w-4 text-ink-4" />}
      </div>
      <p className="relative font-display text-[24px] font-extrabold tracking-tight text-ink">
        {value}
      </p>
      {delta && <p className="relative text-[11.5px] text-ink-3">{delta}</p>}
    </div>
  );
}
