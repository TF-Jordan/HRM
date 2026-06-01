import type { Metadata } from "next";
import { hasLocale, NextIntlClientProvider } from "next-intl";
import { setRequestLocale } from "next-intl/server";
import { notFound } from "next/navigation";
import { Toaster } from "sonner";

import { QueryProvider } from "@/components/providers/query-provider";
import { routing } from "@/i18n/routing";

import "../globals.css";

export const metadata: Metadata = {
  title: {
    default: "HR Core",
    template: "%s · HR Core",
  },
  description: "Plateforme RH multi-tenant — HR Core",
};

export function generateStaticParams() {
  return routing.locales.map((locale) => ({ locale }));
}

export default async function LocaleLayout({
  children,
  params,
}: LayoutProps<"/[locale]">) {
  const { locale } = await params;
  if (!hasLocale(routing.locales, locale)) {
    notFound();
  }
  setRequestLocale(locale);

  return (
    <html lang={locale} suppressHydrationWarning>
      <body className="min-h-screen bg-bg text-ink antialiased">
        <NextIntlClientProvider>
          <QueryProvider>
            <div className="relative z-10">{children}</div>
            <Toaster richColors position="top-right" closeButton />
          </QueryProvider>
        </NextIntlClientProvider>
      </body>
    </html>
  );
}
