import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";
import type { LucideIcon } from "lucide-react";

const kpiVariants = cva(
  "relative flex min-h-[132px] flex-col gap-1.5 overflow-hidden rounded-[20px] p-5 text-white shadow-elev-md transition-all hover:-translate-y-0.5 hover:shadow-elev-lg",
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

export interface KpiCardProps
  extends React.HTMLAttributes<HTMLDivElement>,
    VariantProps<typeof kpiVariants> {
  icon?: LucideIcon;
  label: string;
  value: React.ReactNode;
  delta?: React.ReactNode;
  footer?: React.ReactNode;
}

export function KpiCard({
  icon: Icon,
  label,
  value,
  delta,
  footer,
  tone,
  className,
  ...props
}: KpiCardProps) {
  return (
    <div className={cn(kpiVariants({ tone }), className)} {...props}>
      <span
        aria-hidden
        className="pointer-events-none absolute inset-0 bg-[radial-gradient(120%_80%_at_100%_0%,rgba(255,255,255,0.25),transparent_50%),linear-gradient(180deg,rgba(255,255,255,0.12),transparent_30%)]"
      />
      <div className="relative flex items-start justify-between">
        {Icon && (
          <div className="grid size-10 place-items-center rounded-xl bg-white/20 shadow-inset-top backdrop-blur-sm">
            <Icon className="size-5" aria-hidden />
          </div>
        )}
        {delta && <div className="text-[11.5px] opacity-85">{delta}</div>}
      </div>
      <div className="relative mt-auto">
        <div className="text-[12.5px] font-medium opacity-90">{label}</div>
        <div className="font-display text-[32px] font-extrabold leading-none tracking-tight tabular">
          {value}
        </div>
        {footer && <div className="mt-1 text-[11.5px] opacity-80">{footer}</div>}
      </div>
    </div>
  );
}
