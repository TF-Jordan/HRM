import { ChevronRight } from "lucide-react";
import * as React from "react";

import { cn } from "@/lib/utils";

export interface BreadcrumbEntry {
  label: string;
  href?: string;
}

export interface PageHeaderProps {
  title: React.ReactNode;
  subtitle?: React.ReactNode;
  ucBadge?: string;
  breadcrumb?: BreadcrumbEntry[];
  actions?: React.ReactNode;
  className?: string;
}

export function PageHeader({
  title,
  subtitle,
  ucBadge,
  breadcrumb,
  actions,
  className,
}: PageHeaderProps) {
  return (
    <header className={cn("mb-7 flex flex-wrap items-end justify-between gap-6", className)}>
      <div className="min-w-0 flex-1">
        {(breadcrumb || ucBadge) && (
          <div className="mb-2 flex flex-wrap items-center gap-2 text-[12px] font-medium text-ink-3">
            {ucBadge && (
              <span className="inline-flex items-center rounded-md border border-line bg-bg-soft px-2 py-0.5 font-mono-tabular text-[10px] font-semibold uppercase tracking-wider text-ink-2">
                {ucBadge}
              </span>
            )}
            {breadcrumb?.map((entry, idx) => (
              <React.Fragment key={`${entry.label}-${idx}`}>
                {idx > 0 && <ChevronRight className="h-3 w-3 opacity-45" aria-hidden="true" />}
                {entry.href ? (
                  <a href={entry.href} className="hover:text-ink-2 hover:underline">
                    {entry.label}
                  </a>
                ) : (
                  <span>{entry.label}</span>
                )}
              </React.Fragment>
            ))}
          </div>
        )}
        <h1 className="font-display text-[34px] font-extrabold leading-[1.15] tracking-tight text-ink">
          {title}
        </h1>
        {subtitle && <p className="mt-1.5 text-[14.5px] text-ink-3">{subtitle}</p>}
      </div>
      {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
    </header>
  );
}
