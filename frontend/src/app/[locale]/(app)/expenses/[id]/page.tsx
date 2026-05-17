import { setRequestLocale } from "next-intl/server";
import { ExpenseDetailClient } from "./expense-detail-client";

export default async function ExpenseDetailPage({
  params,
}: {
  params: Promise<{ locale: string; id: string }>;
}) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <ExpenseDetailClient id={id} />;
}
