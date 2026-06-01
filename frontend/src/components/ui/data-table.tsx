import * as React from "react";

import { cn } from "@/lib/utils";

export type Column<T> = {
  key: string;
  header: React.ReactNode;
  cell: (row: T) => React.ReactNode;
  className?: string;
  headClassName?: string;
};

export interface DataTableProps<T> {
  columns: Column<T>[];
  data: T[];
  rowKey: (row: T) => string;
  empty?: React.ReactNode;
  className?: string;
  onRowClick?: (row: T) => void;
}

export function DataTable<T>({
  columns,
  data,
  rowKey,
  empty,
  className,
  onRowClick,
}: DataTableProps<T>) {
  if (data.length === 0 && empty !== undefined) {
    return (
      <div className="rounded-[20px] border border-line bg-white p-10 text-center text-ink-3">
        {empty}
      </div>
    );
  }

  return (
    <div className={cn("overflow-hidden rounded-[20px] border border-line bg-white shadow-sm-brand", className)}>
      <table className="w-full border-collapse">
        <thead>
          <tr>
            {columns.map((col) => (
              <th
                key={col.key}
                className={cn(
                  "border-b border-line px-4.5 py-3.5 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3",
                  "bg-[linear-gradient(180deg,var(--color-bg-dim)_0%,var(--color-bg-soft)_100%)]",
                  "first:pl-6 last:pr-6",
                  col.headClassName,
                )}
              >
                {col.header}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {data.map((row) => (
            <tr
              key={rowKey(row)}
              onClick={onRowClick ? () => onRowClick(row) : undefined}
              className={cn(
                "group relative border-b border-line-soft transition-colors duration-100 last:border-b-0",
                onRowClick && "cursor-pointer",
                "hover:bg-orange-500/[0.035]",
              )}
            >
              {columns.map((col, idx) => (
                <td
                  key={col.key}
                  className={cn(
                    "px-4.5 py-3.5 text-[13.5px] align-middle text-ink-2",
                    "first:pl-6 last:pr-6",
                    idx === 0 &&
                      "group-hover:shadow-[inset_3px_0_0_var(--color-orange-500)]",
                    col.className,
                  )}
                >
                  {col.cell(row)}
                </td>
              ))}
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
