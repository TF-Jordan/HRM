import { setRequestLocale } from "next-intl/server";
import { TrainingsClient } from "./trainings-client";

export default async function TrainingsPage({
  params,
}: {
  params: Promise<{ locale: string }>;
}) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <TrainingsClient />;
}
