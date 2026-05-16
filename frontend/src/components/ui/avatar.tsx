"use client";

import * as React from "react";
import * as AvatarPrimitive from "@radix-ui/react-avatar";
import { cva, type VariantProps } from "class-variance-authority";
import { cn, initials } from "@/lib/utils";

const avatarVariants = cva("relative inline-flex shrink-0 overflow-hidden", {
  variants: {
    size: {
      sm: "size-7 text-[10px]",
      default: "size-8 text-[11.5px]",
      lg: "size-11 text-base",
      xl: "size-20 text-[28px] rounded-[22px]",
    },
    shape: {
      circle: "rounded-full",
      square: "rounded-xl",
    },
    tone: {
      orange: "bg-grad-orange",
      blue: "bg-grad-blue",
      green: "bg-grad-green",
      violet: "bg-grad-violet",
      amber: "bg-grad-amber",
      teal: "bg-grad-teal",
      dark: "bg-grad-dark",
    },
  },
  defaultVariants: { size: "default", shape: "circle", tone: "orange" },
});

export interface AvatarProps
  extends React.ComponentPropsWithoutRef<typeof AvatarPrimitive.Root>,
    VariantProps<typeof avatarVariants> {
  name?: string;
  src?: string;
}

const Avatar = React.forwardRef<React.ElementRef<typeof AvatarPrimitive.Root>, AvatarProps>(
  ({ className, name, src, size, shape, tone, ...props }, ref) => {
    return (
      <AvatarPrimitive.Root
        ref={ref}
        className={cn(avatarVariants({ size, shape, tone }), className)}
        {...props}
      >
        {src && (
          <AvatarPrimitive.Image
            className="aspect-square size-full object-cover"
            src={src}
            alt={name ?? ""}
          />
        )}
        <AvatarPrimitive.Fallback className="grid size-full place-items-center font-bold text-white">
          {initials(name)}
        </AvatarPrimitive.Fallback>
      </AvatarPrimitive.Root>
    );
  },
);
Avatar.displayName = "Avatar";

export { Avatar };
