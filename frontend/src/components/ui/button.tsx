import { cva, type VariantProps } from "class-variance-authority";
import * as React from "react";

import { cn } from "@/lib/utils";

const buttonVariants = cva(
  "relative isolate inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-[12px] font-semibold tracking-tight transition-all duration-200 ease-[var(--ease-brand)] disabled:cursor-not-allowed disabled:opacity-50 active:scale-[0.98] focus-visible:outline-none focus-visible:ring-4 focus-visible:ring-orange-500/20",
  {
    variants: {
      variant: {
        primary:
          "bg-grad-orange text-white shadow-orange-brand before:absolute before:inset-0 before:rounded-[inherit] before:bg-[linear-gradient(180deg,rgba(255,255,255,0.15),transparent_50%)] before:pointer-events-none hover:-translate-y-px hover:shadow-orange-lg-brand",
        secondary:
          "bg-white text-ink border border-line shadow-xs-brand hover:border-line-strong hover:-translate-y-px hover:shadow-sm-brand",
        ghost: "text-ink-2 hover:bg-black/5",
        dark: "bg-ink text-white shadow-md-brand hover:bg-black hover:-translate-y-px hover:shadow-lg-brand",
        danger: "bg-danger-500 text-white shadow-md-brand hover:bg-danger-600",
      },
      size: {
        default: "px-[18px] py-[10px] text-[13px]",
        sm: "px-3 py-1.5 text-[12px] rounded-[9px]",
        lg: "px-6 py-3 text-[14px] rounded-[14px]",
        icon: "h-[38px] w-[38px] rounded-[11px] px-0 py-0",
      },
    },
    defaultVariants: {
      variant: "primary",
      size: "default",
    },
  },
);

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {}

export const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, type = "button", ...props }, ref) => {
    return (
      <button
        ref={ref}
        type={type}
        className={cn(buttonVariants({ variant, size }), className)}
        {...props}
      />
    );
  },
);
Button.displayName = "Button";

export { buttonVariants };
