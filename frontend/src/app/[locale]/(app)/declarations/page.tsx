import { setRequestLocale } from "next-intl/server";
import { DeclarationsClient } from "./declarations-client";

export default async function DeclarationsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <DeclarationsClient />;
}
