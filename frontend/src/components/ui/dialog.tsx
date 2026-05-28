"use client";

import { X } from "lucide-react";
import * as React from "react";

import { cn } from "@/lib/utils";

export interface DialogProps {
  open: boolean;
  onClose: () => void;
  title?: React.ReactNode;
  subtitle?: React.ReactNode;
  children: React.ReactNode;
  footer?: React.ReactNode;
  size?: "sm" | "md" | "lg";
}

export function Dialog({ open, onClose, title, subtitle, children, footer, size = "md" }: DialogProps) {
  React.useEffect(() => {
    if (!open) return;
    function onKey(e: KeyboardEvent) {
      if (e.key === "Escape") onClose();
    }
    document.addEventListener("keydown", onKey);
    return () => document.removeEventListener("keydown", onKey);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-50 grid place-items-center bg-black/40 backdrop-blur-sm fade-up"
      onClick={onClose}
      role="dialog"
      aria-modal="true"
    >
      <div
        className={cn(
          "relative w-full overflow-hidden rounded-[20px] border border-line bg-white shadow-xl-brand",
          size === "sm" && "max-w-[420px]",
          size === "md" && "max-w-[560px]",
          size === "lg" && "max-w-[760px]",
        )}
        onClick={(e) => e.stopPropagation()}
      >
        <button
          type="button"
          aria-label="Close"
          onClick={onClose}
          className="absolute right-3 top-3 grid h-8 w-8 place-items-center rounded-[10px] text-ink-3 hover:bg-bg-soft hover:text-ink"
        >
          <X className="h-4 w-4" />
        </button>
        {(title || subtitle) && (
          <div className="border-b border-line-soft px-6 py-5">
            {title && (
              <h2 className="font-display text-[20px] font-bold tracking-tight text-ink">{title}</h2>
            )}
            {subtitle && <p className="mt-1 text-[13px] text-ink-3">{subtitle}</p>}
          </div>
        )}
        <div className="px-6 py-5">{children}</div>
        {footer && (
          <div className="flex items-center justify-end gap-2 border-t border-line-soft bg-bg-soft/50 px-6 py-4">
            {footer}
          </div>
        )}
      </div>
    </div>
  );
}
