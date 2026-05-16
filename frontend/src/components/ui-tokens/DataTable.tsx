"use client";

import * as React from "react";
import { ChevronUp, ChevronDown, Search, ChevronLeft, ChevronRight } from "lucide-react";
import { useTranslations } from "next-intl";
import { cn } from "@/lib/utils";

export type Column<T> = {
  id: string;
  header: React.ReactNode;
  accessor: (row: T) => unknown;
  cell?: (row: T) => React.ReactNode;
  sortable?: boolean;
  align?: "left" | "right" | "center";
  className?: string;
};

export type DataTableProps<T> = {
  data: T[];
  columns: Column<T>[];
  rowKey: (row: T) => string;
  searchPlaceholder?: string;
  searchable?: boolean;
  pageSize?: number;
  initialSortBy?: string;
  initialSortOrder?: "asc" | "desc";
  emptyMessage?: React.ReactNode;
  onRowClick?: (row: T) => void;
  className?: string;
};

function defaultCompare(a: unknown, b: unknown): number {
  if (a == null) return b == null ? 0 : -1;
  if (b == null) return 1;
  if (typeof a === "number" && typeof b === "number") return a - b;
  return String(a).localeCompare(String(b), undefined, { numeric: true });
}

export function DataTable<T>({
  data,
  columns,
  rowKey,
  searchPlaceholder,
  searchable = true,
  pageSize = 20,
  initialSortBy,
  initialSortOrder = "asc",
  emptyMessage,
  onRowClick,
  className,
}: DataTableProps<T>) {
  const tCommon = useTranslations("common");
  const [search, setSearch] = React.useState("");
  const [page, setPage] = React.useState(0);
  const [sortBy, setSortBy] = React.useState<string | undefined>(initialSortBy);
  const [sortOrder, setSortOrder] = React.useState<"asc" | "desc">(initialSortOrder);

  const filtered = React.useMemo(() => {
    if (!search) return data;
    const q = search.toLowerCase();
    return data.filter((row) =>
      columns.some((col) => {
        const v = col.accessor(row);
        return v != null && String(v).toLowerCase().includes(q);
      }),
    );
  }, [data, search, columns]);

  const sorted = React.useMemo(() => {
    if (!sortBy) return filtered;
    const col = columns.find((c) => c.id === sortBy);
    if (!col) return filtered;
    const sign = sortOrder === "asc" ? 1 : -1;
    return [...filtered].sort((a, b) => sign * defaultCompare(col.accessor(a), col.accessor(b)));
  }, [filtered, sortBy, sortOrder, columns]);

  const totalPages = Math.max(1, Math.ceil(sorted.length / pageSize));
  const safePage = Math.min(page, totalPages - 1);
  const paged = sorted.slice(safePage * pageSize, (safePage + 1) * pageSize);

  const toggleSort = (id: string) => {
    if (sortBy !== id) {
      setSortBy(id);
      setSortOrder("asc");
    } else {
      setSortOrder(sortOrder === "asc" ? "desc" : "asc");
    }
  };

  return (
    <div className={cn("flex flex-col gap-3", className)}>
      {searchable && (
        <div className="flex items-center gap-2.5 rounded-xl border border-line bg-white px-3.5 py-2 text-ink-3 shadow-elev-sm">
          <Search className="size-4" aria-hidden />
          <input
            type="search"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            placeholder={searchPlaceholder ?? tCommon("search")}
            className="grow border-none bg-transparent text-[13.5px] text-ink outline-none placeholder:text-ink-4"
            aria-label={tCommon("search")}
          />
        </div>
      )}

      <div className="overflow-hidden rounded-2xl border border-line bg-white shadow-elev-sm">
        <table className="w-full border-separate border-spacing-0">
          <thead>
            <tr>
              {columns.map((col, idx) => {
                const isFirst = idx === 0;
                const isLast = idx === columns.length - 1;
                const active = sortBy === col.id;
                return (
                  <th
                    key={col.id}
                    scope="col"
                    className={cn(
                      "border-b border-line bg-gradient-to-b from-cream-dim to-cream-soft py-3 text-left text-[11px] font-semibold uppercase tracking-wider text-ink-3",
                      isFirst ? "pl-6 pr-4" : "px-4",
                      isLast && "pr-6",
                      col.align === "right" && "text-right",
                      col.align === "center" && "text-center",
                      col.className,
                    )}
                  >
                    {col.sortable ? (
                      <button
                        type="button"
                        onClick={() => toggleSort(col.id)}
                        className="inline-flex items-center gap-1 hover:text-ink"
                      >
                        {col.header}
                        {active &&
                          (sortOrder === "asc" ? (
                            <ChevronUp className="size-3" />
                          ) : (
                            <ChevronDown className="size-3" />
                          ))}
                      </button>
                    ) : (
                      col.header
                    )}
                  </th>
                );
              })}
            </tr>
          </thead>
          <tbody>
            {paged.length === 0 && (
              <tr>
                <td
                  colSpan={columns.length}
                  className="px-6 py-12 text-center text-[13px] text-ink-3"
                >
                  {emptyMessage ?? tCommon("noResults")}
                </td>
              </tr>
            )}
            {paged.map((row) => {
              const key = rowKey(row);
              const clickable = !!onRowClick;
              return (
                <tr
                  key={key}
                  onClick={clickable ? () => onRowClick(row) : undefined}
                  className={cn(
                    "group transition-colors",
                    clickable && "cursor-pointer hover:bg-brand-50/40",
                  )}
                >
                  {columns.map((col, idx) => {
                    const isFirst = idx === 0;
                    const isLast = idx === columns.length - 1;
                    return (
                      <td
                        key={col.id}
                        className={cn(
                          "border-b border-line-soft py-3.5 text-[13.5px] text-ink-2 last:border-b-0",
                          isFirst
                            ? "pl-6 pr-4 group-hover:shadow-[inset_3px_0_0_var(--color-brand-500)]"
                            : "px-4",
                          isLast && "pr-6",
                          col.align === "right" && "text-right",
                          col.align === "center" && "text-center",
                          col.className,
                        )}
                      >
                        {col.cell ? col.cell(row) : (col.accessor(row) as React.ReactNode)}
                      </td>
                    );
                  })}
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>

      {totalPages > 1 && (
        <div className="flex items-center justify-between gap-3 px-1 text-[12.5px] text-ink-3">
          <span>
            {sorted.length} —{" "}
            <span className="tabular">
              {safePage * pageSize + 1}–{Math.min((safePage + 1) * pageSize, sorted.length)}
            </span>
          </span>
          <div className="flex items-center gap-1">
            <button
              type="button"
              onClick={() => setPage(Math.max(0, safePage - 1))}
              disabled={safePage === 0}
              className="grid size-8 place-items-center rounded-lg border border-line bg-white shadow-elev-sm transition-all hover:border-line-strong disabled:opacity-40"
              aria-label={tCommon("actions.previous")}
            >
              <ChevronLeft className="size-3.5" />
            </button>
            <span className="tabular px-2 font-medium text-ink-2">
              {safePage + 1} / {totalPages}
            </span>
            <button
              type="button"
              onClick={() => setPage(Math.min(totalPages - 1, safePage + 1))}
              disabled={safePage >= totalPages - 1}
              className="grid size-8 place-items-center rounded-lg border border-line bg-white shadow-elev-sm transition-all hover:border-line-strong disabled:opacity-40"
              aria-label={tCommon("actions.next")}
            >
              <ChevronRight className="size-3.5" />
            </button>
          </div>
        </div>
      )}
    </div>
  );
}

