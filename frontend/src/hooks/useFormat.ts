"use client";

import { useLocale } from "next-intl";
import { useMemo } from "react";

export function useFormat() {
  const locale = useLocale();
  return useMemo(() => {
    const moneyXAF = new Intl.NumberFormat(locale, {
      style: "currency",
      currency: "XAF",
      maximumFractionDigits: 0,
    });
    const moneyShortFmt = new Intl.NumberFormat(locale, {
      notation: "compact",
      maximumFractionDigits: 1,
    });
    const number = new Intl.NumberFormat(locale, { maximumFractionDigits: 2 });
    const integer = new Intl.NumberFormat(locale, { maximumFractionDigits: 0 });
    const date = new Intl.DateTimeFormat(locale, {
      day: "2-digit",
      month: "short",
      year: "numeric",
    });
    return {
      money(value: number | string | null | undefined): string {
        if (value == null) return "—";
        const n = typeof value === "string" ? Number(value) : value;
        if (!Number.isFinite(n)) return "—";
        return moneyXAF.format(n);
      },
      moneyShort(value: number | string | null | undefined): string {
        if (value == null) return "—";
        const n = typeof value === "string" ? Number(value) : value;
        if (!Number.isFinite(n)) return "—";
        return `${moneyShortFmt.format(n)} XAF`;
      },
      number(value: number | null | undefined): string {
        return value == null ? "—" : number.format(value);
      },
      integer(value: number | null | undefined): string {
        return value == null ? "—" : integer.format(value);
      },
      date(value: string | Date | null | undefined): string {
        if (!value) return "—";
        const d = typeof value === "string" ? new Date(value) : value;
        if (Number.isNaN(d.getTime())) return "—";
        return date.format(d);
      },
    };
  }, [locale]);
}
