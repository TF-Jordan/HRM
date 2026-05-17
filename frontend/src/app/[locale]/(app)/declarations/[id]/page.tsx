import { setRequestLocale } from "next-intl/server";
import { DeclarationDetailClient } from "./declaration-detail-client";

export default async function DeclarationDetailPage({
  params,
}: {
  params: Promise<{ locale: string; id: string }>;
}) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <DeclarationDetailClient id={id} />;
}
