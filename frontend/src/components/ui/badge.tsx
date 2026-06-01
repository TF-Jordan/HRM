import { cva, type VariantProps } from "class-variance-authority";
import * as React from "react";

import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center gap-1.5 px-2.5 py-0.5 rounded-full text-[11px] font-semibold leading-relaxed whitespace-nowrap",
  {
    variants: {
      tone: {
        success: "bg-success-50 text-success-600 [&_.dot]:bg-success-600",
        info: "bg-info-50 text-info-600 [&_.dot]:bg-info-600",
        danger: "bg-danger-50 text-danger-600 [&_.dot]:bg-danger-600",
        warning: "bg-warning-50 text-warning-600 [&_.dot]:bg-warning-600",
        orange: "bg-orange-50 text-orange-700 [&_.dot]:bg-orange-700",
        violet: "bg-violet-50 text-violet-600 [&_.dot]:bg-violet-600",
        teal: "bg-teal-50 text-teal-600 [&_.dot]:bg-teal-600",
        gray: "bg-bg-soft text-ink-3 [&_.dot]:bg-ink-3",
      },
      showDot: { true: "", false: "" },
    },
    defaultVariants: { tone: "gray", showDot: true },
  },
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof badgeVariants> {}

export function Badge({ tone, showDot = true, className, children, ...props }: BadgeProps) {
  return (
    <span className={cn(badgeVariants({ tone, showDot }), className)} {...props}>
      {showDot && (
        <span className="dot inline-block h-1.5 w-1.5 shrink-0 rounded-full" aria-hidden="true" />
      )}
      {children}
    </span>
  );
}
