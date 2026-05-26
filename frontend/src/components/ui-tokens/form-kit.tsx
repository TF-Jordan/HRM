"use client";

import * as React from "react";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { cn } from "@/lib/utils";

/** Section block: orange dash + uppercase title + muted hint, inside a card. */
export function FormSection({
  title,
  hint,
  children,
  className,
}: {
  title: string;
  hint?: string;
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <Card className={className}>
      <CardContent className="space-y-5 p-5">
        <div className="flex flex-wrap items-center gap-2 border-b border-line-soft pb-3">
          <span aria-hidden className="h-0.5 w-6 rounded-full bg-brand-500" />
          <span className="text-[11px] font-bold uppercase tracking-[0.12em] text-ink">{title}</span>
          {hint && <span className="text-[11.5px] font-normal text-ink-4">· {hint}</span>}
        </div>
        {children}
      </CardContent>
    </Card>
  );
}

export function FieldGrid({
  children,
  cols = 2,
}: {
  children: React.ReactNode;
  cols?: 1 | 2 | 3 | 4;
}) {
  const map = { 1: "sm:grid-cols-1", 2: "sm:grid-cols-2", 3: "sm:grid-cols-3", 4: "sm:grid-cols-4" } as const;
  return <div className={cn("grid grid-cols-1 gap-4", map[cols])}>{children}</div>;
}

/** Labelled field with a right-aligned monospace field-name chip. */
export function Field({
  id,
  label,
  code,
  required,
  hint,
  error,
  children,
  className,
}: {
  id?: string;
  label: string;
  code?: string;
  required?: boolean;
  hint?: string;
  error?: string;
  children: React.ReactNode;
  className?: string;
}) {
  return (
    <div className={cn("space-y-1.5", className)}>
      <div className="flex items-center justify-between gap-2">
        <label htmlFor={id} className="flex items-center gap-1 text-[12.5px] font-semibold text-ink-2">
          <span>{label}</span>
          {required && <span className="text-brand-500">*</span>}
        </label>
        {code && (
          <span className="rounded-md bg-cream-soft px-1.5 py-0.5 font-mono text-[10px] text-ink-4">
            {code}
          </span>
        )}
      </div>
      {children}
      {hint && !error && <p className="text-[11.5px] text-ink-4">{hint}</p>}
      {error && <p className="text-[11.5px] text-status-red-600">{error}</p>}
    </div>
  );
}

export type RadioCardOption<T extends string> = {
  value: T;
  label: string;
  sublabel?: string;
  code?: string;
};

/** Grid of selectable radio cards (orange when active), for enum fields. */
export function RadioCardGroup<T extends string>({
  value,
  onChange,
  options,
  cols = 4,
  name,
}: {
  value: T | null | undefined;
  onChange: (v: T) => void;
  options: RadioCardOption<T>[];
  cols?: 1 | 2 | 3 | 4;
  name?: string;
}) {
  const map = { 1: "sm:grid-cols-1", 2: "sm:grid-cols-2", 3: "sm:grid-cols-3", 4: "sm:grid-cols-4" } as const;
  return (
    <div className={cn("grid grid-cols-1 gap-3", map[cols])} role="radiogroup">
      {options.map((opt) => {
        const active = value === opt.value;
        return (
          <button
            type="button"
            key={opt.value}
            role="radio"
            aria-checked={active}
            name={name}
            onClick={() => onChange(opt.value)}
            className={cn(
              "flex items-center gap-2.5 rounded-[12px] border p-3 text-left transition-colors",
              active
                ? "border-brand-400 bg-brand-50"
                : "border-line bg-white hover:border-brand-300",
            )}
          >
            <span
              className={cn(
                "grid size-4 shrink-0 place-items-center rounded-full border-2",
                active ? "border-brand-500" : "border-line-strong",
              )}
            >
              {active && <span className="size-2 rounded-full bg-brand-500" />}
            </span>
            <span className="min-w-0 flex-1">
              <span className="block truncate text-[13px] font-semibold text-ink">{opt.label}</span>
              {opt.sublabel && (
                <span className="block truncate text-[11.5px] text-ink-4">{opt.sublabel}</span>
              )}
            </span>
            {opt.code && (
              <span className="rounded-md bg-cream-soft px-1.5 py-0.5 font-mono text-[9.5px] text-ink-4">
                {opt.code}
              </span>
            )}
          </button>
        );
      })}
    </div>
  );
}

/** Input with a right-aligned unit suffix (e.g. XAF, jours). */
export const SuffixInput = React.forwardRef<
  HTMLInputElement,
  React.InputHTMLAttributes<HTMLInputElement> & { suffix: string }
>(({ suffix, className, ...props }, ref) => {
  return (
    <div className="relative">
      <Input ref={ref} className={cn("pr-14", className)} {...props} />
      <span className="pointer-events-none absolute right-3.5 top-1/2 -translate-y-1/2 text-[12px] font-medium text-ink-4">
        {suffix}
      </span>
    </div>
  );
});
SuffixInput.displayName = "SuffixInput";

/** Form footer: Cancel (ghost) + primary action, right-aligned. */
export function FormFooter({ children }: { children: React.ReactNode }) {
  return <div className="flex items-center justify-end gap-3 pt-1">{children}</div>;
}
