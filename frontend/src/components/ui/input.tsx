import * as React from "react";

import { cn } from "@/lib/utils";

export interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {}

export const Input = React.forwardRef<HTMLInputElement, InputProps>(
  ({ className, type = "text", ...props }, ref) => {
    return (
      <input
        ref={ref}
        type={type}
        className={cn(
          "w-full rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none transition-all duration-200 ease-[var(--ease-brand)] placeholder:text-ink-4 focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12 disabled:cursor-not-allowed disabled:opacity-50",
          className,
        )}
        {...props}
      />
    );
  },
);
Input.displayName = "Input";

export interface TextareaProps extends React.TextareaHTMLAttributes<HTMLTextAreaElement> {}

export const Textarea = React.forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ className, ...props }, ref) => {
    return (
      <textarea
        ref={ref}
        className={cn(
          "min-h-[92px] w-full resize-y rounded-[11px] border border-line bg-white px-3.5 py-[11px] text-[13.5px] text-ink shadow-xs-brand outline-none transition-all duration-200 ease-[var(--ease-brand)] placeholder:text-ink-4 focus:border-orange-400 focus:ring-4 focus:ring-orange-500/12",
          className,
        )}
        {...props}
      />
    );
  },
);
Textarea.displayName = "Textarea";

export function Label({
  className,
  ...props
}: React.LabelHTMLAttributes<HTMLLabelElement>) {
  return (
    <label
      className={cn("text-[12px] font-medium text-ink-2", className)}
      {...props}
    />
  );
}

export function Field({
  label,
  children,
  hint,
  error,
  className,
}: {
  label?: string;
  children: React.ReactNode;
  hint?: string;
  error?: string;
  className?: string;
}) {
  return (
    <div className={cn("flex flex-col gap-1.5", className)}>
      {label && <Label>{label}</Label>}
      {children}
      {hint && !error && <p className="text-[11.5px] text-ink-4">{hint}</p>}
      {error && <p className="text-[11.5px] text-danger-600">{error}</p>}
    </div>
  );
}
