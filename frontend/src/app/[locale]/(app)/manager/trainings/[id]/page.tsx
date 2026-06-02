import { setRequestLocale } from "next-intl/server";

import { TrainingDetail } from "@/components/trainings/training-detail";

export default async function Page({ params }: PageProps<"/[locale]/manager/trainings/[id]">) {
  const { locale, id } = await params;
  setRequestLocale(locale);
  return <TrainingDetail trainingId={id} />;
}
