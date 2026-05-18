import { setRequestLocale } from "next-intl/server";
import { TrainingDetailClient } from "./training-detail-client";

export default async function TrainingDetailPage({
  params,
}: {
  params: Promise<{ locale: string; id: string }>;
}) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <TrainingDetailClient id={id} />;
}
