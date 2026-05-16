import { hasLocale, NextIntlClientProvider } from "next-intl";
import { setRequestLocale, getMessages } from "next-intl/server";
import { notFound } from "next/navigation";
import { Toaster } from "sonner";
import { QueryProvider } from "@/components/providers/QueryProvider";
import { routing } from "@/i18n/routing";

export function generateStaticParams() {
  return routing.locales.map((locale) => ({ locale }));
}

export default async function LocaleLayout({
  children,
  params,
}: {
  children: React.ReactNode;
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  if (!hasLocale(routing.locales, locale)) notFound();
  setRequestLocale(locale);

  const messages = await getMessages();

  return (
    <NextIntlClientProvider messages={messages} locale={locale}>
      <QueryProvider>
        {children}
        <Toaster
          richColors
          position="bottom-right"
          toastOptions={{
            classNames: {
              toast: "rounded-2xl border border-line bg-white shadow-elev-lg",
            },
          }}
        />
      </QueryProvider>
    </NextIntlClientProvider>
  );
}
