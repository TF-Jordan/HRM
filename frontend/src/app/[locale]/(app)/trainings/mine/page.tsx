import { setRequestLocale } from "next-intl/server";

import { MyTrainings } from "@/components/trainings/my-trainings";

export default async function MyTrainingsPage({ params }: PageProps<"/[locale]/trainings/mine">) {
  const { locale } = await params;
  setRequestLocale(locale);
  return <MyTrainings />;
}
