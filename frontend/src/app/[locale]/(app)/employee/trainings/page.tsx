import { setRequestLocale } from "next-intl/server";

import { TrainingsCatalog } from "@/components/trainings/trainings-catalog";

export default async function Page({ params }: PageProps<"/[locale]/employee/trainings">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <TrainingsCatalog />;
}
