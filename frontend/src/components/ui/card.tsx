import * as React from "react";

import { cn } from "@/lib/utils";

type CardVariant = "default" | "glass" | "dark";

export interface CardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: CardVariant;
  clickable?: boolean;
}

export const Card = React.forwardRef<HTMLDivElement, CardProps>(
  ({ className, variant = "default", clickable, ...props }, ref) => {
    return (
      <div
        ref={ref}
        className={cn(
          "relative overflow-hidden rounded-[20px] border border-line bg-white shadow-sm-brand transition-all duration-200 ease-[var(--ease-brand)]",
          "before:absolute before:inset-0 before:bg-[linear-gradient(180deg,rgba(255,255,255,0.6),transparent_30%)] before:opacity-50 before:pointer-events-none",
          variant === "glass" &&
            "bg-white/65 backdrop-blur-xl backdrop-saturate-[180%] border-white/80",
          variant === "dark" &&
            "bg-grad-dark text-white border-white/10 before:bg-[radial-gradient(circle_at_100%_0%,rgba(242,107,15,0.20)_0%,transparent_50%)] before:opacity-100",
          clickable &&
            "cursor-pointer hover:border-orange-300 hover:-translate-y-0.5 hover:shadow-lg-brand",
          className,
        )}
        {...props}
      />
    );
  },
);
Card.displayName = "Card";

export const CardContent = React.forwardRef<
  HTMLDivElement,
  React.HTMLAttributes<HTMLDivElement> & { padding?: "sm" | "md" | "lg" }
>(({ className, padding = "md", ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "relative",
      padding === "sm" && "p-4",
      padding === "md" && "p-[22px]",
      padding === "lg" && "p-[26px]",
      className,
    )}
    {...props}
  />
));
CardContent.displayName = "CardContent";

export const CardHeader = React.forwardRef<HTMLDivElement, React.HTMLAttributes<HTMLDivElement>>(
  ({ className, ...props }, ref) => (
    <div
      ref={ref}
      className={cn(
        "relative flex items-center justify-between border-b border-line-soft px-6 py-5",
        className,
      )}
      {...props}
    />
  ),
);
CardHeader.displayName = "CardHeader";

export const CardTitle = React.forwardRef<HTMLHeadingElement, React.HTMLAttributes<HTMLHeadingElement>>(
  ({ className, ...props }, ref) => (
    <h3
      ref={ref}
      className={cn("text-[16px] font-bold tracking-tight text-ink", className)}
      {...props}
    />
  ),
);
CardTitle.displayName = "CardTitle";
