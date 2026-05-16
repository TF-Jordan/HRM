import * as React from "react";
import { Slot } from "@radix-ui/react-slot";
import { cva, type VariantProps } from "class-variance-authority";
import { cn } from "@/lib/utils";

const buttonVariants = cva(
  "inline-flex items-center justify-center gap-2 whitespace-nowrap rounded-xl text-[13px] font-semibold tracking-tight transition-all duration-200 disabled:pointer-events-none disabled:opacity-50 active:scale-[0.98] [&_svg]:size-4 [&_svg]:shrink-0",
  {
    variants: {
      variant: {
        primary:
          "bg-grad-orange text-white shadow-brand hover:shadow-brand-lg hover:-translate-y-px relative isolate before:absolute before:inset-0 before:rounded-[inherit] before:bg-[linear-gradient(180deg,rgba(255,255,255,0.15),transparent_50%)] before:pointer-events-none",
        secondary:
          "bg-white text-ink border border-line shadow-elev-sm hover:border-line-strong hover:shadow-md hover:-translate-y-px",
        ghost: "text-ink-2 hover:bg-black/5 hover:text-ink",
        dark: "bg-ink text-white shadow-elev-md hover:bg-black hover:-translate-y-px hover:shadow-elev-lg",
        destructive:
          "bg-status-red-500 text-white shadow-elev-sm hover:bg-status-red-600 hover:-translate-y-px",
        link: "text-brand-600 underline-offset-4 hover:underline hover:text-brand-700",
      },
      size: {
        sm: "h-8 px-3 text-xs rounded-[9px]",
        default: "h-10 px-4",
        lg: "h-12 px-6 text-sm rounded-2xl",
        icon: "h-10 w-10 rounded-xl",
      },
    },
    defaultVariants: { variant: "primary", size: "default" },
  },
);

export interface ButtonProps
  extends React.ButtonHTMLAttributes<HTMLButtonElement>,
    VariantProps<typeof buttonVariants> {
  asChild?: boolean;
}

const Button = React.forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant, size, asChild = false, ...props }, ref) => {
    const Comp = asChild ? Slot : "button";
    return (
      <Comp
        ref={ref}
        className={cn(buttonVariants({ variant, size }), className)}
        {...props}
      />
    );
  },
);
Button.displayName = "Button";

export { Button, buttonVariants };
