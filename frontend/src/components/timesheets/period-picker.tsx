"use client";

import { ChevronLeft, ChevronRight } from "lucide-react";

import { formatPeriod } from "@/lib/format";

function shift(periode: string, months: number): string {
  const [y, m] = periode.split("-").map(Number);
  const date = new Date(y, (m ?? 1) - 1 + months, 1);
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, "0")}`;
}

export function PeriodPicker({
  value,
  onChange,
  maxPeriode,
}: {
  value: string;
  onChange: (next: string) => void;
  maxPeriode?: string;
}) {
  const canGoForward = !maxPeriode || value < maxPeriode;
  return (
    <div className="inline-flex items-center gap-1 rounded-xl border border-line bg-white px-1.5 py-1 shadow-xs-brand">
      <button
        type="button"
        onClick={() => onChange(shift(value, -1))}
        className="grid h-8 w-8 place-items-center rounded-[9px] text-ink-3 hover:bg-bg-soft hover:text-ink"
        aria-label="Mois précédent"
      >
        <ChevronLeft className="h-4 w-4" />
      </button>
      <span className="min-w-[140px] text-center text-[13px] font-semibold capitalize text-ink">
        {formatPeriod(value, "fr")}
      </span>
      <button
        type="button"
        disabled={!canGoForward}
        onClick={() => canGoForward && onChange(shift(value, 1))}
        className="grid h-8 w-8 place-items-center rounded-[9px] text-ink-3 hover:bg-bg-soft hover:text-ink disabled:cursor-not-allowed disabled:opacity-40"
        aria-label="Mois suivant"
      >
        <ChevronRight className="h-4 w-4" />
      </button>
    </div>
  );
}
