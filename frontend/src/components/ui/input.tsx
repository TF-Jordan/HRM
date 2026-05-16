import * as React from "react";
import { cn } from "@/lib/utils";

const Input = React.forwardRef<HTMLInputElement, React.InputHTMLAttributes<HTMLInputElement>>(
  ({ className, type, ...props }, ref) => {
    return (
      <input
        type={type}
        ref={ref}
        className={cn(
          "h-11 w-full rounded-[11px] border border-line bg-white px-3.5 py-2.5 text-[13.5px] text-ink shadow-elev-sm outline-none transition-colors placeholder:text-ink-4 focus-visible:border-brand-400 focus-visible:shadow-[0_0_0_4px_rgba(242,107,15,0.12)] disabled:cursor-not-allowed disabled:opacity-50",
          className,
        )}
        {...props}
      />
    );
  },
);
Input.displayName = "Input";

export { Input };
