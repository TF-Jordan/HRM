import { setRequestLocale } from "next-intl/server";
import { TrainingBudgetsClient } from "./training-budgets-client";

export default async function TrainingBudgetsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <TrainingBudgetsClient />;
}
