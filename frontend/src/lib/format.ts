import { format as dateFnsFormat, formatDistance, parseISO } from "date-fns";
import { enUS, fr as frLocale } from "date-fns/locale";

const locales = { fr: frLocale, en: enUS } as const;
type AppLocale = keyof typeof locales;

/**
 * Format a monetary amount in the given currency (default XAF — no decimals).
 * FCFA n'a PAS de centimes — toujours afficher en entiers.
 */
export function formatMoney(
  amount: number,
  options?: { currency?: string; locale?: AppLocale; withCurrency?: boolean },
): string {
  const currency = options?.currency ?? "XAF";
  const locale = options?.locale ?? "fr";
  const formatter = new Intl.NumberFormat(locale === "fr" ? "fr-FR" : "en-US", {
    style: options?.withCurrency === false ? "decimal" : "currency",
    currency,
    maximumFractionDigits: 0,
    minimumFractionDigits: 0,
  });
  return formatter.format(Math.round(amount));
}

export function formatNumber(value: number, locale: AppLocale = "fr"): string {
  return new Intl.NumberFormat(locale === "fr" ? "fr-FR" : "en-US").format(value);
}

export function formatPercent(value: number, locale: AppLocale = "fr", fractionDigits = 1): string {
  return new Intl.NumberFormat(locale === "fr" ? "fr-FR" : "en-US", {
    style: "percent",
    maximumFractionDigits: fractionDigits,
    minimumFractionDigits: 0,
  }).format(value);
}

function toDate(value: Date | string): Date {
  return value instanceof Date ? value : parseISO(value);
}

export function formatDate(
  value: Date | string,
  options?: { locale?: AppLocale; pattern?: string },
): string {
  const locale = options?.locale ?? "fr";
  const pattern = options?.pattern ?? "dd/MM/yyyy";
  return dateFnsFormat(toDate(value), pattern, { locale: locales[locale] });
}

export function formatDateLong(value: Date | string, locale: AppLocale = "fr"): string {
  return dateFnsFormat(toDate(value), locale === "fr" ? "d MMMM yyyy" : "MMMM d, yyyy", {
    locale: locales[locale],
  });
}

export function formatDateTime(value: Date | string, locale: AppLocale = "fr"): string {
  const pattern = locale === "fr" ? "dd/MM/yyyy 'à' HH'h'mm" : "MM/dd/yyyy 'at' HH:mm";
  return dateFnsFormat(toDate(value), pattern, { locale: locales[locale] });
}

export function formatPeriod(period: string, locale: AppLocale = "fr"): string {
  // period = "YYYY-MM"
  const [year, month] = period.split("-").map(Number);
  const date = new Date(year, (month ?? 1) - 1, 1);
  return dateFnsFormat(date, locale === "fr" ? "MMMM yyyy" : "MMMM yyyy", {
    locale: locales[locale],
  });
}

export function formatRelative(value: Date | string, locale: AppLocale = "fr"): string {
  return formatDistance(toDate(value), new Date(), { addSuffix: true, locale: locales[locale] });
}
