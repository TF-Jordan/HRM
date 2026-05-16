import * as React from "react";
import { ChevronRight } from "lucide-react";
import { cn } from "@/lib/utils";

export type Crumb = { label: string; href?: string };

export type PageHeaderProps = {
  title: React.ReactNode;
  subtitle?: React.ReactNode;
  ucBadge?: string; // e.g. "UC-09"
  crumbs?: Crumb[];
  actions?: React.ReactNode;
  className?: string;
};

export function PageHeader({
  title,
  subtitle,
  ucBadge,
  crumbs,
  actions,
  className,
}: PageHeaderProps) {
  return (
    <div className={cn("mb-7 flex flex-wrap items-end justify-between gap-6", className)}>
      <div>
        {(crumbs?.length || ucBadge) && (
          <nav
            aria-label="Fil d'Ariane"
            className="mb-2 flex items-center gap-1.5 text-[12px] font-medium text-ink-3"
          >
            {ucBadge && (
              <span className="rounded-md bg-status-blue-50 px-1.5 py-0.5 font-mono text-[10px] font-semibold text-status-blue-600">
                {ucBadge}
              </span>
            )}
            {crumbs?.map((c, idx) => (
              <React.Fragment key={idx}>
                {idx > 0 && <ChevronRight className="size-3 opacity-45" aria-hidden />}
                {c.href ? (
                  <a href={c.href} className="hover:text-ink-2">
                    {c.label}
                  </a>
                ) : (
                  <span>{c.label}</span>
                )}
              </React.Fragment>
            ))}
          </nav>
        )}
        <h1 className="font-display text-[34px] font-extrabold leading-[1.15] tracking-tight text-ink">
          {title}
        </h1>
        {subtitle && <p className="mt-1.5 text-[14.5px] text-ink-3">{subtitle}</p>}
      </div>
      {actions && <div className="flex flex-wrap items-center gap-2">{actions}</div>}
    </div>
  );
}
