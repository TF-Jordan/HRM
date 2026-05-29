import { hasLocale } from "next-intl";
import { getRequestConfig } from "next-intl/server";

import { routing } from "./routing";

async function loadMessages(locale: string) {
  const namespaces = [
    "common",
    "auth",
    "shell",
    "errors",
    "validation",
    "design",
    "admin",
    "employees",
    "leaves",
  ];
  const entries = await Promise.all(
    namespaces.map(async (ns) => {
      try {
        const mod = await import(`./messages/${locale}/${ns}.json`);
        return [ns, mod.default] as const;
      } catch {
        return [ns, {}] as const;
      }
    }),
  );
  return Object.fromEntries(entries);
}

export default getRequestConfig(async ({ requestLocale }) => {
  const requested = await requestLocale;
  const locale = hasLocale(routing.locales, requested) ? requested : routing.defaultLocale;
  return {
    locale,
    messages: await loadMessages(locale),
  };
});
