import { setRequestLocale } from "next-intl/server";

import { NewTrainingForm } from "@/components/trainings/new-training-form";

export default async function Page({ params }: PageProps<"/[locale]/drh/trainings/new">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <NewTrainingForm />;
}
