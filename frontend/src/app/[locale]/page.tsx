import { redirect } from "next/navigation";
import { setRequestLocale } from "next-intl/server";

export default async function LocaleIndex({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  // Locale prefix is "as-needed" — going to /dashboard keeps the user on the
  // current locale ; the proxy will bounce to /login if no session.
  redirect("/dashboard");
}
