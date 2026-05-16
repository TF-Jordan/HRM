export const locales = ["fr", "en"] as const;
export const defaultLocale = "fr" as const;

export type Locale = (typeof locales)[number];

export function isLocale(value: string): value is Locale {
  return (locales as readonly string[]).includes(value);
}

export const localeLabels: Record<Locale, string> = {
  fr: "Français",
  en: "English",
};

export const currencyByLocale: Record<Locale, string> = {
  fr: "XAF",
  en: "XAF",
};
