import { cva, type VariantProps } from "class-variance-authority";
import * as React from "react";

import { cn } from "@/lib/utils";

const statusPillVariants = cva(
  "inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-[11px] font-bold uppercase tracking-wider text-white",
  {
    variants: {
      tone: {
        orange: "bg-grad-orange shadow-orange-brand",
        success: "bg-grad-green",
        danger: "bg-danger-500",
        dark: "bg-ink",
        info: "bg-grad-blue",
      },
    },
    defaultVariants: { tone: "orange" },
  },
);

export interface StatusPillProps
  extends React.HTMLAttributes<HTMLSpanElement>,
    VariantProps<typeof statusPillVariants> {}

export function StatusPill({ tone, className, ...props }: StatusPillProps) {
  return <span className={cn(statusPillVariants({ tone }), className)} {...props} />;
}
