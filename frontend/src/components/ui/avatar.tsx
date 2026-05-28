import { cva, type VariantProps } from "class-variance-authority";
import * as React from "react";

import { cn, initials } from "@/lib/utils";

const avatarVariants = cva(
  "relative shrink-0 grid place-items-center font-bold text-white tracking-wide overflow-hidden ring-2 ring-white shadow-xs-brand",
  {
    variants: {
      tone: {
        orange: "bg-grad-orange",
        blue: "bg-grad-blue",
        green: "bg-grad-green",
        violet: "bg-grad-violet",
        amber: "bg-grad-amber",
        teal: "bg-grad-teal",
      },
      size: {
        sm: "h-[26px] w-[26px] text-[10px] rounded-full",
        md: "h-8 w-8 text-[11.5px] rounded-full",
        lg: "h-[46px] w-[46px] text-base rounded-full",
        xl: "h-20 w-20 text-[28px] rounded-[22px]",
      },
    },
    defaultVariants: { tone: "orange", size: "md" },
  },
);

export interface AvatarProps
  extends Omit<React.HTMLAttributes<HTMLSpanElement>, "children">,
    VariantProps<typeof avatarVariants> {
  name?: string;
  initials?: string;
  src?: string;
}

export function Avatar({ tone, size, name, initials: rawInitials, src, className, ...props }: AvatarProps) {
  const display = rawInitials ?? (name ? initials(name) : "?");
  return (
    <span className={cn(avatarVariants({ tone, size }), className)} {...props}>
      {src ? (
        // eslint-disable-next-line @next/next/no-img-element
        <img src={src} alt={name ?? "avatar"} className="h-full w-full object-cover" />
      ) : (
        <span aria-hidden="true">{display}</span>
      )}
      <span
        aria-hidden="true"
        className="pointer-events-none absolute inset-0 bg-[linear-gradient(180deg,rgba(255,255,255,0.2),transparent_50%)]"
      />
    </span>
  );
}

export function AvatarStack({ children }: { children: React.ReactNode }) {
  return <span className="inline-flex [&>*]:-ml-2.5 [&>*:first-child]:ml-0">{children}</span>;
}
