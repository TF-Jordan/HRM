import * as React from "react";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const badgeVariants = cva(
  "inline-flex items-center gap-1.5 rounded-full px-2.5 py-0.5 text-[11px] font-semibold leading-tight whitespace-nowrap",
  {
    variants: {
      tone: {
        gray: "bg-cream-soft text-ink-3",
        green: "bg-status-green-50 text-status-green-600",
        orange: "bg-brand-50 text-brand-700",
        amber: "bg-status-amber-50 text-status-amber-600",
        red: "bg-status-red-50 text-status-red-600",
        blue: "bg-status-blue-50 text-status-blue-600",
        violet: "bg-status-violet-50 text-status-violet-600",
        teal: "bg-status-teal-50 text-status-teal-600",
        dark: "bg-ink text-white",
      },
      withDot: { true: "before:size-1.5 before:rounded-full before:bg-current", false: "" },
    },
    defaultVariants: { tone: "gray", withDot: true },
  },
);

export interface BadgeProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof badgeVariants> {}

export function Badge({ className, tone, withDot, ...props }: BadgeProps) {
  return <span className={cn(badgeVariants({ tone, withDot }), className)} {...props} />;
}
