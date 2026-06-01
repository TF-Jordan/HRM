import { cva, type VariantProps } from "class-variance-authority";
import type { LucideIcon } from "lucide-react";

import { cn } from "@/lib/utils";

const iconTileVariants = cva(
  "relative grid h-[38px] w-[38px] shrink-0 place-items-center rounded-[11px] shadow-xs-brand after:absolute after:inset-0 after:rounded-[inherit] after:bg-[linear-gradient(180deg,rgba(255,255,255,0.4),transparent_50%)] after:pointer-events-none",
  {
    variants: {
      tone: {
        orange: "bg-orange-50 text-orange-600",
        success: "bg-success-50 text-success-600",
        info: "bg-info-50 text-info-600",
        violet: "bg-violet-50 text-violet-600",
        warning: "bg-warning-50 text-warning-600",
        danger: "bg-danger-50 text-danger-600",
        teal: "bg-teal-50 text-teal-600",
        gray: "bg-bg-soft text-ink-3",
      },
      size: {
        sm: "h-8 w-8",
        md: "h-[38px] w-[38px]",
        lg: "h-12 w-12",
      },
    },
    defaultVariants: { tone: "orange", size: "md" },
  },
);

export interface IconTileProps extends VariantProps<typeof iconTileVariants> {
  icon: LucideIcon;
  className?: string;
}

export function IconTile({ icon: Icon, tone, size, className }: IconTileProps) {
  return (
    <span className={cn(iconTileVariants({ tone, size }), className)}>
      <Icon className={cn("relative z-10", size === "sm" ? "h-4 w-4" : size === "lg" ? "h-6 w-6" : "h-5 w-5")} />
    </span>
  );
}
