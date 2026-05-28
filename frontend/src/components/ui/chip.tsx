import * as React from "react";

import { cn } from "@/lib/utils";

export interface ChipProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  active?: boolean;
  tone?: "default" | "orange";
}

export const Chip = React.forwardRef<HTMLButtonElement, ChipProps>(
  ({ active, tone = "default", className, type = "button", ...props }, ref) => {
    return (
      <button
        ref={ref}
        type={type}
        className={cn(
          "inline-flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-[12.5px] font-medium shadow-xs-brand transition-all duration-200 ease-[var(--ease-brand)]",
          !active && "border-line bg-white text-ink-2 hover:-translate-y-px hover:border-line-strong",
          active && tone === "default" && "border-ink bg-ink text-white shadow-sm-brand",
          active && tone === "orange" && "border-transparent bg-grad-orange text-white shadow-orange-brand",
          className,
        )}
        {...props}
      />
    );
  },
);
Chip.displayName = "Chip";
